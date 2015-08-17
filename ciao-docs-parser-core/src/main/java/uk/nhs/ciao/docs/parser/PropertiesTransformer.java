package uk.nhs.ciao.docs.parser;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Transforms an incoming set of properties
 */
public class PropertiesTransformer implements PropertiesExtractor<Map<String, Object>> {
	/**
	 * If true, the transformation is performed directly on the input properties,
	 * otherwise the input properties are cloned before transformation
	 */
	private boolean inPlace = true;
	
	/**
	 * The registered list of transformation to perform.
	 * <p>
	 * The transformations are processed sequentially based on registration order
	 */
	private List<PropertiesTransformation> transformations = Lists.newArrayList();
	
	@Override
	public Map<String, Object> extractProperties(final Map<String, Object> source) throws UnsupportedDocumentTypeException {
		final Map<String, Object> destination = inPlace ? source : deepClone(source);
		
		for (final PropertiesTransformation transformation: transformations) {
			transformation.apply(source, destination);
		}
		
		return destination;
	}
	
	public boolean isInPlace() {
		return inPlace;
	}
	
	public void setInPlace(final boolean inPlace) {
		this.inPlace = inPlace;
	}
	
	public List<PropertiesTransformation> getTransformations() {
		return transformations;
	}
	
	public void setTransformations(final List<PropertiesTransformation> transformations) {
		this.transformations = Preconditions.checkNotNull(transformations);
	}
	
	public void addTransformation(final PropertiesTransformation transformation) {
		if (transformation != null) {
			transformations.add(transformation);
		}
	}
	
	public void renameProperty(final String from, final String to) {
		transformations.add(new RenamePropertyTransformation(from, to));
	}
	
	/**
	 * Transforms properties from the source map into the destinations map
	 */
	interface PropertiesTransformation {
		/**
		 * Transforms properties from the source map into the destinations map
		 * <p>
		 * The source and destination map may be the same instance.
		 */
		void apply(final Map<String, Object> source, final Map<String, Object> destination);
	}
	
	/**
	 * Renames / copies a property to a new name
	 */
	public static class RenamePropertyTransformation implements PropertiesTransformation {
		private final String from;
		private final String to;
		private final boolean retainOriginal;
		private final boolean cloneNestedProperties;
		
		/**
		 * Creates a new property rename transformation which retains the original property
		 * in the source map
		 */
		public RenamePropertyTransformation(final String from, final String to) {
			this(from, to, true, false);
		}
		
		/**
		 * Creates a new property rename transformation
		 */
		public RenamePropertyTransformation(final String from, final String to,
				final boolean retainOriginal, final boolean cloneNestedProperties) {
			this.from = Preconditions.checkNotNull(from);
			this.to = Preconditions.checkNotNull(to);
			this.retainOriginal = retainOriginal;
			this.cloneNestedProperties = cloneNestedProperties;
		}
		
		@Override
		public void apply(final Map<String, Object> source,
				final Map<String, Object> destination) {
			if (!source.containsKey(from)) {
				return;
			}
			
			Object value = retainOriginal ? source.get(from) : source.remove(from);
			if (cloneNestedProperties) {
				value = deepCloneNestedProperties(value);
			}
			
			destination.put(to, value);
		}
	}
	
	private static <K> Map<K, Object> deepClone(final Map<K, Object> map) {
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
	
	private static List<Object> deepClone(final List<?> list) {
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
	private static Object deepCloneNestedProperties(final Object value) {
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
