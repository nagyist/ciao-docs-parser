package uk.nhs.ciao.docs.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Validates a set of properties against a configurable set of rules
 */
public class PropertiesValidator implements PropertiesExtractor<Map<String, Object>> {
	private List<PropertiesValidation> validations = Lists.newArrayList();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Checks the extracted properties against a series of validations. If a
	 * validation fails an {@link UnsupportedDocumentTypeException} is thrown.
	 * 
	 * @param properties The properties to validate
	 * @return The properties being validated
	 * @throws UnsupportedDocumentTypeException If the specified properties fails a validation
	 */
	@Override
	public Map<String, Object> extractProperties(final Map<String, Object> properties) throws UnsupportedDocumentTypeException {
		final ValidationResult result = new ValidationResult();
		for (final PropertiesValidation validation: validations) {
			validation.validate(properties, result);
		}
		
		result.assertIsValid();
		
		return properties;
	}
	
	/**
	 * The validation tests to run against extracted properties
	 */
	public List<PropertiesValidation> getValidations() {
		return validations;
	}
	
	/**
	 * Sets the validation tests to run against extracted properties
	 */
	public void setValidations(final List<PropertiesValidation> validations) {
		this.validations = Preconditions.checkNotNull(validations);
	}
	
	/**
	 * Registers a validation test to run against extracted properties
	 * 
	 * @param validation The validation to add
	 */
	public void addValidation(final PropertiesValidation validation) {
		if (validation != null) {
			validations.add(validation);
		}
	}

	/**
	 * Adds a validation to check that the extracted properties contains
	 * a named property and that the associated value is not empty.
	 * 
	 * @param propertyName The name of the required property
	 * @see NonEmptyPropertyValidation
	 */
	public void requireNonEmptyProperty(final String propertyName) {
		addValidation(new NonEmptyPropertyValidation(propertyName));
	}
	
	/**
	 * Adds a validation to check that the extracted properties contains
	 * a named property and that the associated value matches the specified date pattern
	 * 
	 * @param propertyName The name of the required property
	 * @see DatePropertyValidation
	 */
	public void requireDateProperty(final String propertyName, final String pattern) {
		final boolean required = true;
		addValidation(new DatePropertyValidation(propertyName, pattern, required));
	}
	
	/**
	 * Adds a validation to check that if the extracted properties contains
	 * a named property then the associated value matches the specified date pattern
	 * 
	 * @param propertyName The name of the optional property
	 * @see DatePropertyValidation
	 */
	public void optionalDateProperty(final String propertyName, final String pattern) {
		final boolean required = false;
		addValidation(new DatePropertyValidation(propertyName, pattern, required));
	}
	
	/**
	 * Checks that the extracted properties passes a validation test.
	 */
	interface PropertiesValidation {
		/**
		 * Tests that the specified properties passes the validation
		 * 
		 * @param properties The properties to validate
		 * @param result The result of the validation
		 */
		void validate(Map<String, Object> properties, ValidationResult result);
	}
	
	public static class ValidationResult {
		private List<Object> errors = Lists.newArrayList();

		public void addError(final Object error) {
			if (error != null) {
				errors.add(error);
			}
		}
		
		public void addPropertyValidationError(final String propertyName, final String description) {
			addError(new PropertyValidationError(propertyName, description));
		}
		
		public boolean isValid() {
			return errors.isEmpty();
		}
		
		/**
		 * Checks that the extracted properties passed validation
		 * 
		 * @throws UnsupportedDocumentTypeException If the extracted properties failed validation
		 */
		public void assertIsValid() throws UnsupportedDocumentTypeException {
			if (isValid()) {
				return;
			}
			
			final String message = Joiner.on('\n').join(errors);
			throw new UnsupportedDocumentTypeException(message);
		}
	}
	
	public static class PropertyValidationError {
		private final String propertyName;
		private final String description;
		
		public PropertyValidationError(final String propertyName, final String description) {
			this.propertyName = Preconditions.checkNotNull(propertyName);
			this.description = Strings.nullToEmpty(description);
		}
		
		@Override
		public String toString() {
			return "'" + propertyName + "' " + description;
		}
	}
	
	/**
	 * Checks that the extracted properties contains a property of the specified name
	 * and that the associated value is not empty.
	 */
	public static class NonEmptyPropertyValidation implements PropertiesValidation {
		private final String propertyName;
		
		public NonEmptyPropertyValidation(final String propertyName) {
			this.propertyName = Preconditions.checkNotNull(propertyName);
		}
		
		@Override
		public void validate(final Map<String, Object> properties, final ValidationResult result) {
			final Object value = properties.get(propertyName);
			
			final boolean isEmpty;
			if (value instanceof CharSequence) {
				isEmpty = ((CharSequence) value).length() == 0;
			} else if (value instanceof Collection<?>) {
				isEmpty = ((Collection<?>) value).isEmpty();
			} else {
				isEmpty = value == null;
			}
			
			if (isEmpty) {
				result.addPropertyValidationError(propertyName, "must not be empty");
			}
		}
	}
	
	public static class DatePropertyValidation implements PropertiesValidation {
		private final String propertyName;
		private final DateTimeFormatter formatter;
		private final boolean required;
		
		public DatePropertyValidation(final String propertyName, final String pattern, final boolean required) {
			this(propertyName, DateTimeFormat.forPattern(pattern), required);
		}
		
		public DatePropertyValidation(final String propertyName, final DateTimeFormatter formatter, final boolean required) {
			this.propertyName = Preconditions.checkNotNull(propertyName);
			this.formatter = Preconditions.checkNotNull(formatter);
			this.required = required;
		}
		
		@Override
		public void validate(final Map<String, Object> properties, final ValidationResult result) {
			final Object value = properties.get(propertyName);
			if (value == null) {
				if (required) {
					result.addPropertyValidationError(propertyName, "must be specified");
				}
				return;
			}
			
			try {
				formatter.parseMillis(value.toString());
			} catch (IllegalArgumentException e) {
				result.addPropertyValidationError(propertyName, "is not a valid date: " + value);
			}
		}
	}
}
