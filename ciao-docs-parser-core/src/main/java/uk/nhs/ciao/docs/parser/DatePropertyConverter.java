package uk.nhs.ciao.docs.parser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
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
	private static final DateFormatThreadLocal HL7_FORMAT = new DateFormatThreadLocal("yyyyMMdd");
	
	private final DateFormatThreadLocal parseFormat;
	
	/**
	 * Constructs a new date property converter using the specified pattern as the input format
	 */
	public DatePropertyConverter(final String pattern) {
		parseFormat = new DateFormatThreadLocal(pattern);
	}
	
	@Override
	public String convertProperty(final String name, final String value) {
		if (Strings.isNullOrEmpty(value)) {
			return value;
		}
		
		try {
			final Date date = parseFormat.get().parse(value);
			return HL7_FORMAT.get().format(date);
		} catch (Exception e) {
			LOGGER.debug("Unable to parse property name: {} value: {} - continuing with original value",
					name, value, e);
			return value;
		}
	}
	
	/**
	 * ThreadLocal to contain simple date format instances (SimpleDateFormat is NOT thread-safe so
	 * synchronisation or thread-confinement is required)
	 */
	private static class DateFormatThreadLocal extends ThreadLocal<DateFormat> {
		private final String pattern;
		
		public DateFormatThreadLocal(final String pattern) {
			this.pattern = Preconditions.checkNotNull(pattern);
		}
		
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(pattern);
		}
	}
}
