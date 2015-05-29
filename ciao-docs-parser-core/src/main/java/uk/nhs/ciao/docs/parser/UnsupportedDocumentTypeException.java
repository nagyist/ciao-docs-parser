package uk.nhs.ciao.docs.parser;

/**
 * Indicates that the type of a document does not match
 * the supported types.
 */
public class UnsupportedDocumentTypeException extends Exception {
	private static final long serialVersionUID = -620912451444936379L;

	/**
	 * Constructs a new exception with no specified message or cause
	 */
	public UnsupportedDocumentTypeException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified message and cause
	 * 
	 * @param message The detail message
	 * @param cause The initial cause of the exception
	 */
	public UnsupportedDocumentTypeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified message
	 * 
	 * @param The detail message
	 */
	public UnsupportedDocumentTypeException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified cause
	 * 
	 * @param cause The initial cause of the exception
	 */
	public UnsupportedDocumentTypeException(final Throwable cause) {
		super(cause);
	}
}
