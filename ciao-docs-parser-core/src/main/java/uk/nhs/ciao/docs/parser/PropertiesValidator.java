package uk.nhs.ciao.docs.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
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
		for (final PropertiesValidation validation: validations) {
			validation.validate(properties);
		}
		
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
	 * @see RequireNonEmptyProperty
	 */
	public void requireNonEmptyProperty(final String propertyName) {
		addValidation(new RequireNonEmptyProperty(propertyName));
	}
	
	/**
	 * Checks that the extracted properties passes a validation test.
	 */
	interface PropertiesValidation {
		/**
		 * Tests that the specified properties passes the validation
		 * 
		 * @param properties The properties to validate
		 * @throws UnsupportedDocumentTypeException If the properties fails the validation
		 */
		void validate(Map<String, Object> properties) throws UnsupportedDocumentTypeException;
	}
	
	/**
	 * Checks that the extracted properties contains a property of the specified name
	 * and that the associated value is not empty.
	 */
	public static class RequireNonEmptyProperty implements PropertiesValidation {
		private final String propertyName;
		
		public RequireNonEmptyProperty(final String propertyName) {
			this.propertyName = Preconditions.checkNotNull(propertyName);
		}
		
		@Override
		public void validate(final Map<String, Object> properties) throws UnsupportedDocumentTypeException {
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
				throw new UnsupportedDocumentTypeException("'" + propertyName + "' must not be empty");
			}
		}
	}
}
