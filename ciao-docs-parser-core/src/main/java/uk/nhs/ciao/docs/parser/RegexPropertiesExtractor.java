package uk.nhs.ciao.docs.parser;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * {@link PropertiesExtractor} which uses regular expressions to extract properties
 * from a DOM.
 * <p>
 * The extraction of each property/regex pair is handled by instances of {@link RegexPropertyFinder}.
 */
public class RegexPropertiesExtractor implements PropertiesExtractor<Document> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegexPropertiesExtractor.class);
	
	private final Set<RegexPropertyFinder> propertyFinders;
	private String fromNodeText;
	private String toNodeText;
	
	/**
	 * Creates a new (empty) extractor instance
	 */
	public RegexPropertiesExtractor() {
		propertyFinders = Sets.newLinkedHashSet();
	}
	
	/**
	 * Creates a new extractor instance backed by the specified property finders
	 */
	public RegexPropertiesExtractor(final RegexPropertyFinder... propertyFinders) {
		this();
		
		addPropertyFinders(propertyFinders);
	}
	
	/**
	 * Adds the specified property finder to this extractor
	 */
	public final void addPropertyFinder(final RegexPropertyFinder propertyFinder) {
		if (propertyFinder != null) {
			propertyFinders.add(propertyFinder);
		}
	}
	
	/**
	 * Adds the specified property finders to this extractor
	 */
	public final void addPropertyFinders(final RegexPropertyFinder... propertyFinders) {
		for (final RegexPropertyFinder propertyFinder: propertyFinders) {
			addPropertyFinder(propertyFinder);
		}
	}
	
	/**
	 * Adds the specified property finders to this extractor
	 */
	public final void addPropertyFinders(final Iterable<? extends RegexPropertyFinder> propertyFinders) {
		for (final RegexPropertyFinder propertyFinder: propertyFinders) {
			addPropertyFinder(propertyFinder);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The text content to match is first extracted from the document, then properties are
	 * extracted from the text by calling each registered property finder.
	 * 
	 * @throws UnsupportedDocumentTypeException If no matching properties could be found in the document
	 * @see #getTextContent(Document)
	 */
	@Override
	public Map<String, Object> extractProperties(final Document document)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		final String textContent = getTextContent(document);
		
		for (final RegexPropertyFinder propertyFinder: propertyFinders) {
			final String value = propertyFinder.findValue(textContent);
			if (!value.isEmpty()) {
				properties.put(propertyFinder.getName(), value);
			}
		}
		
		if (properties.isEmpty()) {
			throw new UnsupportedDocumentTypeException("No matching properties could be found");
		}
		LOGGER.trace("properties: {}", properties);
		
		return properties;
	}
	
	/**
	 * Enables text filtering of the document before property extraction.
	 * <p>
	 * This can be useful if property matching has to occur against a section
	 * of the document which always varies - but is terminated instead by the
	 * end of an XML element. Default extraction flattens the whole document
	 * removing the start/end of XML documents, with the text filter enabled
	 * just a sub-set of document text is searched for matching patterns.
	 * 
	 * @param fromNodeText The start text of the initial node to match or null
	 * @param toNodeText The start text of the final node to match or null
	 */
	public void setTextFilter(final String fromNodeText, final String toNodeText) {
		this.fromNodeText = fromNodeText;
		this.toNodeText = toNodeText;
	}
	
	/**
	 * Returns the text content of the document, possibly filtered if from/to node
	 * filtering has been enabled.
	 * <p>
	 * Only text contained in 'p' tags is used when determining the content to return.
	 * 
	 * @see #setTextFilter(String, String)
	 */
	protected String getTextContent(final Document document) {
		if (Strings.isNullOrEmpty(fromNodeText) || Strings.isNullOrEmpty(toNodeText)) {
			return document.getDocumentElement().getTextContent();
		}
		
		final StringBuilder text = new StringBuilder();
		final NodeList nodes = document.getElementsByTagName("p");
		
		for (int index = 0; index < nodes.getLength(); index++) {		
			final String nodeText = nodes.item(index).getTextContent();
			if (nodeText.trim().startsWith(fromNodeText)) {
				text.append(nodeText);
			} else if (text.length() > 0) {
				text.append(nodeText);
				if (nodeText.trim().startsWith(toNodeText)) {
					break;
				}
			}
		}
		
		return text.toString();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("propertyFinders", propertyFinders)
				.toString();
	}
}
