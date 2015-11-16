package uk.nhs.ciao.docs.parser;

/**
 * Visits properties within a dynamic property structure
 */
public interface PropertyVisitor {
	/**
	 * Visits a property 
	 * 
	 * @param name The name of the property
	 * @param value The value of the property
	 */
	void onProperty(final PropertyName name, final Object value);
}
