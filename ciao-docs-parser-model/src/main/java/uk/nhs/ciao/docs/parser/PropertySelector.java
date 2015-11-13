package uk.nhs.ciao.docs.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.nhs.ciao.util.SimpleEntry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
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
		if (properties == null) {
			return null;
		} else if (isRoot()) {
			return type.isInstance(properties) ? SimpleEntry.valueOf("", type.cast(properties)) : null;
		}
		
		final Map<String, T> results = selectAll(type, properties);
		return Iterables.getFirst(results.entrySet(), null);
	}
	
	/**
	 * Selects the first matching property value from the dynamic map
	 * 
	 * @param type The type of objects matched - any objects matching the path selector but of 
	 * 		the wrong type are ignored
	 */
	public <T> T selectValue(final Class<T> type, final Map<String, Object> properties) {
		final Entry<String, T> entry = select(type, properties);
		return entry == null ? null : entry.getValue();
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
	public Collection<Object> selectAllValues(final Map<String, Object> properties) {
		return selectAll(properties).values();
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
		if (properties == null) {
			return Maps.<String, T>newHashMap();
		}
		
		final Map<String, T> results = Maps.newLinkedHashMap();
		if (isRoot()) {
			if (type.isInstance(properties)) {
				results.put("", type.cast(properties));
			}
		} else {
			final StringBuilder prefix = new StringBuilder();
			findAndAddSelected(type, results, prefix, properties, 0);
		}
		
		return results;
	}
	
	/**
	 * Selects all matching property values from the dynamic map
	 * 
	 * @param type The type of objects matched - any objects matching the path selector but of 
	 * 		the wrong type are ignored
	 */
	public <T> Collection<T> selectAllValues(final Class<T> type, final Map<String, Object> properties) {
		return selectAll(type, properties).values();
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

	/**
	 * Recursively finds selected key/value pairs and adds them to the results map
	 * <p>
	 * The results map is 'partially flattened' - i.e. the key values are encoded paths,
	 * however selected values may have nested maps/lists (determined by the structure
	 * of the incoming data)
	 * 
	 * @param type The type of object to match
	 * @param results The results map matching pairs are added to
	 * @param prefix The key/path prefix - for the root this is the empty string
	 * @param value The current value being matches - either a container (map/list) or leaf value
	 * @param index The segment index being matched
	 */
	private <T> void findAndAddSelected(final Class<T> type, final Map<String, T> results,
			final StringBuilder prefix, final Object value, final int index) {
		if (value == null) {
			return;
		} else if (index >= segments.length) {
			// Found a potential match
			if (type.isInstance(value)) {
				results.put(prefix.toString(), type.cast(value));
			}
			return;
		}
		
		final Object segment = segments[index];
		final int prefixLength = prefix.length();
		if (segment == PropertyPath.ANY_KEY) {
			// Loop all elements in map
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)value;
				for (final Entry<String, Object> entry: map.entrySet()) {
					if (prefixLength > 0) {
						prefix.append('.');
					}
					prefix.append(entry.getKey());
					findAndAddSelected(type, results, prefix, entry.getValue(), index + 1);
					prefix.setLength(prefixLength);
				}
			}
		} else if (segment == PropertyPath.ANY_INDEX) {
			// Loop all elements in list
			if (value instanceof List) {
				int listIndex = 0;
				for (final Object next:(List<?>)value) {
					prefix.append('[').append(listIndex).append(']');
					findAndAddSelected(type, results, prefix, next, index + 1);
					prefix.setLength(prefixLength);
					listIndex++;
				}
			}
		} else if (segment instanceof Integer) {
			// Match index in list
			final int targetIndex = (Integer)segment;
			if (value instanceof List && targetIndex < ((List<?>)value).size()) {
				final Object next = ((List<?>)value).get(targetIndex);
				prefix.append('[').append(targetIndex).append(']');
				findAndAddSelected(type, results, prefix, next, index + 1);
				prefix.setLength(prefixLength);
			}
		} else { // String
			// Match named key in map
			final String targetKey = (String)segment;
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)value;
				final Object next = map.get(targetKey);
				
				if (prefixLength > 0) {
					prefix.append('.');
				}
				prefix.append(targetKey);
				findAndAddSelected(type, results, prefix, next, index + 1);
				prefix.setLength(prefixLength);
			}
		}
	}
}
