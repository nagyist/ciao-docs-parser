package uk.nhs.ciao.docs.parser;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Property converter which converts a given date format into the canonical
 * date format used by HL7
 */
public class DatePropertyConverter implements PropertyConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatePropertyConverter.class);
	
	/**
	 * Standard Date format used by HL7
	 */
	private static final DateTimeFormatter HL7_FORMAT = DateTimeFormat.forPattern("yyyyMMdd").withLocale(Locale.getDefault());
	
	private final DateTimeFormatter parseFormat;
	
	/**
	 * Constructs a new date property converter using the specified pattern as the input format
	 */
	public DatePropertyConverter(final String pattern) {
		parseFormat = DateTimeFormat.forPattern(pattern);
	}
	
	@Override
	public String convertProperty(final String name, final String value) {
		if (Strings.isNullOrEmpty(value)) {
			return value;
		}
		
		try {
			final DateTime date = parseFormat.parseDateTime(value);
			return HL7_FORMAT.print(date);
		} catch (Exception e) {
			LOGGER.debug("Unable to parse property name: {} value: {} - continuing with original value",
					name, value, e);
			return value;
		}
	}
}
