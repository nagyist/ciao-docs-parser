package uk.nhs.ciao.docs.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A {@link DocumentParser} backed by Apache Tika.
 * <p>
 * The documents are first parsed by Tika (using the configured parser) and
 * converted to an XHTML DOM representation. Next a map of key/value properties
 * are extracted from the DOM and returned.
 * <p>
 * Whitespace text nodes are normalised in the intermediate document
 */
public class TikaDocumentParser implements DocumentParser {
	private final Parser parser;
	private final PropertiesExtractor<Document> propertiesExtractor;
	private final SAXContentToDOMHandler handler;
	
	/**
	 * Creates a new document parser backed by the specified Tika parser and
	 * properties extractor.
	 */
	public TikaDocumentParser(final Parser parser, final PropertiesExtractor<Document> propertiesExtractor)
			throws ParserConfigurationException {
		this.parser = Preconditions.checkNotNull(parser);
		this.propertiesExtractor = Preconditions.checkNotNull(propertiesExtractor);
		this.handler = createHandler();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * First Tika is used to interpret the input stream, then the configured property
	 * extractor is used to find properties with the interpreted document text. Finally,
	 * the extracted properties are enriched by including additional metadata detected
	 * by Tika.
	 */
	@Override
	public Map<String, Object> parseDocument(final InputStream in)
			throws UnsupportedDocumentTypeException, IOException {
		final Document document = parseToDom(in);
		final Map<String, Object> properties = propertiesExtractor.extractProperties(document);		
		addTikaMetadataProperties(document, properties);		
		return properties;
	}

	/**
	 * Parses the input document via Tika, converting the output XHTML into a DOM
	 * representation
	 */
	private Document parseToDom(final InputStream in) throws IOException {
		try {
			final Metadata metadata = new Metadata();
			final ParseContext context = new ParseContext();
			parser.parse(in, handler, metadata, context);
			
			return handler.getDocument();
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (TikaException e) {
			throw new IOException(e);
		} finally {
			handler.clear();
		}
	}
	
	/**
	 * Enriches the properties with additional metadata detected by Tika (e.g.
	 * the original document media type)
	 */
	@SuppressWarnings("unchecked")
	private void addTikaMetadataProperties(final Document document, final Map<String, Object> properties) {
		if (properties == null) {
			return;
		}
		
		final Map<String, Object> metadata = Maps.newLinkedHashMap();
		if (properties.containsKey(PropertyNames.METADATA)) {			
			metadata.putAll((Map<String, ?>)properties.get(PropertyNames.METADATA));
		}
		
		final NodeList nodes = document.getElementsByTagName("meta");
		for (int index = 0; index < nodes.getLength(); index++) {
			final Element element = (Element)nodes.item(index);
			final String name = element.getAttribute("name");
			final String value = element.getAttribute("content");
			
			if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(value)) {
				final Object previousValue = metadata.get(name);
				if (previousValue == null) {
					metadata.put(name, value);
				} else if (previousValue instanceof List) {
					((List<Object>)previousValue).add(value);
				} else {
					metadata.put(name, Lists.newArrayList(previousValue, value));
				}
			}
		}
		
		if (!metadata.isEmpty()) {
			properties.put(PropertyNames.METADATA, metadata);
		}
	}

	/**
	 * Creates a new handler to converter SAX content to DOM.
	 * <p>
	 * Whitespace normalisation will be performed on documents created by the handler.
	 */
	private static SAXContentToDOMHandler createHandler() throws ParserConfigurationException {
		final DocumentBuilder documentBuilder= DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();		
		final boolean whitespaceNormalisationEnabled = true;
		
		return new SAXContentToDOMHandler(documentBuilder,
				whitespaceNormalisationEnabled);
	}
}
