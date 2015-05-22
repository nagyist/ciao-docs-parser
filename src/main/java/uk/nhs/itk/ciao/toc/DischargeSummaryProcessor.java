package uk.nhs.itk.ciao.toc;

import java.io.InputStream;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

public class DischargeSummaryProcessor implements Processor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DischargeSummaryProcessor.class);
	
	private final DocumentParser parser;
	
	public DischargeSummaryProcessor(final DocumentParser parser) {
		this.parser = Preconditions.checkNotNull(parser);
	}
	
	@Override
	public void process(final Exchange exchange) throws Exception {
		LOGGER.debug("process: {}", exchange);
		final Message inputMessage = exchange.getIn();
		final String location = inputMessage.getHeader(Exchange.FILE_PATH, String.class);
		final InputStream body = inputMessage.getBody(InputStream.class);
		
		try {
			final Map<String, Object> properties = parser.parseDocument(body);
			LOGGER.debug("Parsed document properties: {} -> {}", exchange, properties);
			
			final DischargeSummary summary = new DischargeSummary(location, properties);
			final Message outputMessage = exchange.getOut();
			outputMessage.setBody(summary);
			outputMessage.setHeader(Exchange.FILE_NAME, inputMessage.getHeader(Exchange.FILE_NAME));
		} finally {
			Closeables.closeQuietly(body);
		}
	}
}
