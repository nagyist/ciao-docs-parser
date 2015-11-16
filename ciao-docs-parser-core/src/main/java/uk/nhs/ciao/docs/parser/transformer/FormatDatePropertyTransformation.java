package uk.nhs.ciao.docs.parser.transformer;

import java.util.Map;

import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Parses and reformats a date property
 */
public class FormatDatePropertyTransformation implements PropertiesTransformation {
	private static final Logger LOGGER = LoggerFactory.getLogger(FormatDatePropertyTransformation.class);
	
	private final PropertyName from;
	private final PropertyMutator to;
	private final DateTimeFormatter fromFormat;
	private final DateTimeFormatter toFormat;
	private final boolean retainOriginal;
	
	public FormatDatePropertyTransformation(final PropertyName from, final DateTimeFormatter fromFormat,
			final PropertyMutator to, final DateTimeFormatter toFormat) {
		this(from, fromFormat, to, toFormat, true);
	}
	
	public FormatDatePropertyTransformation(final PropertyName from, final DateTimeFormatter fromFormat,
			final PropertyMutator to, final DateTimeFormatter toFormat, final boolean retainOriginal) {
		this.from = Preconditions.checkNotNull(from);
		this.fromFormat = Preconditions.checkNotNull(fromFormat);
		this.to = Preconditions.checkNotNull(to);
		this.toFormat = Preconditions.checkNotNull(toFormat);
		this.retainOriginal = retainOriginal;
	}
	
	@Override
	public void apply(final TransformationRecorder recorder, final Map<String, Object> source,
			final Map<String, Object> destination) {
		final Object originalValue = from.get(source);
		final String inputValue = originalValue == null ? null : originalValue.toString().trim();
		if (Strings.isNullOrEmpty(inputValue)) {
			return;
		}
		
		try {
			final long millis = fromFormat.parseMillis(inputValue);
			final String outputValue = toFormat.print(millis);
			
			if (!retainOriginal) {
				from.remove(source);
			}
			
			to.set(recorder, from, destination, outputValue);
		} catch (IllegalArgumentException e) {
			LOGGER.debug("Unable to transform date property - the value does not match the expected pattern", e);
		}
	}
}
