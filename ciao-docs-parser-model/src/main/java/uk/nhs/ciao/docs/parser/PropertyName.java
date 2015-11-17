package uk.nhs.ciao.docs.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;

/**
 * Identifies a property within a dynamic property structure.
 * <p>
 * Property names are encoded as hierarchical strings made up of multiple segments:
 * <ul>
 * <li>The root property is defined as the empty string and has no parent.
 * <li>Named properties represents the keys in a Java Map - keys are separated using 'dot' notation
 * <li>Indexed properties represents the integer index within a Java List - indexes are separated using 'array' notation
 * </ul>
 * The list of reserved characters is:
 * <ul>
 * <li><code>. [ ] * \</code>
 * </ul>
 * <strong>Reserved characters must be escaped within property key segments by prefixing the '\' character.</strong>
 * <p>
 * Some example property names include:
 * <ul>
 * <li><code>name</code> -> <code>["name"]</code>
 * <li><code>[0]</code> -> <code>[0]</code>
 * <li><code>multiple.nested[1][2].keys</code> -> <code>["multiple", "nested", 1, 2, "keys"]</code>
 * <li><code>e\*n\\cod\.ed.valu\[e\]s</code> -> <code>["e*n\cod.ed", "valu[e]s"]</code>
 * </ul>
 * PropertyName instances can be used to address properties across multiple source objects.
 * <p>
 * While PropertyName uniquely identifies a property, {@link PropertySelector} can be used to find multiple
 * properties using nested segments and wildcards.
 * 
 * @see #get(Object)
 * @see #set(Object, Object)
 */
public final class PropertyName {
	private static final PropertyName ROOT = new PropertyName(new Object[0]);
	
	private final Object[] segments;
	private final String path;
	
	/**
	 * Returns a PropertyName corresponding to the specified path.
	 * <p>
	 * Segments within the path must have special/reserved characters escaped.
	 * 
	 * @param path The encoded path which defines the PropertyName
	 * @return The PropertyName corresponding to <code>path</code>
	 * @throws IllegalArgumentException If the string is not a valid PropertyName
	 */
	public static PropertyName valueOf(final String path) {
		return Strings.isNullOrEmpty(path) ? ROOT : new PropertyName(PropertyPath.parse(path));
	}
	
	/**
	 * Returns the root PropertyName
	 */
	public static PropertyName getRoot() {
		return ROOT;
	}
	
	/**
	 * Constructs a new PropertyName instance
	 * 
	 * @param segments The property segments - the array is NOT copied
	 */
	// Package private to allow construction from PropertySelector
	PropertyName(final Object[] segments) {
		this.segments = segments;
		this.path = PropertyPath.toString(segments);
	}
	
	/**
	 * Tests if this property name is the root name
	 */
	public boolean isRoot() {
		return segments.length == 0;
	}
	
	/**
	 * Tests if this property name ends with an indexed segment (i.e.
	 * addresses a property within a list)
	 */
	public boolean isIndexed() {
		return lastSegment() instanceof Integer;
	}
	
	/**
	 * Tests if this property name ends with a named segment (i.e.
	 * addresses a property within a Map)
	 */
	public boolean isNamed() {
		return lastSegment() instanceof String;
	}
	
	/**
	 * Returns the ending named segment of this property name (if named),
	 * otherwise <code>null</code> is returned.
	 */
	public String getName() {
		return isNamed() ? (String)lastSegment() : null;
	}
	
	/**
	 * Returns the ending index segment of this property name (if indexed),
	 * otherwise <code>-1</code> is returned.
	 */
	public int getIndex() {
		return isIndexed() ? (Integer)lastSegment() : -1;
	}
	
	/**
	 * Returns the encoded path of this property name
	 * <p>
	 * Individual segments within the path have special/reserved characters escaped
	 * with the '\' character. Un-escaped values can be obtained by navigating through
	 * the parent hierarchy and calling {@link #getIndex()} or {@link #getName()} as
	 * appropriate.
	 * 
	 * @return the encoded path which defines this property name
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Returns the PropertySelector equivalent to this name
	 */
	public PropertySelector toPropertySelector() {
		return new PropertySelector(Arrays.copyOf(segments, segments.length));
	}
	
	/**
	 * Returns a child of this property name.
	 * <p>
	 * The specified child PropertyName can contain multiple nested
	 * levels.
	 * 
	 * @param childName The (possibly hierarchical) child segments
	 * @return A PropertyName representing the child
	 */
	public PropertyName getChild(final PropertyName childName) {
		Preconditions.checkNotNull(childName);
		Preconditions.checkArgument(!childName.isRoot());
		
		final Object[] childSegments = ObjectArrays.concat(segments, childName.segments, Object.class);
		return new PropertyName(childSegments);
	}
	
	/**
	 * Returns the immediate named child of this property name.
	 * <p>
	 * The child name is not treated as hierarchical - special characters
	 * should not be escaped. The child name forms a single segment within
	 * the returned PropertyName path.
	 * 
	 * @param childName The child name segment
	 * @return A PropertyName representing the child
	 */
	public PropertyName getChild(final String childName) {
		Preconditions.checkNotNull(childName);
		Preconditions.checkArgument(!childName.isEmpty());
		
		final Object[] childSegments = Arrays.copyOf(segments, segments.length + 1);
		childSegments[segments.length] = childName;
		return new PropertyName(childSegments);
	}
	
