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
	
	public MultiCauseIOException(final String message, final List<? extends Exception> causes) {
		super(message);
		
		if (causes.size() == 1) {
			initCause(causes.get(1));
			this.causes = new Exception[0];
		} else {
			this.causes = causes.toArray(new Exception[causes.size()]);
		}
	}
	
	/**
	 * The multiple causes associated with this exception
	 */
	public Exception[] getCauses() {
		return Arrays.copyOf(causes, causes.length);
	}
}
