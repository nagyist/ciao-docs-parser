package uk.nhs.itk.ciao.camel;

import org.apache.camel.spring.Main;
import org.apache.camel.util.IOHelper;
import org.springframework.context.support.AbstractApplicationContext;

import uk.nhs.itk.ciao.configuration.CIAOConfig;
import uk.nhs.itk.ciao.exceptions.CIAOConfigurationException;
import uk.nhs.itk.ciao.spring.CiaoParentApplicationContextFactory;

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
	private static CamelApplication instance;
	
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
        //application.enableHangupSupport();
        application.run(args);
        
        // TODO: Remove this!
        Thread.sleep(50000);
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
