package uk.nhs.ciao.docs.parser;

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
}
