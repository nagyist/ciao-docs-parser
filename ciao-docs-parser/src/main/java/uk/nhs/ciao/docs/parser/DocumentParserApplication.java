package uk.nhs.ciao.docs.parser;

import uk.nhs.ciao.camel.CamelApplication;
import uk.nhs.ciao.camel.CamelApplicationRunner;
import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * The main ciao-docs-parser application
 * <p>
 * The application configuration is handled by Spring loading META-INF/spring/beans.xml 
 * resource off the class-path. Additional spring configuration is loaded based on
 * properties specified in CIAO-config (ciao-docs-parser.properties). At runtime the application
 * can start multiple routes (one per input folder) determined via the specified CIAO-config properties. 
 * <p>
 * The main flow of the application is:
 * <code>Input source (folder monitor) -> document parser (property extraction)
 * -> Output sink (JMS queue)</code>
 * <p>
 * The following properties configure which additional spring configuration to load:
 * 
 * <dl>
 * <dt>repositoryConfig:</dt>
 * <dd><code>META-INF/spring/repositories/${value}.xml</code></dd>
 * <dd>Configures the Idempotent Consumer pattern to handle multiple competing file consumers.
 * Hazelcast is recommended when running in a clustered environment</dd>
 * 
 * <dt>processorConfig:</dt>
 * <dd><code>META-INF/spring/processors/${value}.xml</code></dd>
 * <dd>Configures which parsers and propertyextractors to load. These are later referenced by ID when
 * choosing routes via CIAO-properties</dd>
 * 
 * <dt>messagingConfig:</dt>
 * <dd><code>META-INF/spring/messaging/${value}.xml</code><dd>
 * <dd>Configures which Camel messaging component to use for publishing output messages. The component
 * should be mapped to the 'jms' ID.</dd>
 * </dl>
 */
public class DocumentParserApplication extends CamelApplication {
	/**
	 * Runs the document parser application
	 * 
	 * @see CIAOConfig#CIAOConfig(String[], String, String, java.util.Properties)
	 * @see CamelApplicationRunner
	 */
	public static void main(final String[] args) throws Exception {
		final CamelApplication application = new DocumentParserApplication(args);
		CamelApplicationRunner.runApplication(application);
	}
	
	public DocumentParserApplication(final String[] args) throws CIAOConfigurationException {
		super("ciao-docs-parser.properties", args);
	}
}
