package uk.nhs.ciao.docs.parser.transformer;

import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.format.DateTimeFormatter;

import uk.nhs.ciao.docs.parser.PropertyName;
import uk.nhs.ciao.docs.parser.PropertyVisitor;

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
		final Map<PropertyName, Long> matchingProperties = findMatchingProperties(source);
		
		for (final Entry<PropertyName, Long> entry: matchingProperties.entrySet()) {
			final String newValue = toFormat.print(entry.getValue());
			new PropertyMutator(entry.getKey()).set(recorder, entry.getKey(), destination, newValue);
		}
	}
	
	private Map<PropertyName, Long> findMatchingProperties(final Map<String, Object> container) {
		final Map<PropertyName, Long> matchingProperties = Maps.newLinkedHashMap();
		
		PropertyName.getRoot().accept(container, new PropertyVisitor() {
			@Override
			public void onProperty(final PropertyName name, final Object candidate) {
				if (candidate instanceof CharSequence) {
					try {
						final long millis = fromFormat.parseMillis(candidate.toString());
						matchingProperties.put(name, millis);
					} catch (IllegalArgumentException e) {
						// Not a date
					}
				}
			}
		});

		return matchingProperties;
	}
}
