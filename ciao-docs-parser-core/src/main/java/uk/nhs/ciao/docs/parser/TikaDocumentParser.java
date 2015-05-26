package uk.nhs.ciao.docs.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A {@link DischargeSummaryReader} backed by Apache Tika.
 * <p>
 * The documents are first parsed by Tika (using the configured parser) and
 * converted to an XHTML DOM representation. Next a map of key/value properties
 * are extracted from the dom and returned.
 * <p>
 * Whitespace text nodes are normalized in the intermediate document
 */
public class TikaDocumentParser implements DocumentParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(TikaDocumentParser.class);
	private final Parser parser;
	private final PropertiesExtractor<Document> propertiesExtractor;
	private final DocumentBuilder documentBuilder;
	
	public TikaDocumentParser(final Parser parser, final PropertiesExtractor<Document> propertiesExtractor)
			throws ParserConfigurationException {
		this.parser = Preconditions.checkNotNull(parser);
		this.propertiesExtractor = Preconditions.checkNotNull(propertiesExtractor);
		this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}
	
	@Override
	public Map<String, Object> parseDocument(final InputStream in)
			throws UnsupportedDocumentTypeException, IOException {
		final Document document = convertToDom(in);
		return propertiesExtractor.extractProperties(document);
	}

	private Document convertToDom(final InputStream in) throws IOException {
		final DocumentContentHandler handler = new DocumentContentHandler();
		
		try {
			parser.parse(in, handler, handler.metadata, handler.context);
			return handler.document;
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (TikaException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * SAX content handler to convert the content to a DOM and
	 * normalize any whitespace nodes
	 *
	 */
	private class DocumentContentHandler implements ContentHandler {
		final Metadata metadata = new Metadata();
		final ParseContext context = new ParseContext();
		
		private org.w3c.dom.Document document;
		private Deque<Element> elements;

		@Override
		public void setDocumentLocator(final Locator locator) {
			LOGGER.trace("setDocumentLocator: {}", locator);
		}

		@Override
		public void startDocument() throws SAXException {
			LOGGER.trace("startDocument: ");
			
			try {
				document = documentBuilder.newDocument();
				elements = Lists.newLinkedList();
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}

		@Override
		public void endDocument() throws SAXException {
			LOGGER.trace("endDocument: ");
			
			elements.clear();
			
			document.normalizeDocument();
			
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

		@Override
		public void startPrefixMapping(final String prefix, final String uri)
				throws SAXException {
			LOGGER.trace("startPrefixMapping: {}, {}", prefix, uri);
		}

		@Override
		public void endPrefixMapping(final String prefix) throws SAXException {
			LOGGER.trace("endPrefixMapping: {}", prefix);
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes atts) throws SAXException {
			final Map<String, String> attributeMap = toMap(atts);
			LOGGER.trace("startElement: {}, {}", localName, attributeMap);
			
			final Element element = document.createElement(localName);
			if (elements.isEmpty()) {
				document.appendChild(element);				
			} else {
				elements.getLast().appendChild(element);
			}
			elements.add(element);
			
			for (final Entry<String, String> attribute: attributeMap.entrySet()) {
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
			LOGGER.trace("characters: {}", new String(ch, start, length));
			
			elements.getLast().appendChild(document.createTextNode(new String(ch, start, length)));
		}

		@Override
		public void ignorableWhitespace(final char[] ch, final int start, final int length)
				throws SAXException {
			LOGGER.trace("ignorableWhitespace: {}", length);
			
			elements.getLast().appendChild(document.createTextNode(new String(ch, start, length)));
		}

		@Override
		public void processingInstruction(final String target, final String data)
				throws SAXException {
			LOGGER.trace("processingInstruction: {}, {}", target, data);
		}

		@Override
		public void skippedEntity(final String name) throws SAXException {
			LOGGER.trace("skippedEntity: {}", name);
		}
		
		private Map<String, String> toMap(final Attributes atts) {
			final Map<String, String> values = Maps.newLinkedHashMap();
			
			for (int index = 0; index < atts.getLength(); index++) {
				values.put(atts.getLocalName(index), atts.getValue(index));
			}
			
			return values;
		}
	}
}
