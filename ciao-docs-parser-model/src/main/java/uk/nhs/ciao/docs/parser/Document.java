package uk.nhs.ciao.docs.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Represents the binary content of a named document.
 * <p>
 * The content of the document is maintained in memory.
 * <p>
 * When serialising instances of the class, Jackson uses the JavaBean accessors
 * of this class to determine which JSON properties to include. During
 * unmashalling the annotated constructor of this class is used to determine
 * the JSON to Java properties mapping.
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
	 * Constructs a new document instance using the default media type
	 * 
	 * @param name The name of the document
	 * @param content The document content - the byte array is stored directly,
	 * 			no defensive copies are made
	 */
	public Document(final String name, final byte[] content) {
		this(name, content, DEFAULT_MEDIA_TYPE);
	}
	
	/**
	 * Constructs a new document instance
	 * 
	 * @param name The name of the document
	 * @param content The document content - the byte array is stored directly,
	 * 			no defensive copies are made
	 * @param mediaType The media type of the document
	 */
	@JsonCreator
	public Document(@JsonProperty("name") final String name,
			@JsonProperty("content") final byte[] content,
			@JsonProperty(value="mediaType", required=false) final String mediaType) {
		this.name = Preconditions.checkNotNull(name);
		this.content = Preconditions.checkNotNull(content);
		this.mediaType = Strings.isNullOrEmpty(mediaType) ? DEFAULT_MEDIA_TYPE : mediaType;
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
	 * Tests if the document is empty
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return content == null || content.length == 0;
	}
	
	/**
	 * The content of the document encoded using Base64
	 */
	@JsonIgnore
	public String getBase64Content() {
		return DatatypeConverter.printBase64Binary(content);
	}
	
	/**
	 * The content of the document as an input stream
	 */
	@JsonIgnore
	public InputStream getContentStream() {
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