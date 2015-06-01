package uk.nhs.ciao.docs.parser;

import java.io.InputStream;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Represents the binary content of a named document.
 * <p>
 * The content of the document is maintained in memory.
 * <p>
 * When serialising instances of the class, Jackson uses the JavaBean accessors
 * of this class to determine which JSON properties to include.
 */
public class Document {
	private final String name;
	private final byte[] content;
	
	/**
	 * Constructs a new document instance
	 * 
	 * @param name The name of the document
	 * @param content The document content - the byte array is stored directly,
	 * 			no defensive copies are made
	 */
	public Document(final String name, final byte[] content) {
		this.name = Preconditions.checkNotNull(name);
		this.content = Preconditions.checkNotNull(content);
	}
	
	/**
	 * The name of the document (e.g. file name)
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The content of the document
	 * <p>
	 * Jackson serialises this property using Base64 encoding.
	 */
	public byte[] getContent() {
		return content;
	}
	
	/**
	 * The content of the document as an input stream
	 */
	@JsonIgnore
	public InputStream getStream() {
		return new ByteArrayInputStream(content);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("size", content.length)
				.toString();
	}
	
	/**
	 * Returns a document instance corresponding to the specified Camel message
	 * <p>
	 * The camel FILE_NAME header is used as the document name
	 * 
	 * @param message The camel message representation of the document
	 * @return The associated document instance
	 */
	public static Document valueOf(final Message message) {
		final String name = message.getHeader(Exchange.FILE_NAME, String.class);
		final byte[] body = message.getBody(byte[].class);
		
		return new Document(name, body);
	}
}