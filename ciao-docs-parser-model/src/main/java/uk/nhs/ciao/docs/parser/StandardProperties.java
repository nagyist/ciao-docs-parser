package uk.nhs.ciao.docs.parser;

import static uk.nhs.ciao.docs.parser.PropertyNames.*;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * A bean-like view of a {@link ParsedDocument#getProperties()} for standard properties
 * 
 * @see PropertyNames
 */
public class StandardProperties {
	private final Map<String, Object> properties;
	private final Metadata metadata; // lazy-loaded

	public StandardProperties(final Map<String, Object> properties) {
		this.properties = Preconditions.checkNotNull(properties);
		this.metadata = new Metadata();
	}
	
	public Metadata getMetadata() {
		return metadata;
	}
	
	/**
	 * A bean-like view of ParsedDocument metadata values
	 * 
	 * @see PropertyNames#METADATA
	 */
	public class Metadata {
		/**
		 * Accessor for {@link PropertyNames#CONTENT_TYPE}
		 */
		public String getContentType() {
			return get(CONTENT_TYPE);
		}
		
		/**
		 * Mutator for {@link PropertyNames#CONTENT_TYPE}
		 */
		public void setContentType(final String contentType) {
			set(CONTENT_TYPE, contentType);
		}
		
		private String get(final String name) {
			String result = null;
			
			final Object metadataValue = properties.get(METADATA);
			if (metadataValue instanceof Map<?, ?>) {
				final Object value = ((Map<?, ?>) metadataValue).get(name);
				if (value instanceof String) {
					result = (String)value;
				}
			}
			
			return result;
		}
		
		@SuppressWarnings("unchecked")
		private void set(final String name, final String value) {
			Object metadataValue = properties.get(METADATA);
			Map<String, Object> metadataMap;
			if (metadataValue == null) {
				metadataMap = Maps.<String, Object>newLinkedHashMap();
				properties.put(METADATA, metadataMap);
			} else if (metadataValue instanceof Map<?,?>) {
				metadataMap = (Map<String, Object>)metadataValue;
			} else {
				throw new IllegalStateException(METADATA + " property is not a Map");
			}
			
			metadataMap.put(name, value);
		}
	}
}
