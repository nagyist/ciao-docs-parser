package uk.nhs.ciao.docs.parser;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.camel.CamelApplication;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.itk.ciao.CIPRoutes;
import uk.nhs.itk.ciao.configuration.CIAOConfig;
import uk.nhs.itk.ciao.exceptions.CIAOConfigurationException;

public class DocumentParserRoutes extends CIPRoutes {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserRoutes.class);
	private static final String ROOT_PROPERTY = "parseDocumentRoutes";
	
	@Override
	public void configure() {
		super.configure();
		
		final CIAOConfig config = CamelApplication.getConfig(getContext());
		
		try {
			final String[] routeNames = config.getConfigValue(ROOT_PROPERTY).split(",");
			for (final String routeName: routeNames) {
				final ParseDocumentRouteBuilder builder = new ParseDocumentRouteBuilder(
						routeName, config);
				builder.configure();
			}
		} catch (CIAOConfigurationException e) {
			throw new RuntimeException("Unable to build routes from CIAOConfig", e);
		}
	}
	
	private class ParseDocumentRouteBuilder {
		private final String name;
		private final String inputFolder;
		private final String processorId;
		private final String outputFolder;
		private final String completedFolder;
		private final String errorFolder;
		private final String idempotentRepositoryId;
		private final String inProgressRepositoryId;
		
		public ParseDocumentRouteBuilder(final String name, final CIAOConfig config) throws CIAOConfigurationException {
			this.name = name;
			this.inputFolder = findProperty(config, "inputFolder");
			this.processorId = findProperty(config, "processorId");
			this.outputFolder = findProperty(config, "outputFolder");
			this.completedFolder = findProperty(config, "completedFolder");			
			this.errorFolder = findProperty(config, "errorFolder");
			this.idempotentRepositoryId = findProperty(config, "idempotentRepositoryId");
			this.inProgressRepositoryId = findProperty(config, "inProgressRepositoryId");
		}
		
		/**
		 * Try the specific 'named' property then fall back to the general 'all-routes' property
		 */
		private String findProperty(final CIAOConfig config, final String propertyName) throws CIAOConfigurationException {
			final String specificName = ROOT_PROPERTY + "." + name + "." + propertyName;
			final String genericName = ROOT_PROPERTY + "." + propertyName;
			if (config.getConfigKeys().contains(specificName)) {
				return config.getConfigValue(specificName);
			} else if (config.getConfigKeys().contains(genericName)) {
				
				return config.getConfigValue(genericName);
			} else {
				throw new CIAOConfigurationException("Could not find property " + propertyName +
						" for route " + name);
			}
		}

		@SuppressWarnings("deprecation")
		public void configure() {
			from("file://" + inputFolder + "?idempotent=true&" +
					"idempotentRepository=#" + idempotentRepositoryId + "&" +
					"inProgressRepository=#" + inProgressRepositoryId + "&" +
					"readLock=idempotent&" +
					"move=" + completedFolder + "/${date:now:yyyy/MM/dd/HHmmSS}-${file:name}&" +
					"moveFailed=" + errorFolder + "/${date:now:yyyy/MM/dd/HHmmSS}-${file:name}")
			.id("parse-document-" + name)
			.streamCaching()
			.doTry()
				.processRef(processorId)				
				.log(LoggingLevel.INFO, LOGGER, "Parsed incoming document: ${file:name}")
				.marshal().json(JsonLibrary.Jackson)
				.to("file://" + outputFolder + "?fileName=${file:name.noext}.json")
			.doCatch(UnsupportedDocumentTypeException.class)
				.log(LoggingLevel.INFO, LOGGER, "Unsupported document type: ${file:name}")
				.handled(false)
			.doCatch(Exception.class)
				.log(LoggingLevel.ERROR, LOGGER, "Exception while processing document: ${file:name}")
				.to("log:uk.nhs.itk.ciao.toc.TransferOfCareRoutes?level=ERROR&showCaughtException=true")
				.handled(false);
		}
	}
}
