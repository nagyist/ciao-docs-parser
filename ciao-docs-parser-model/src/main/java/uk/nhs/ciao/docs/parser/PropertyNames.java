package uk.nhs.ciao.docs.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Names of properties that can be extracted while parsing a document and utility methods
 * associated with property names in a dynamic map structure
 * <p>
 * This list is non-exhaustive, and only defines special case properties - document
 * parsers are free to add a property with any name.
 */
public final class PropertyNames {
	private PropertyNames() {
		// Suppress default constructor
	}
	
	/**
	 * The property name used to specify document metadata
	 * <p>
	 * The expected value should form a map of key/value pairs - value can
	 * be a single object or a list
	 */
	public static final String METADATA = "metadata";
	
	/**
	 * The property name parsers can use to declare the media type of the document being parsed
	 * <p>
	 * This is equivalent to the CONTENT_TYPE HTTP header, Tika meta tag and Camel Exchange header
	 * <p>
	 * The property should be nested as a child of {@link METADATA}
	 */
	public static final String CONTENT_TYPE = "Content-Type";
	
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
			addChildNames(names, null, map, includeContainers);
		}
			
		return names;
	}
	
	private static void addChildNames(final Set<String> names, final String parent, final Map<String, Object> container, boolean includeContainers) {
		for (final Entry<String, Object> entry: container.entrySet()) {
			final String name = valueOf(parent, entry.getKey());
			final Object value = entry.getValue();
			addNameAndChildNames(names, name, value, includeContainers);
		}
	}
	
	private static void addChildNames(final Set<String> names, final String parent, final List<?> container, boolean includeContainers) {
		int index = 0;
		for (final Object value: container) {
			final String name = valueOf(parent, index);
			addNameAndChildNames(names, name, value, includeContainers);
			index++;
		}
	}
	
	private static void addNameAndChildNames(final Set<String> names, final String name, final Object value, boolean includeContainers) {
		if (includeContainers || !isContainer(value)) {
			names.add(name);
		}
		
		if (value instanceof Map<?,?>) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> map = (Map<String, Object>)value;
			addChildNames(names, name, map, includeContainers);
		} else if (value instanceof List<?>) {
			final List<?> list = (List<?>)value;				
			addChildNames(names, name, list, includeContainers);
		}
	}
	
	private static boolean isContainer(final Object value) {
		return (value instanceof Map<?, ?>) || value instanceof List<?>;
	}
}
