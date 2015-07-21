package uk.nhs.ciao.docs.parser;

/**
 * Headers names associated with the processing of a ParsedDocument
 */
public final class HeaderNames {
	
	/**
	 * The unique ID associated with the processing of a parsed document
	 * <p>
	 * This is a synonym for Camel's <code>Exchange.CORRELATION_ID</code> header
	 */
	public static final String CORRELATION_ID = "CamelCorrelationId";
	
	/**
	 * The file name of the source document that was parsed
	 */
	public static final String SOURCE_FILE_NAME = "ciaoSourceFileName";
	
	/**
	 * The time processing was started expressed as a Unix timestamp (i.e.
	 * milliseconds since 1970)
	 */
	public static final String TIMESTAMP = "ciaoTimestamp";
	
	/**
	 * The folder where the parsed document should be moved to if processing fails
	 */
	public static final String ERROR_FOLDER = "ciaoErrorFolder";
	
	/**
	 * The folder where the parsed document should be moved to while processing is
	 * in-progress
	 */
	public static final String IN_PROGRESS_FOLDER = "ciaoInProgressFolder";
	
	/**
	 * The folder where the parsed document should be moved to once it has completed
	 */
	public static final String COMPLETED_FOLDER = "ciaoCompletedFolder";
	
	private HeaderNames() {
		// Suppress default constructor
	}
}
