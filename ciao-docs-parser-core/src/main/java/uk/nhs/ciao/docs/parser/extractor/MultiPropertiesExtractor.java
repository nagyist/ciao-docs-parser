package uk.nhs.ciao.docs.parser.extractor;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.collect.Sets;

/**
 * An extractor which extracts properties by calling multiple delegate extractors sequentially until one
 * is found which supports the type of document.
 * 
 * @param <T> The document representation that properties can be extracted from
 */
public class MultiPropertiesExtractor<T> implements PropertiesExtractor<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiPropertiesExtractor.class);
	private final Set<PropertiesExtractor<? super T>> extractors;
	
	/**
	 * Constructs a new empty multi-property extractor. Delegate extractors can be added later
	 * after construction.
	 */
	public MultiPropertiesExtractor() {
		this.extractors = Sets.newLinkedHashSet();
	}
	
	/**
	 * Constructs a new multi-property extractor which delegates to the specified extractors
	 * when attempting to extract properties from documents. Additional delegate extractors
	 * can be added later after construction.
	 * <p>
	 * Delegate extractors are attempted in registration order.
	 * 
	 * @param parsers The delegate extractors to register
	 */
	public MultiPropertiesExtractor(final PropertiesExtractor<? super T>... extractors) {
		this();
		addExtractors(extractors);
	}
	
	/**
	 * Registers the specified extractor as a delegate to use when attempting to extract properties
	 * from documents.
	 * <p>
	 * Delegate extractors are attempted in registration order
	 * 
	 * @param extractor The delegate extractor to register
	 */
	public void addExtractor(final PropertiesExtractor<? super T> extractor) {
		if (extractor != null) {
			extractors.add(extractor);
		}
	}
	
	/**
	 * Registers the specified extractors as delegates to use when attempting to extract properties
	 * from documents.
	 * <p>
	 * Delegate extractors are attempted in registration order
	 * 
	 * @param extractors The delegate extractors to register
	 */
	public void addExtractors(final Iterable<? extends PropertiesExtractor<? super T>> extractors) {
		for (final PropertiesExtractor<? super T> extractor: extractors) {
			addExtractor(extractor);
		}
	}
	
	/**
	 * Registers the specified extractors as delegates to use when attempting to extract properties
	 * from documents.
	 * <p>
	 * Delegate extractors are attempted in registration order
	 * 
	 * @param extractors The delegate extractor to register
	 */
	public void addExtractors(final PropertiesExtractor<? super T>... extractors) {
		for (final PropertiesExtractor<? super T> extractor: extractors) {
			addExtractor(extractor);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegate extractors are attempted in registration order until once successfully extracts properties from
	 * the document.
	 * 
	 * @throws UnsupportedDocumentTypeException If no delegate extractors are registered, or if all delegate
	 * 			extractors fail to extract properties from the document
	 */
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
