package uk.nhs.ciao.docs.parser;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class RegexPropertiesExtractor implements PropertiesExtractor<Document> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegexPropertiesExtractor.class);
	
	private final Set<RegexPropertyFinder> propertyFinders;
	private String fromNodeText;
	private String toNodeText;
	
	public RegexPropertiesExtractor() {
		propertyFinders = Sets.newLinkedHashSet();
	}
	
	public RegexPropertiesExtractor(final RegexPropertyFinder... propertyFinders) throws ParserConfigurationException {
		this();
		
		addPropertyFinders(propertyFinders);
	}
	
	public final void addPropertyFinder(final RegexPropertyFinder propertyFinder) {
		if (propertyFinder != null) {
			propertyFinders.add(propertyFinder);
		}
	}
	
	public final void addPropertyFinders(final RegexPropertyFinder... propertyFinders) {
		for (final RegexPropertyFinder propertyFinder: propertyFinders) {
			addPropertyFinder(propertyFinder);
		}
	}
	
	public final void addPropertyFinders(final Iterable<? extends RegexPropertyFinder> propertyFinders) {
		for (final RegexPropertyFinder propertyFinder: propertyFinders) {
			addPropertyFinder(propertyFinder);
		}
	}
	
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
	 * filtering has been enabled
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
	
	public static RegexPropertyFinderBuilder propertyFinder(final String name) {
		return new RegexPropertyFinderBuilder(name);
	}
	
	/**
	 * Defines a document property and an associated regular
	 * expression capable of finding the property in a stream of text
	 */
	public static class RegexPropertyFinder {
		private final String name;
		private final Pattern pattern;
		
		public RegexPropertyFinder(final String name, final Pattern pattern) {
			this.name = Preconditions.checkNotNull(name);
			this.pattern = Preconditions.checkNotNull(pattern);
		}
		
		public String getName() {
			return name;
		}
		
		/**
		 * Attempts to find the property value in the specified text
		 * 
		 * @param text The text to search
		 * @return The associated value if one could be found, or the empty string otherwise.
		 */
		public String findValue(final String text) {
			final Matcher matcher = pattern.matcher(text);
			return matcher.find() ? matcher.group(1).trim() : "";
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("name", name)
					.add("pattern", pattern)
					.toString();
		}
	}
	
	public static class RegexPropertyFinderBuilder {
		private final String name;
		private String startLiteral;
		private String endLiteral;

		private RegexPropertyFinderBuilder(final String name) {
			this.name = Preconditions.checkNotNull(name);
			this.startLiteral = name;
		}
		
		public RegexPropertyFinderBuilder from(final String startLiteral) {
			this.startLiteral = Preconditions.checkNotNull(startLiteral);
			return this;
		}
		
		public RegexPropertyFinderBuilder to(final String endLiteral) {
			this.endLiteral = endLiteral;
			return this;
		}
		
		// bean setter for spring
		public void setStartLiteral(final String startLiteral) {
			this.startLiteral = startLiteral;
		}
		
		// bean setter for spring
		public void setEndLiteral(final String endLiteral) {
			this.endLiteral = endLiteral;
		}
		
		public RegexPropertyFinder build() {
			final String suffix = endLiteral == null ? "" :
				Pattern.quote(endLiteral);
			final Pattern pattern = Pattern.compile(Pattern.quote(startLiteral) +
					"\\s*:\\s*+(.*)\\s*+" + suffix);
			return new RegexPropertyFinder(name, pattern);
		}
	}
}
