package uk.nhs.ciao.docs.parser;

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
		final Message inputMessage = exchange.getIn();
		// TODO: There is a hidden dependency on the input document coming from a file (rather than say an email or message etc)
		final String originalDocumentLocation = inputMessage.getHeader(Exchange.FILE_PATH, String.class);
		final InputStream body = inputMessage.getBody(InputStream.class);
		
		try {
			final Map<String, Object> properties = parser.parseDocument(body);
			LOGGER.debug("Parsed document properties: {} -> {}", exchange, properties);
			
			final ParsedDocument summary = new ParsedDocument(originalDocumentLocation, properties);
			final Message outputMessage = exchange.getOut();
			outputMessage.setBody(summary);
			outputMessage.setHeader(Exchange.FILE_NAME, inputMessage.getHeader(Exchange.FILE_NAME));
		} finally {
			Closeables.closeQuietly(body);
		}
	}
}
