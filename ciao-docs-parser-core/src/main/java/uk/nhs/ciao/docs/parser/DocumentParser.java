package uk.nhs.ciao.docs.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * A parser capable of reading a document from a stream of bytes and
 * parsing it to extract key/value properties
 */
public interface DocumentParser {
	/**
	 * Parses a document from an stream of bytes, extracting key/value properties
	 * from it.
	 * <p>
	 * The input stream should is not closed by this method.
	 * 
	 * @param in The input stream to read
	 * @return The key/value properties extracted from the document
	 * @throws UnsupportedDocumentTypeException If document type is not supported by this parser
	 * @throws IOException If the document could not be read or parsed
	 */
	Map<String, Object> parseDocument(InputStream in) throws UnsupportedDocumentTypeException, IOException;
}
