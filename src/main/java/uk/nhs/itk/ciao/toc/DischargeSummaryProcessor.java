package uk.nhs.itk.ciao.toc;

import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.google.common.base.Preconditions;

public class DischargeSummaryProcessor implements Processor {
	private final DischargeSummaryReader<?> reader;
	
	public DischargeSummaryProcessor(final DischargeSummaryReader<?> reader) {
		this.reader = Preconditions.checkNotNull(reader);
	}
	
	@Override
	public void process(final Exchange exchange) throws Exception {
		final InputStream in = exchange.getIn().getBody(InputStream.class);
		final Object result = reader.readDocument(in);
		exchange.getIn().setBody(result);
	}
}
