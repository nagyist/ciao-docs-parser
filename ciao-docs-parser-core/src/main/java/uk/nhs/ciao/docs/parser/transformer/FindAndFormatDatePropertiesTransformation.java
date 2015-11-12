package uk.nhs.ciao.docs.parser.transformer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Finds all string properties matching the specified date pattern and reformats
 * them to use the specified output pattern
 */
public class FindAndFormatDatePropertiesTransformation implements PropertiesTransformation {
	private final DateTimeFormatter fromFormat;
	private final DateTimeFormatter toFormat;
	
	public FindAndFormatDatePropertiesTransformation(final DateTimeFormatter fromFormat, final DateTimeFormatter toFormat) {
		this.fromFormat = Preconditions.checkNotNull(fromFormat);
		this.toFormat = Preconditions.checkNotNull(toFormat);
	}
	
	@Override
	public void apply(final TransformationRecorder recorder, final Map<String, Object> source, final Map<String, Object> destination) {
		final Map<String, Long> matchingProperties = findMatchingProperties(source);
		
		for (final Entry<String, Long> entry: matchingProperties.entrySet()) {
			final String newValue = toFormat.print(entry.getValue());
			new PropertyMutator(entry.getKey()).set(recorder, entry.getKey(), destination, newValue);
		}
	}
	
	private Map<String, Long> findMatchingProperties(final Map<String, Object> container) {
		final Map<String, Long> matchingProperties = Maps.newLinkedHashMap();
		final StringBuilder prefix = new StringBuilder();
		findMatchingProperties(prefix, container, matchingProperties);
		return matchingProperties;
	}
	
	private void findMatchingProperties(final StringBuilder prefix, final Map<String, Object> container, final Map<String, Long> matchingProperties) {
		final int prefixLength = prefix.length();
		for (final Entry<String, Object> entry: container.entrySet()) {
			if (prefixLength > 0) {
				prefix.append('.');
			}
			prefix.append(entry.getKey());
			findMatchingProperties(prefix, entry.getValue(), matchingProperties);
			prefix.setLength(prefixLength);
		}
	}
	
	private void findMatchingProperties(final StringBuilder prefix, final List<?> container, final Map<String, Long> matchingProperties) {
		final int prefixLength = prefix.length();
		for (int index = 0; index < container.size(); index++) {
			prefix.append('[').append(index).append(']');
			findMatchingProperties(prefix, container.get(index), matchingProperties);
			prefix.setLength(prefixLength);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void findMatchingProperties(final StringBuilder prefix, final Object candidate, final Map<String, Long> matchingProperties) {
		if (candidate instanceof Map) {
			findMatchingProperties(prefix, (Map<String, Object>)candidate, matchingProperties);
		} else if (candidate instanceof List) {
			findMatchingProperties(prefix, (List<?>)candidate, matchingProperties);
		} else if (candidate instanceof CharSequence) {
			try {
				final long millis = fromFormat.parseMillis(candidate.toString());
				matchingProperties.put(prefix.toString(), millis);
			} catch (IllegalArgumentException e) {
				// Not a date
			}
		}
	}
}
