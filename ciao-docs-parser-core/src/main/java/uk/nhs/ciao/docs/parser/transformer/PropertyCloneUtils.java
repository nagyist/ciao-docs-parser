package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Utility methods to clone / deep clone property object graphs
 */
public final class PropertyCloneUtils {
	private PropertyCloneUtils() {
		// Suppress default constructor
	}
	
	public static <K> Map<K, Object> deepClone(final Map<K, Object> map) {
		if (map == null) {
			return null;
		}
		
		final Map<K, Object> clone = Maps.newLinkedHashMap();
		for (final Entry<K, Object> entry: map.entrySet()) {
			Object value = entry.getValue();
			value = deepCloneNestedProperties(value);
			
			clone.put(entry.getKey(), value);
		}
		
		return clone;
	}
	
	public static List<Object> deepClone(final List<?> list) {
		if (list == null) {
			return null;
		}
		
		final List<Object> clone = Lists.newArrayList();
		for (Object value: list) {
			value = deepCloneNestedProperties(value);
			
			clone.add(value);
		}
		
		return clone;
	}
	
	@SuppressWarnings("unchecked")
	public static Object deepCloneNestedProperties(final Object value) {
		final Object result;
		
		if (value instanceof Map) {
			result = deepClone((Map<Object, Object>)value);
		} else if (value instanceof List) {
			result = deepClone((List<?>)value);
		} else {
			result = value;
		}
		
		return result;
	}
}
