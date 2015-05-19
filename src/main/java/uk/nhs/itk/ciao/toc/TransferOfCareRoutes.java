package uk.nhs.itk.ciao.toc;

import java.io.IOException;

import org.apache.camel.Processor;
import org.apache.camel.spi.Registry;

import uk.nhs.itk.ciao.CIPRoutes;

public class TransferOfCareRoutes extends CIPRoutes {
	@Override
	public void configure() {
		super.configure();
		
		from("file://{{inputFolder}}?noop=true").id("file-transfer")
			.streamCaching()
			.doTry()
				.process(lookupProcessor("dischargeSummaryProcessor"))
				.to("file://{{outputFolder}}")
			.doCatch(IOException.class)
				.to("file://{{errorFolder}}");
			
	}
	
	private Processor lookupProcessor(final String name) {
		final Registry registry = getContext().getRegistry();
		return registry.lookupByNameAndType(name, Processor.class);
	}
}
