package uk.nhs.itk.ciao.toc;

import java.util.Properties;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.itk.ciao.CIPRoutes;

public class TransferOfCareRoutes extends CIPRoutes {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransferOfCareRoutes.class);
	private static final String ROOT_PROPERTY = "parseDocumentRoutes";
	
	@Override
	public void configure() {
		super.configure();
		
		final Properties ciaoProperties = getContext().getRegistry()
				.lookupByNameAndType("ciaoProperties", Properties.class);
		
		final String[] routeNames = ciaoProperties.getProperty(ROOT_PROPERTY).split(",");
		for (final String routeName: routeNames) {
			final ParseDocumentRouteBuilder builder = new ParseDocumentRouteBuilder(
					routeName, ciaoProperties);
			builder.configure();
		}	
	}
	
	private class ParseDocumentRouteBuilder {
		private final String name;
		private final String inputFolder;
		private final String processorId;
		private final String outputFolder;
		private final String errorFolder;
		
		public ParseDocumentRouteBuilder(final String name, final Properties ciaoProperties) {
			this.name = name;
			this.inputFolder = findProperty(ciaoProperties, "inputFolder");
			this.processorId = findProperty(ciaoProperties, "processorId");;
			this.outputFolder = findProperty(ciaoProperties, "outputFolder");;
			this.errorFolder = findProperty(ciaoProperties, "errorFolder");;
		}
		
		private String findProperty(final Properties ciaoProperties, final String propertyName) {
			// Try the specific 'named' property
			String property = ciaoProperties.getProperty(ROOT_PROPERTY + "." + name + "." + propertyName, "");
			if (property.isEmpty()) {
				// Fall back to the general 'all-routes' property
				property = ciaoProperties.getProperty(ROOT_PROPERTY + "." + propertyName, "");				
				if (property.isEmpty()) {
					throw new IllegalArgumentException("Could not find property " + propertyName +
							" for route " + name);
				}
			}
			
			return property;
		}
		
		public void configure() {
			from("file://" + inputFolder + "?noop=true").id("parse-document-" + name)
			.streamCaching()
			.doTry()
				.processRef(processorId)				
				.log(LoggingLevel.INFO, LOGGER, "Parsed incoming document: ${file:name}")
				.marshal().json(JsonLibrary.Jackson)
				.to("file://" + outputFolder + "?fileName=${file:name.noext}.json")
			.doCatch(UnsupportedDocumentTypeException.class)
				.log(LoggingLevel.INFO, LOGGER, "Unsupported document type: ${file:name}")
				.to("file://" + errorFolder)
			.doCatch(Exception.class)
				.log(LoggingLevel.ERROR, LOGGER, "Exception while processing document: ${file:name}")
				.to("log:uk.nhs.itk.ciao.toc.TransferOfCareRoutes?level=ERROR&showCaughtException=true")
				.to("file://" + errorFolder);
		}
	}
}
