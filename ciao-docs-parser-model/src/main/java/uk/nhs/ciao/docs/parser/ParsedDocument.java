package uk.nhs.ciao.docs.parser;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * The result of parsing a document into a collection of key/value properties.
 * <p>
 * When serialising instances of the class, Jackson uses the JavaBean accessors
 * of this class to determine which JSON properties to include. During
 * unmashalling the annotated constructor of this class is used to determine
 * the JSON to Java properties mapping.
 */
public class ParsedDocument {
	private final Document originalDocument;
	private final Map<String, Object> properties;
	private volatile StandardProperties standardProperties; // lazy-loaded
	
	/**
	 * Constructs a new parsed document from the specified original document
	 * and associated extracted properties
	 * 
	 * @param originalDocument The document that was parsed
	 * @param properties The extracted properties
	 */
	@JsonCreator
	public ParsedDocument(@JsonProperty("originalDocument") final Document originalDocument,
			@JsonProperty("properties") final Map<String, Object> properties) {
		this.originalDocument = Preconditions.checkNotNull(originalDocument);
		this.properties = Preconditions.checkNotNull(properties);
	}
	
	/**
	 * The document that was parsed
	 */
	public Document getOriginalDocument() {
		return originalDocument;
	}
	
	/**
	 * The key/value properties extracted from the document
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	/**
	 * A bean-like view of {@link #getProperties()} which exposes a set of standard properties
	 * <p>
	 * Any changes made are reflected on the underlying properties map.
	 * 
	 * @see PropertyNames
	 */
	@JsonIgnore
	public StandardProperties getStandardProperties() {
		if (standardProperties == null) {
			standardProperties = new StandardProperties(properties);
		}
		
		return standardProperties;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("originalDocument", originalDocument)
				.add("properties", properties)
				.toString();
	}
}
