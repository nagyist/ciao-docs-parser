package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Utility classes associated with property names in a dynamic map structure
 */
public final class PropertyName {
	private PropertyName() {
		// Suppress default constructor
	}
	
	public static String valueOf(final String parent, final int childIndex) {
		return (parent == null ? "" : parent) + "[" + childIndex + "]";
	}
	
	public static String valueOf(final String parent, final String child) {
		if (Strings.isNullOrEmpty(parent)) {
			return Strings.nullToEmpty(child);
		} else if (Strings.isNullOrEmpty(child)) {
			return parent;
		}
		
		return parent.endsWith("]") ? (parent + child) : (parent + "." + child);
	}
	
	public static Set<String> findAll(final Map<String, Object> map, final boolean includeContainers) {
		final Set<String> names = Sets.newLinkedHashSet();
		
		if (map != null) {
			findAll(names, null, map, includeContainers);
		}
			
		return names;
	}
	
	private static void findAll(final Set<String> names, final String parent, final Map<String, Object> container, boolean includeContainers) {
		for (final Entry<String, Object> entry: container.entrySet()) {
			final String name = valueOf(parent, entry.getKey());
			final Object value = entry.getValue();
			
			if (value instanceof Map<?,?>) {
				if (includeContainers) {
					names.add(name);
				}
				
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)value;
				findAll(names, name, map, includeContainers);
			} else if (value instanceof List<?>) {
				if (includeContainers) {
					names.add(name);
				}
				
				final List<?> list = (List<?>)value;				
				findAll(names, name, list, includeContainers);
			} else {
				names.add(name);
			}
		}
	}
	
	private static void findAll(final Set<String> names, final String parent, final List<?> container, boolean includeContainers) {
		int index = 0;
		for (final Object value: container) {
			final String name = valueOf(parent, index);
			
			if (value instanceof Map<?,?>) {
				if (includeContainers) {
					names.add(name);
				}
				
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>)value;
				findAll(names, name, map, includeContainers);
			} else if (value instanceof List<?>) {
				if (includeContainers) {
					names.add(name);
				}
				
				final List<?> list = (List<?>)value;				
				findAll(names, name, list, includeContainers);
			} else {
				names.add(name);
			}
			
			index++;
		}
	}
}
