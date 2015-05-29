package uk.nhs.ciao.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * IOException with multiple causes.
 * <p>
 * From Java 7 onwards: prefer {@link IOException#addSuppressed(Throwable)}
 */
public class MultiCauseIOException extends IOException {
	private static final long serialVersionUID = -8167162583826676106L;
	
	private final Exception[] causes;
	
	/**
	 * Constructs a new exception with the specified detail message, and list of initial causes.
	 * <p>
	 * If <code>causes</code> contains a single exception, it is registered as the main cause (@see
	 * {@link #getCause()}.
	 * <p>
	 * All exceptions in the <code>causes</code> list can be retrieved later via {@link #getCauses()}
	 * 
	 * @param message The detail message
	 * @param causes The list of initial causes
	 */
	public MultiCauseIOException(final String message, final List<? extends Exception> causes) {
		super(message);
		
		if (causes.size() == 1) {
			initCause(causes.get(0));
		}
		
		this.causes = causes.toArray(new Exception[causes.size()]);
	}
	
	/**
	 * The multiple causes associated with this exception
	 * <p>
	 * A defensive copy of the causes is returned - any modifications on the returned array
	 * will not be reflected in subsequent calls to this method.
	 */
	public Exception[] getCauses() {
		// defensive copy
		return Arrays.copyOf(causes, causes.length);
	}
}
