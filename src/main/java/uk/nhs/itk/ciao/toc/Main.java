package uk.nhs.itk.ciao.toc;

import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import uk.nhs.itk.ciao.camel.CamelApplication;
import uk.nhs.itk.ciao.configuration.CIAOConfig;

public class Main {
	public static void main(final String[] args) throws Exception {
		final ClassPathResource resource = new ClassPathResource("ciao-transfer-of-care.properties");
		final Properties defaultConfig = PropertiesLoaderUtils.loadProperties(resource);
		final CIAOConfig config = new CIAOConfig(args, "ciao-transfer-of-care", "1.0.0-SNAPSHOT", defaultConfig);
		CamelApplication.startApplication(config, args);
	}
}
