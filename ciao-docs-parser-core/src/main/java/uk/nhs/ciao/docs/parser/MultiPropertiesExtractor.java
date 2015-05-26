package uk.nhs.ciao.docs.parser;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class MultiPropertiesExtractor<T> implements PropertiesExtractor<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiPropertiesExtractor.class);
	private final Set<PropertiesExtractor<? super T>> extractors;
	
	public MultiPropertiesExtractor() {
		this.extractors = Sets.newLinkedHashSet();
	}
	
	public MultiPropertiesExtractor(final PropertiesExtractor<? super T>... extractors) {
		this();
		addExtractors(extractors);
	}
	
	public void addExtractor(final PropertiesExtractor<? super T> extractor) {
		if (extractor != null) {
			extractors.add(extractor);
		}
	}
	
	public void addExtractors(final Iterable<? extends PropertiesExtractor<? super T>> extractors) {
		for (final PropertiesExtractor<? super T> extractor: extractors) {
			addExtractor(extractor);
		}
	}
	
	public void addExtractors(final PropertiesExtractor<? super T>... extractors) {
		for (final PropertiesExtractor<? super T> extractor: extractors) {
			addExtractor(extractor);
		}
	}
	
	@Override
	public Map<String, Object> extractProperties(final T document) throws UnsupportedDocumentTypeException {
		if (extractors.isEmpty()) {
			throw new UnsupportedDocumentTypeException("No property extractors are available");
		} else if (extractors.size() == 1) {
			final PropertiesExtractor<? super T> extractor = extractors.iterator().next();
			return extractor.extractProperties(document);
		}
		
		for (final PropertiesExtractor<? super T> extractor: extractors) {
			try {
				return extractor.extractProperties(document);
			} catch (final UnsupportedDocumentTypeException e) {
				LOGGER.trace("Property extractor {} does not support the type of document", extractor, e);
			}
		}
		
		throw new UnsupportedDocumentTypeException("No property extractors support the type of document");
	}
}
