package uk.nhs.ciao.docs.parser;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Extracts key/value properties from a chain of delegate extractors
 *
 * @param <T> The document representation that properties can be extracted from
 */
public class PropertiesExtractorChain<T> implements PropertiesExtractor<T> {
	private final PropertiesExtractor<T> firstExtractor;
	private final List<PropertiesExtractor<Map<String, Object>>> additionalExtractors = Lists.newArrayList();
	
	public PropertiesExtractorChain(final PropertiesExtractor<T> firstExtractor) {
		this.firstExtractor = Preconditions.checkNotNull(firstExtractor);
	}
	
	public void addExtractor(final PropertiesExtractor<Map<String, Object>> additionalExtractor) {
		if (additionalExtractor != null) {
			additionalExtractors.add(additionalExtractor);
		}
	}
	
	@Override
	public Map<String, Object> extractProperties(final T document) throws UnsupportedDocumentTypeException {
		Map<String, Object> properties = firstExtractor.extractProperties(document);
		for (final PropertiesExtractor<Map<String, Object>> extractor: additionalExtractors) {
			properties = extractor.extractProperties(properties);
		}
		return properties;
	}
}
