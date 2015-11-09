package uk.nhs.ciao.docs.parser;

import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * SAX content handler to convert the content to a DOM and
 * optionally normalise any whitespace nodes
 */
public class SAXContentToDOMHandler extends DefaultHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SAXContentToDOMHandler.class);
	private static final char NON_BREAKING_SPACE = 160;
	
	private final DocumentBuilder documentBuilder;
	private final boolean whitespaceNormalisationEnabled;
	private final Deque<Element> elements;
	
	private Document document;

	/**
	 * Creates a new DOMBuilder backed by the specified document
	 * builder
	 */
	public SAXContentToDOMHandler(final DocumentBuilder documentBuilder,
			final boolean whitespaceNormalisationEnabled) {
		this.documentBuilder = Preconditions.checkNotNull(documentBuilder);
		this.whitespaceNormalisationEnabled = whitespaceNormalisationEnabled;
		this.elements = Lists.newLinkedList();
	}
	
	/**
	 * Clears the builder so that it can be re-used
	 */
	public void clear() {
		this.document = null;
		this.elements.clear();
	}
	
	/**
	 * Returns the constructed document
	 */
	public Document getDocument() {
		return document;
	}
	
	@Override
	public void startDocument() throws SAXException {
		LOGGER.trace("startDocument: ");
		
		elements.clear();
		try {
			document = documentBuilder.newDocument();
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		LOGGER.trace("endDocument: ");
		
		elements.clear();
		if (whitespaceNormalisationEnabled) {
			normaliseWhitespace();
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes atts) throws SAXException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("startElement: {}, {}", localName, toMap(atts));
		}
		
		final Element element = document.createElement(localName);
		if (elements.isEmpty()) {
			document.appendChild(element);				
		} else {
			elements.getLast().appendChild(element);
		}
		elements.add(element);
		
		for (final Entry<String, String> attribute: toMap(atts).entrySet()) {
			element.setAttribute(attribute.getKey(), attribute.getValue());
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		LOGGER.trace("endElement: {}", localName);
		elements.removeLast();	
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		final String string = toNormalizedString(ch, start, length);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("characters: {}", string);
		}
		
		elements.getLast().appendChild(document.createTextNode(string));
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		LOGGER.trace("ignorableWhitespace: {}", length);
		
		elements.getLast().appendChild(document.createTextNode(toNormalizedString(ch, start, length)));
	}
	
	private String toNormalizedString(final char[] ch, final int start, final int length) {
		// normalise non-breaking spaces to a standard space
		// standard Java regex patterns and string trim do not work with non-breaking spaces
		for (int index = start; index < length; index++) {
			if (ch[index] == NON_BREAKING_SPACE) {
				ch[index] = ' ';
			}
		}
		
		return new String(ch, start, length);
	}
	
	/**
	 * Normalises adjacent whitespace nodes into a single node and trims non-whitespace
	 * text nodes of any starting and trailing whitespace
	 */
	private void normaliseWhitespace() {			
		document.normalizeDocument();
		
		if (document.getDocumentElement() == null) {
			return;
		}
		
		final Queue<Node> queue = Lists.newLinkedList();
		queue.add(document.getDocumentElement());
		while (!queue.isEmpty()) {
			final Node node = queue.remove();
			final NodeList children = node.getChildNodes();
			for (int index = 0; index < children.getLength(); index++) {
				queue.add(children.item(index));
			}
			
			if (node.getNodeType() == Node.TEXT_NODE) {
				node.setTextContent(node.getTextContent().trim());
				if (node.getTextContent().isEmpty()) {
					node.getParentNode().removeChild(node);
				}
			}
		}
	}
	
	/**
	 * Converts the XML Attributes instance into a standard map
	 * of key/value pairs
	 */
	private Map<String, String> toMap(final Attributes atts) {
		final Map<String, String> values = Maps.newLinkedHashMap();
		
		for (int index = 0; index < atts.getLength(); index++) {
			values.put(atts.getLocalName(index), atts.getValue(index));
		}
		
		return values;
	}
}