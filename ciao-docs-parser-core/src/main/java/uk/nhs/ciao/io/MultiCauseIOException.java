package uk.nhs.ciao.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * IOException with multiple causes.
 * <p>
 * From Java 7 onwards: prefer {@link IOException#addSuppressed(Throwable)}
 */
public class MultiCauseIOException extends IOException {
	private static final long serialVersionUID = -8167162583826676106L;
	
	private final List<Exception> causes;
	public MultiCauseIOException(final String message, final List<? extends Exception> causes) {
		super(message);
		
		if (causes.size() == 1) {
			initCause(causes.get(1));
			this.causes = Collections.emptyList();
		} else {
			this.causes = Lists.newArrayList(causes);
		}
	}
	
	public List<Exception> getCauses() {
		return Collections.unmodifiableList(causes);
	}
}
