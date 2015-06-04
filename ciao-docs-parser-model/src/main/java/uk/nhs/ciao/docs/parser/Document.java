package uk.nhs.ciao.docs.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
	/**
	 * Generic media type to use when a specific media type is not specified
	 */
	private static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
	
	private final String name;
	private final byte[] content;
	private String mediaType;
	
	/**
	 * Constructs a new document instance with the default media type
	 * 
	 * @param name The name of the document
	 * @param content The document content - the byte array is stored directly,
	 * 			no defensive copies are made
	 */
	public Document(final String name, final byte[] content) {
		this.name = Preconditions.checkNotNull(name);
		this.content = Preconditions.checkNotNull(content);
		this.mediaType = DEFAULT_MEDIA_TYPE;
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
	 * The media type of the document
	 */
	public String getMediaType() {
		return mediaType;
	}
	
	/**
	 * Sets the media type of the document
	 */
	public void setMediaType(final String mediaType) {
		this.mediaType = Preconditions.checkNotNull(mediaType);
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
}