	/**
	 * Returns the immediate indexed child of this property name.
	 * 
	 * @param index The index of the child
	 * @return A PropertyName representing the child
	 */
	public PropertyName getChild(final int index) {
		Preconditions.checkArgument(index >= 0);
		
		final Object[] childSegments = Arrays.copyOf(segments, segments.length + 1);
		childSegments[segments.length] = index;
		return new PropertyName(childSegments);
	}
	
	/**
	 * Returns the parent PropertyName of this name (if applicable).
	 * <p>
	 * All PropertyNames have a parent except the root.
	 */
	public PropertyName getParent() {
		return isRoot() ? null : new PropertyName(Arrays.copyOf(segments, segments.length - 1));
	}
	
	/**
	 * Lists the immediate children of this property on the specified source object.
	 * 
	 * @param source The source object to search on
	 * @return The existing child properties
	 */
	public List<PropertyName> listChildren(final Object source) {
		final List<PropertyName> children = Lists.newArrayList();

		final Object value = get(source);
		if (value instanceof List) {
			final List<?> list = (List<?>)value;
			for (int index = 0; index < list.size(); index++) {
				children.add(getChild(index));
			}
		} else if (value instanceof Map) {
			final Map<?, ?> map = (Map<?, ?>)value;
			for (final Object key: map.keySet()) {
				if (key instanceof String) {
					children.add(getChild((String)key));
				}
			}
		}
		
		return children;
	}
	
	/**
	 * Gets the value of this property on the specified source object.
	 * 
	 * @param source The source object to search on
	 * @return The current value of this property
	 */
	public Object get(final Object source) {
		return get(Object.class, source);
	}
	
	/**
	 * Gets the value of this property on the specified source object if it
	 * is of the specified kind.
	 * 
	 * @param type The type of value to return
	 * @param source The source object to search on
	 * @return The current value of this property
	 */
	public <T> T get(final Class<T> type, final Object source) {
		return PropertyPath.getValue(type, source, segments);
	}
	
	/**
	 * Sets the value of this property on the specified source object.
	 * <p>
	 * If this property is indexed by preceding siblings are missing, the 
	 * parent list will be extended with null values for the missing siblings.
	 * 
	 * @param source The source object to modify
	 * @param value The value of the property
	 * @return true if the value could be set or false otherwise
	 */
	public boolean set(final Object source, final Object value) {
		return PropertyPath.setValue(source, segments, value);
	}
	
	/**
	 * Gets the parent container of this property on the specified source object (if one exists)
	 * 
	 * @param source The source object to search
	 * @return The immediate parent container of the property or null if the
	 * 		container does not exist
	 */
	public Object getParentContainer(final Object source) {
		final boolean createIfMissing = false;
		return PropertyPath.getParentContainer(source, segments, createIfMissing);
	}
	
	/**
	 * Makes and returns the parent containers of this property on the specified source object
	 * 
	 * @param source The source object to search/modify
	 * @return The immediate parent container of this property or null
	 * 		if the container did not exist and could not be created.
	 */
	public Object makeParents(final Object source) {
		final boolean createIfMissing = true;
		return PropertyPath.getParentContainer(source, segments, createIfMissing);
	}
	
	/**
	 * Removes/deletes the current property value from the source object.
	 * <p>
	 * If an indexed property is removed from the middle of a list the entry is nulled
	 * to maintain the original size, otherwise of the property is at the end of the list
	 * the list is truncated.
	 * 
	 * @param source The source object to modify
	 * @return true if the property was removed or false if the property did
	 * 		not exist or could not be removed
	 */
	public boolean remove(final Object source) {
		return PropertyPath.remove(source, segments);
	}
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return path.equals(((PropertyName)obj).path);
	}
	
	@Override
	public String toString() {
		return getPath();
	}
	
	private Object lastSegment() {
		return segments.length == 0 ? null : segments[segments.length - 1];
	}
	
	/**
	 * Accepts the specified visitor for this property value and all
	 * child PropertyNames on the source object.
	 * 
	 * @param source The current value of this property
	 * @param visitor The visitor visting the property structure
	 */
	public void accept(final Object source, final PropertyVisitor visitor) {
		visitor.onProperty(this, source);

		// Recurse through the child structures
		if (source instanceof List<?>) {
			int index = 0;
			for (final Object childValue: (List<?>)source) {
				getChild(index).accept(childValue, visitor);
				index++;
			}
		} else if (source instanceof Map<?,?>) {
			final Map<?, ?> map = (Map<?, ?>)source;
			for (final Entry<?, ?> entry: map.entrySet()) {
				if (entry.getKey() instanceof String) {
					getChild((String)entry.getKey()).accept(entry.getValue(),
							visitor);
				}
			}
		}
	}
	
	/**
	 * Finds a complete set of all property names across the specified source structure
	 * 
	 * @param source The source object associated with the root property name
	 * @param includeContainers true if container names should be returned, or false if they should be omitted
	 * @return The set of all property names across the dynamic structure
	 */
	public static Set<PropertyName> findAll(final Object source, final boolean includeContainers) {
		final Set<PropertyName> names = Sets.newLinkedHashSet();
		getRoot().accept(source, new PropertyVisitor() {
			@Override
			public void onProperty(final PropertyName name, final Object value) {
				if (includeContainers || !ContainerType.isContainer(value)) {
					names.add(name);
				}
 			}
		});
		return names;
	}
}
