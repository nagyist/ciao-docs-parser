package uk.nhs.ciao.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.Main;
import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;
import uk.nhs.ciao.properties.CIAOConfigFactory;
import uk.nhs.ciao.spring.CiaoParentApplicationContextFactory;

/**
 * A CIAO camel application
 * <p>
 * Camel is initialised (via spring) and the specified CIAO properties
 * are registered and made available via Spring environment properties.
 * <p>
 * To make the CIAO/spring properties available to camel - an instance
 * of {@link org.apache.camel.spring.spi.BridgePropertyPlaceholderConfigurer}
 * should be registered in the XML configuration.
 * 
 * @see #startApplication(CIAOConfig, String...)
 */
public class CamelApplication extends Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(CamelApplication.class);
	
	private static CamelApplication instance;
	
	/**
	 * Starts a new camel application using the specified default configuration
	 * from the classpath
	 * 
	 * @param defaultConfigPath The location of the default configuration properties
	 * 			on the classpath
	 * @param args The application arguments
	 * @throws Exception If the application could not be started
	 * @see CIAOConfigFactory
	 */
	public static CamelApplication startApplication(final String defaultConfigPath, final String... args) throws Exception {
		final CIAOConfig config = CIAOConfigFactory.getCIAOConfigFromClasspath(defaultConfigPath, args);
		LOGGER.info("Initialised CIP configuration");
		LOGGER.info("CIP config values: {}", config);
		
		return startApplication(config, args);
	}
	
	/**
	 * Starts a new camel application
	 * 
	 * @param config The CIAO configuration used by the application
	 * @param args The application arguments
	 * @throws Exception If the application could not be started
	 */
	public static CamelApplication startApplication(final CIAOConfig config, final String... args) throws Exception {
        final CamelApplication application = new CamelApplication(config);
        Main.instance = application;
        instance = application;
        application.enableHangupSupport();
        application.run(args);

        return application;
    }
	
	/**
	 * The system-wide application instance
	 * 
	 * @see Main#getInstance()
	 */
	public static CamelApplication getInstance() {
		return instance;
	}
	
	/**
	 * Gets the CIAOConfig from the specified camel context
	 * 
	 * @return The configuration stored in the context's registry
	 * @see CiaoParentApplicationContextFactory#PROPERTY_CIAO_CONFIG
	 */
	public static CIAOConfig getConfig(final CamelContext context) {
		return getConfig(context.getRegistry());
	}
	
	/**
	 * Gets the CIAOConfig from the specified camel registry
	 * 
	 * @return The configuration stored in the specified registry
	 * @see CiaoParentApplicationContextFactory#PROPERTY_CIAO_CONFIG
	 */
	public static CIAOConfig getConfig(final Registry registry) {
		return registry.lookupByNameAndType(CiaoParentApplicationContextFactory.PROPERTY_CIAO_CONFIG,
				CIAOConfig.class);
	}
	
	private final CIAOConfig config;

	/**
	 * Creates a new application backed by the specified CIAO configuration
	 */
	public CamelApplication(final CIAOConfig config) {
		super();
		this.config = config;
	}
	
	/**
	 * The config used by this application
	 */
	public CIAOConfig getCIAOConfig() {
		return config;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStart() throws Exception {
		final AbstractApplicationContext parentContext = getOrCreateParentApplicationContext();
		parentContext.refresh();
		parentContext.start();
		
		super.doStart();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStop() throws Exception {
		super.doStop();
		
		IOHelper.close(getParentApplicationContext());
	}

	/**
	 * Returns the parent application context, creating a new instance if required.
	 */
	private AbstractApplicationContext getOrCreateParentApplicationContext() throws CIAOConfigurationException {
		AbstractApplicationContext parentContext = getParentApplicationContext();
		
		if (parentContext == null) {
			parentContext = new CiaoParentApplicationContextFactory(config).createParentApplicationContext();
			setParentApplicationContext(parentContext);
		}
		
		return parentContext;
	}
}
