package uk.nhs.ciao.docs.parser;

import static uk.nhs.ciao.docs.parser.ParsedDocumentHeaders.*;

import java.io.File;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spi.IdempotentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.CIPRoutes;
import uk.nhs.ciao.camel.CamelApplication;
import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * Configures multiple camel document parser routes determined by properties specified
 * in the applications registered {@link CIAOConfig}.
 * <p>
 * The 'bootstrap' / {@link #ROOT_PROPERTY} determines which named routes to created (via
 * a comma-separated list).
 * <p>
 * The properties of each route are then looked up via the <code>${ROOT_PROPERTY}.${routeName}.${propertyName}</code>,
 * falling back to <code>${ROOT_PROPERTY}.${propertyName}</code> if a specified property is not provided.
 * This allows for shorthand specification of properties when they are shared across multiple routes.
 * <p>
 * The following properties are supported per named route:
 * <dl>
 * <dt>inputFolder<dt>
 * <dd>The file path (absolute or relative to the working directory) of the input folder to monitor</dd>
 * 
 * <dt>processorId<dt>
 * <dd>The spring ID of this routes {@link DocumentParserProcessor}</dd>
 * 
 * <dt>outputQueue<dt>
 * <dd>The name of the queue output messages should be sent to</dd>
 * 
 * <dt>completedFolder<dt>
 * <dd>The file path (absolute or relative to inputFolder) where successfully processed input files should be moved to</dd>
 * 
 * <dt>errorFolder<dt>
 * <dd>The file path (absolute or relative to inputFolder) where non process-able input files should be moved to</dd>
 * 
 * <dt>idempotentRepositoryId<dt>
 * <dd>The spring ID of the {@link IdempotentRepository} to use for the input folders idempotentRepository</dd>
 * 
 * <dt>inProgressRepositoryId<dt>
 * <dd>The spring ID of the {@link IdempotentRepository} to use for the input folders inProgressRepository</dd>
 * </dl>
 * <p>
 * The {@link IdempotentRepository} is used to support multiple competing file consumers - see http://camel.apache.org/idempotent-consumer.html
 * and http://camel.apache.org/file2.html : readLock=idempotent for further details. The route is configured to move the
 * file into a completion directory (filed with a timestamp), it is therefore safe to use readLockRemoveOnCommit=true.
 * This ensures that processed files are removed from idempotentRepository, and identically named files can be processed
 * in the future.
 */
public class DocumentParserRoutes extends CIPRoutes {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserRoutes.class);
	
	/**
	 * The root property 
	 */
	public static final String ROOT_PROPERTY = "documentParserRoutes";
	
	/**
	 * Creates multiple document parser routes
	 * 
	 * @throws RuntimeException If required CIAO-config properties are missing
	 */
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
	
	/**
	 * Creates a Camel route for the specified name / property prefix.
	 * <p>
	 * Each configurable property is determined by:
	 * <ul>
	 * <li>Try the specific property: <code>${ROOT_PROPERTY}.${name}.${propertyName}</code></li>
	 * <li>If missing fallback to: <code>${ROOT_PROPERTY}.${propertyName}</code></li>
	 * </ul>
	 */
	private class ParseDocumentRouteBuilder {
		private final String name;
		private final String inputFolder;
		private final String processorId;
		private final String outputQueue;
		private final String completedFolder;
		private final String inProgressFolder;
		private final String errorFolder;
		private final String idempotentRepositoryId;
		private final String inProgressRepositoryId;
		
		/**
		 * Creates a new route builder for the specified name / property prefix
		 * 
		 * @param name The route name / property prefix
		 * @throws CIAOConfigurationException If required properties were missing
		 */
		public ParseDocumentRouteBuilder(final String name, final CIAOConfig config) throws CIAOConfigurationException {
			this.name = name;
			this.inputFolder = findProperty(config, "inputFolder");
			this.processorId = findProperty(config, "processorId");
			this.outputQueue = findProperty(config, "outputQueue");
			this.completedFolder = findProperty(config, "completedFolder");
			this.inProgressFolder = findProperty(config, "inProgressFolder");
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

		/**
		 * Configures / creates a new Camel route corresponding to the set of CIAO-config
		 * properties associated with the route name.
		 */
		@SuppressWarnings("deprecation")
		public void configure() {
			from("file://" + inputFolder + "?idempotent=true&" +
					"idempotentRepository=#" + idempotentRepositoryId + "&" +
					"inProgressRepository=#" + inProgressRepositoryId + "&" +
					"readLock=idempotent&" +
//					"delete=true&" + // file is initially copied into the in-progress directory
//					"move=&" + // disables move when completed - this will be handled by a later component when all processing is completed
					"move=${header." + IN_PROGRESS_FOLDER + "}/${file:name}&" +
					"moveFailed=${header." + ERROR_FOLDER + "}/${file:name}")
			.id("parse-document-" + name)

			// Generate the standard parsed document headers
			.setHeader(TIMESTAMP, method(System.class, "currentTimeMillis()"))
			.setHeader(Exchange.CORRELATION_ID, method(DocumentParserRoutes.class, "generateId()"))
			.setHeader(SOURCE_FILE_NAME, simple("${file:onlyname}"))
			.setHeader(IN_PROGRESS_FOLDER, simple(inProgressFolder))
			.setHeader(COMPLETED_FOLDER, simple(completedFolder))
			.setHeader(ERROR_FOLDER, simple(errorFolder))
			
			// Ensure that any relative paths are converted to absolute paths (for later CIPs/components)
			.setHeader(IN_PROGRESS_FOLDER, method(DocumentParserRoutes.class, "getAbsolutePath(${header." + IN_PROGRESS_FOLDER + "})"))
			.setHeader(COMPLETED_FOLDER, method(DocumentParserRoutes.class, "getAbsolutePath(${header." + COMPLETED_FOLDER + "})"))
			.setHeader(ERROR_FOLDER, method(DocumentParserRoutes.class, "getAbsolutePath(${header." + ERROR_FOLDER + "})"))
			
			.streamCaching()
			.doTry()
//				.multicast()
					// First create a copy in the in-progress folder
					// The file2 preMove option cannot be used because context specific properties and
					// headers need to be evaluated *before* the move can take place
//					.to("file://?fileName=${header." + IN_PROGRESS_FOLDER + "}/${file:name}")
					
					// Then process the file in a standard pipeline
//					.pipeline()
						.processRef(processorId)	
						.log(LoggingLevel.INFO, LOGGER, "Parsed incoming document: ${file:name}")
						.marshal().json(JsonLibrary.Jackson)
						.setHeader(Exchange.FILE_NAME, simple("${file:name.noext}.json"))
						.to("jms:queue:" + outputQueue)
//					.end()
//				.end()
			.endDoTry()
			.doCatch(UnsupportedDocumentTypeException.class)
				.log(LoggingLevel.INFO, LOGGER, "Unsupported document type: ${file:name}")
				.handled(false)
			.doCatch(Exception.class)
				.log(LoggingLevel.ERROR, LOGGER, "Exception while processing document: ${file:name}")
				.to("log:" + LOGGER.getName() + "?level=ERROR&showCaughtException=true")
				.handled(false);
		}
	}
	
	/**
	 * Generates a unique ID to represent the processing of a document
	 */
	public static String generateId() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Returns the absolute path associated with the specified file path.
	 * <p>
	 * If <code>path</code> is relative, the current working directory is used to resolve
	 * the absolute path
	 */
	public static String getAbsolutePath(final String path) {
		return new File(path).getAbsolutePath();
	}
}
