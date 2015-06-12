package uk.nhs.ciao.docs.parser;

/**
 * Converts the value of a named property.
 * <p>
 * A converter instance be used to convert the value to a canonical form - e.g. an incoming date
 * value of '1 December 2014' could be converted to '20141201'.
 */
public interface PropertyConverter {
	String convertProperty(final String name, final String value);
}
