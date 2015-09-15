package uk.nhs.ciao.docs.parser.transformer;

import java.util.Map;

/**
 * Transforms properties from the source map into the destinations map
 */
public interface PropertiesTransformation {
	/**
	 * Transforms properties from the source map into the destinations map
	 * <p>
	 * The source and destination map may be the same instance.
	 */
	void apply(final Map<String, Object> source, final MappedProperties destination);
}