package uk.nhs.ciao.docs.parser;

/**
 * Indicates that the type of a document does not match
 * the supported types.
 */
public class UnsupportedDocumentTypeException extends Exception {
	private static final long serialVersionUID = -620912451444936379L;

	public UnsupportedDocumentTypeException() {
		super();
	}

	public UnsupportedDocumentTypeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnsupportedDocumentTypeException(final String message) {
		super(message);
	}

	public UnsupportedDocumentTypeException(final Throwable cause) {
		super(cause);
	}
}
