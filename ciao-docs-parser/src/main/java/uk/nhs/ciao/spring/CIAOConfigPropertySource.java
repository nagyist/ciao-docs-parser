package uk.nhs.ciao.spring;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * An adaptor to view a {@link CIAOConfig} instance as a spring
 * property source
 * <p>
 * To make the properties available via the spring environment, the property source
 * should be registered on the application context.
 * <p>
 * The spring environment properties are not automatically made available via property place-holders
 * referenced in an XML configuration. To make them available an instance of
 * {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer} should be registered
 * in the application context. The instance can be created without reference to any additional
 * properties resources (unless non-CIAO properties are desired) and the CIAO properties will
 * automatically be made available through spring property place-holders.
 * 
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 * @see org.springframework.core.env.ConfigurableEnvironment#getPropertySources()
 */
public class CIAOConfigPropertySource extends EnumerablePropertySource<CIAOConfig> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIAOConfigPropertySource.class);
	private static final String[] NO_PROPERTY_NAMES = new String[0];

	public CIAOConfigPropertySource(final String name, final CIAOConfig source) {
		super(name, source);
	}
	
	/**
	 * Returns the backing CIAOConfig instance
	 */
	public CIAOConfig getCIAOConfig() {
		return getSource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getProperty(final String name) {
		try {
			return source.getConfigValue(name);
		} catch (final CIAOConfigurationException e) {
			LOGGER.debug("Could not find property {} in CIAOConfig", name, e);
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getPropertyNames() {
		try {
			final Set<String> configKeys = source.getConfigKeys();
			return configKeys.toArray(new String[configKeys.size()]);
		} catch (final CIAOConfigurationException e) {
			LOGGER.debug("Could not get config keys from CIAOConfig", e);
			return NO_PROPERTY_NAMES;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsProperty(final String name) {
		try {
			return source.getConfigKeys().contains(name);
		} catch (final CIAOConfigurationException e) {
			LOGGER.debug("Could not get config keys from CIAOConfig", e);
			return false;
		}
	}
}
