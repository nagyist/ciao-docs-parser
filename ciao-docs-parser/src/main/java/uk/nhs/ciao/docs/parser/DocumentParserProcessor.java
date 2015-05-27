package uk.nhs.ciao.docs.parser;

import java.io.ByteArrayInputStream;
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

public class DocumentParserProcessor implements Processor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserProcessor.class);
	
	private final DocumentParser parser;
	
	public DocumentParserProcessor(final DocumentParser parser) {
		this.parser = Preconditions.checkNotNull(parser);
	}
	
	@Override
	public void process(final Exchange exchange) throws Exception {
		LOGGER.debug("process: {}", exchange);
		
		final OriginalDocument originalDocument = getOriginalDocument(exchange);		
		final InputStream inputStream = new ByteArrayInputStream(originalDocument.getBody());
		
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
	
	private OriginalDocument getOriginalDocument(final Exchange exchange) {
		final Message inputMessage = exchange.getIn();
		final String name = inputMessage.getHeader(Exchange.FILE_NAME, String.class);
		final byte[] body = inputMessage.getBody(byte[].class);
		
		return new OriginalDocument(name, body);
	}
}
