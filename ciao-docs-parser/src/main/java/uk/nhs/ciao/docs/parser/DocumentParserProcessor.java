package uk.nhs.ciao.docs.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.docs.parser.DocumentParser;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * A camel processor to parse an incoming document.
 * <p>
 * The processor delegates parsing to the {@link DocumentParser} provided
 * at runtime.
 */
public class DocumentParserProcessor implements Processor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserProcessor.class);
	
	private final DocumentParser parser;
	
	/***
	 * Constructs a new processor backed by the specified document parser
	 * 
	 * @param parser The parser used to process incoming documents
	 */
	public DocumentParserProcessor(final DocumentParser parser) {
		this.parser = Preconditions.checkNotNull(parser);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates processing of the incoming document to the configured parser
	 * and adds the extracted properties to the exchange output message as
	 * {@link ParsedDocument}.
	 * 
	 * @throws UnsupportedDocumentTypeException If the parser does not support the type (e.g. syntax)
	 * 		of the incoming document
	 * @throws IOException If parser failed to read the incoming document
	 */
	@Override
	public void process(final Exchange exchange) throws UnsupportedDocumentTypeException, IOException {
		LOGGER.debug("process: {}", exchange);
		
		final Document originalDocument = Document.valueOf(exchange.getIn());		
		final InputStream inputStream = originalDocument.getStream();
		
		try {
			final Map<String, Object> properties = parser.parseDocument(inputStream);
			LOGGER.debug("Parsed document properties: {} -> {}", exchange, properties);
			
			final Message outputMessage = exchange.getOut();
			final ParsedDocument parsedDocument = new ParsedDocument(originalDocument, properties);
			outputMessage.setBody(parsedDocument);
			outputMessage.setHeader(Exchange.FILE_NAME, originalDocument.getName());
		} finally {
			Closeables.closeQuietly(inputStream);
		}
	}
}
