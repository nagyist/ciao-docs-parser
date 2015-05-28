package uk.nhs.ciao.docs.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

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
	 * extractor is used to find properties with the interpreted document text.
	 */
	@Override
	public Map<String, Object> parseDocument(final InputStream in)
			throws UnsupportedDocumentTypeException, IOException {
		final Document document = parseToDom(in);
		return propertiesExtractor.extractProperties(document);
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
