package uk.nhs.ciao.docs.parser.extractor;

import java.util.Map;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

/**
 * Extracts key/value properties from a document
 *
 * @param <T> The document representation that properties can be extracted from
 */
public interface PropertiesExtractor<T> {
	
	/**
	 * Extracts known properties from the specified document.
	 * 
	 * @param document The document to extract properties from
	 * @return The key/value properties extracted from the document
	 * @throws UnsupportedDocumentTypeException If the type of document is not supported by this extractor
	 */
	Map<String, Object> extractProperties(T document) throws UnsupportedDocumentTypeException;
}
