package uk.nhs.ciao.docs.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.nhs.ciao.util.SimpleEntry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;

/**
 * Selects a property (or properties) from a dynamic map structure.
 * <p>
 * Supported syntax is:
 * <ul>
 * <li><code>.{string}</code> - matches key in map
 * <li><code>[{integer}]</code> - matches index in list
 * <li><code>.*</code> - matches any key in map
 * <li><code>[*]</code> - matches any index in list
 * </ul>
 * <p>
 * Some examples:
 * <ul>
 * <li><code>authors[1]</code>
 * <li><code>author.firstName</code>
 * <li><code>author.*</code>
 * <li><code>authors[*]</code>
 * <li><code>authors[*].*</code>
 * </ul>
 */
public final class PropertySelector {		
	/**
	 * Selects the root of a properties map
	 */
	private static final PropertySelector ROOT = new PropertySelector(new Object[0]);
	
	private final Object[] segments;
	private final boolean multi;
	private int hash;
	
	/**
	 * Returns a selector matching the specified path
	 */
	public static PropertySelector valueOf(final String path) {
		final boolean allowWildcards = true;
		return Strings.isNullOrEmpty(path) ? ROOT : new PropertySelector(PropertyPath.parse(path, allowWildcards));
	}
	
	PropertySelector(final Object[] segments) {
		this.segments = segments;
		this.multi = PropertyPath.containsWildcard(segments);
	}
	
	/**
	 * Tests if this is the root selector
	 */
	public boolean isRoot() {
		return segments.length == 0;
	}
	
	/**
	 * Tests if this selector <em>may</em> select multiple matches
	 * (i.e. contains a wildcard)
	 */
	public boolean isMulti() {
		return multi;
	}
	
	/**
	 * Returns the PropertyName equivalent to this selector if a single name is identified or
	 * <code>null</code> if the path contains wildcards
	 */
	public PropertyName toPropertyName() {
		return multi ? null : new PropertyName(Arrays.copyOf(segments, segments.length));
	}
	
	/**
	 * Returns the string path associated with this selector
	 */
	public String getPath() {
		return PropertyPath.toString(segments);
	}
	
	/**
	 * Returns the parent of this selector if it is not a root
	 */
	public PropertySelector getParent() {
		return isRoot() ? null : new PropertySelector(Arrays.copyOf(segments, segments.length - 1));
	}
	
	/**
	 * Returns the child selector associated with this selector and the specified
	 * appended path segments
	 */
	public PropertySelector getChild(final String childPath) {
		final boolean allowWildcards = true;
		final Object[] childSegments = PropertyPath.parse(childPath, allowWildcards);
		Preconditions.checkArgument(childSegments.length > 0, "childPath must be provided");
		
		return new PropertySelector(ObjectArrays.concat(segments, childSegments, Object.class));
	}
	
	/**
	 * Selects the first matching property key/value pairs from the dynamic map
	 * <p>
	 * The entry key is encoded as a matching path
	 */
	public Entry<String, Object> select(final Map<String, Object> properties) {
		return select(Object.class, properties);
	}
	
	/**
	 * Selects the first matching property value from the dynamic map
	 */
	public Object selectValue(final Map<String, Object> properties) {
		return selectValue(Object.class, properties);
	}
	
	/**
	 * Selects the first matching property key/value pairs from the dynamic map
	 * <p>
	 * The entry key is encoded as a matching path
	 * 
	 * @param type The type of objects matched - any objects matching the path selector but of 
	 * 		the wrong type are ignored
	 */
	public <T> Entry<String, T> select(final Class<T> type, final Map<String, Object> properties) {
		final Entry<Object[], T> entry = PropertyPath.getEntry(type, properties, segments);
		return entry == null ? null : SimpleEntry.valueOf(PropertyPath.toString(entry.getKey()), entry.getValue());
	}
	
	/**
	 * Selects the first matching property value from the dynamic map
	 * 
	 * @param type The type of objects matched - any objects matching the path selector but of 
	 * 		the wrong type are ignored
	 */
	public <T> T selectValue(final Class<T> type, final Map<String, Object> properties) {
		return PropertyPath.getValue(type, properties, segments);
	}
	
	/**
	 * Selects all matching property key/value pairs from the dynamic map
	 * <p>
	 * The entry key is encoded as a matching path
	 */
	public Map<String, Object> selectAll(final Map<String, Object> properties) {
		return selectAll(Object.class, properties);
	}
	
	/**
	 * Selects all matching property values from the dynamic map
	 */
	public List<Object> selectAllValues(final Map<String, Object> properties) {
		return selectAllValues(Object.class, properties);
	}
	
	/**
	 * Selects all matching property key/value pairs from the dynamic map
	 * <p>
	 * The entry key is encoded as a matching path
	 * 
	 * @param type The type of objects matched - any objects matching the path selector but of 
	 * 		the wrong type are ignored
	 */
	public <T> Map<String, T> selectAll(final Class<T> type, final Map<String, Object> properties) {
		final Map<String, T> results = Maps.newLinkedHashMap();
		
		for (final Entry<Object[], T> entry: PropertyPath.findAll(type, properties, segments)) {
			results.put(PropertyPath.toString(entry.getKey()), entry.getValue());
		}
		
		return results;
	}
	
	/**
	 * Selects all matching property values from the dynamic map
	 * 
	 * @param type The type of objects matched - any objects matching the path selector but of 
	 * 		the wrong type are ignored
	 */
	public <T> List<T> selectAllValues(final Class<T> type, final Map<String, Object> properties) {
		final List<T> results = Lists.newArrayList();
		
		for (final Entry<Object[], T> entry: PropertyPath.findAll(type, properties, segments)) {
			results.add(entry.getValue());
		}
		
		return results;
	}
	
	@Override
	public int hashCode() {
		int result = hash;
		if (result == 0) {
			result = Arrays.hashCode(segments);
			hash = result; // safe to publish without volatile
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final PropertySelector other = (PropertySelector) obj;
		return Arrays.equals(segments, other.segments);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(segments);
	}
}
