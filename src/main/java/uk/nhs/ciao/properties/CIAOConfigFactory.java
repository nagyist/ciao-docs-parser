package uk.nhs.ciao.properties;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * A factory to initialise CIAOConfig from a set of default properties
 */
public class CIAOConfigFactory {
	public static final String PROPERTY_CIP_NAME = "cip.name";
	public static final String PROPERTY_CIP_VERSION = "cip.version";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CIAOConfigFactory.class);
	
	private CIAOConfigFactory() {
		// Suppress default constructor
	}
	
	public static CIAOConfig getCIAOConfigFromClasspath(final String resourcePath, final String... args) throws CIAOConfigurationException {
		final ClassPathResource resource = new ClassPathResource(resourcePath);
		return getCIAOConfig(resource, args);
	}
	
	public static CIAOConfig getCIAOConfig(final Resource resource, final String... args) throws CIAOConfigurationException {
		try {
			final Properties defaultConfig = PropertiesLoaderUtils.loadProperties(resource);
			return getCIAOConfig(defaultConfig, args);
		} catch (IOException e) {
			throw new CIAOConfigurationException("Could not read properties from " + resource, e);
		}
	}
	
	public static CIAOConfig getCIAOConfig(final Properties defaultConfig, final String... args) throws CIAOConfigurationException {
		final String name = defaultConfig.getProperty(PROPERTY_CIP_NAME);
		final String version = defaultConfig.getProperty(PROPERTY_CIP_VERSION);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attempting to load CIAOConfig: name={}, version={}", name, version);
		}
		
		return new CIAOConfig(args, name, version, defaultConfig);
	}
}
