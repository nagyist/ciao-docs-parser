package uk.nhs.ciao.docs.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import uk.nhs.ciao.camel.CamelApplicationRunner;
import uk.nhs.ciao.camel.CamelApplicationRunner.AsyncExecution;
import uk.nhs.ciao.configuration.CIAOConfig;
import uk.nhs.ciao.configuration.impl.MemoryCipProperties;
import static org.junit.Assert.*;


/**
 * Tests for the ciao-docs-parser CIP application
 */
public class DocumentParserApplicationTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParserApplicationTest.class);
	private static final String CIP_NAME = "ciao-docs-parser";
	
	@Rule
	public Timeout globalTimeout = Timeout.seconds(30);
	
	private ExecutorService executorService;
	private DocumentParserApplication application;
	private AsyncExecution execution;
	private ObjectMapper objectMapper;
	
	@Before
	public void setup() throws Exception {
		final CIAOConfig ciaoConfig = setupCiaoConfig();
		application = new DocumentParserApplication(ciaoConfig);
		
		executorService = Executors.newSingleThreadExecutor();
		objectMapper = new ObjectMapper();
	}
	
	private CIAOConfig setupCiaoConfig() throws IOException {
		final MemoryCipProperties cipProperties = new MemoryCipProperties(CIP_NAME, "tests");
		addProperties(cipProperties, CIP_NAME + ".properties");
		addProperties(cipProperties, CIP_NAME + "-test.properties");
		
		return new CIAOConfig(cipProperties);
	}
	
	private void addProperties(final MemoryCipProperties cipProperties, final String resourcePath) throws IOException {
		final Resource resource = new ClassPathResource(resourcePath);
		final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
		cipProperties.addConfigValues(properties);
	}
	
	private void runApplication() throws Exception {
		LOGGER.info("About to start camel application");
		
		execution = CamelApplicationRunner.runApplication(application, executorService);
		
		LOGGER.info("Camel application has started");
	}
	
	@After
	public void tearDown() {
		try {
			stopApplication();
		} finally {
			// Always stop the executor service
			executorService.shutdownNow();
		}
	}
	
	private void stopApplication() {
		if (execution == null) {
			return;
		}
		
		final CamelContext context = getCamelContext();
		try {
			LOGGER.info("About to stop camel application");
			execution.getRunner().stop();
			execution.getFuture().get(); // wait for task to complete
			LOGGER.info("Camel application has stopped");
		} catch (Exception e) {
			LOGGER.warn("Exception while trying to stop camel application", e);
		} finally {
			if (context != null) {
				MockEndpoint.resetMocks(context);
			}
		}
	}
	
	private CamelContext getCamelContext() {
		if (execution == null) {
			return null;
		}
		
		final List<CamelContext> camelContexts = execution.getRunner().getCamelContexts();
		return camelContexts.isEmpty() ? null : camelContexts.get(0);
	}
	
	@Test
	public void testApplicationStartsUsingSpringConfig() throws Exception {
		LOGGER.info("Checking the application starts via spring config");

		runApplication();
		
		assertNotNull(execution);
		assertFalse(execution.getRunner().getCamelContexts().isEmpty());
		assertNotNull(getCamelContext());
	}
	
	@Test
	public void testApplicationProcessesAParsableDocument() throws Exception {
		LOGGER.info("Checking a parsable document");

		final Map<String, Object> expectedProperties = getExpectionFromJson("Example2.txt");
		runApplication();
		
		final MockEndpoint endpoint = MockEndpoint.resolve(getCamelContext(), "jms:queue:parsed-documents");
		endpoint.expectedMessageCount(1);
		endpoint.expectedMessagesMatches(new Predicate() {			
			@Override
			public boolean matches(final Exchange exchange) {
				try {
					final byte[] json = exchange.getIn().getBody(byte[].class);
					final Map<String, Object> parsedDocument = fromJson(json);
					
					// check the value of 'properties' in the parsedDocument
					@SuppressWarnings("unchecked")
					final Map<String, Object> properties = (Map<String, Object>) parsedDocument.get("properties");
					return properties.entrySet().containsAll(expectedProperties.entrySet());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		copyInputFixtureToFile("Example2.pdf", new File("target/test-data/input/auto-detect"));
		
		MockEndpoint.assertIsSatisfied(10, TimeUnit.SECONDS, endpoint);
	}
	
	private Map<String, Object> fromJson(final byte[] json) throws IOException {
		return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
	}
	
	private Map<String, Object> getExpectionFromJson(final String resourceName) throws IOException {
		final Resource resource = new ClassPathResource("fixtures/expected/" + resourceName);
		InputStream in = null;
		try {
			in = resource.getInputStream();
			return fromJson(ByteStreams.toByteArray(in));
		} finally {
			Closeables.closeQuietly(in);
		}
	}
	
	private void copyInputFixtureToFile(final String resourceName, final File outputFolder) throws IOException {
		final Resource resource = new ClassPathResource("fixtures/input/" + resourceName);
		
		// First write to a temp file
		final File tempFolder = new File("target/test-data/temp");
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
		final File tempFile = new File(tempFolder, resourceName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = resource.getInputStream();
			out = new FileOutputStream(tempFile);
			ByteStreams.copy(in, out);
			out.flush();
			
			Closeables.close(out, true);
			Closeables.closeQuietly(in);
			
			// Then move to the final destination
			final File outputFile = new File(outputFolder, resourceName);
			if (!outputFolder.exists()) {
				outputFolder.mkdirs();
			}
			assertTrue("could not move file to: " + outputFile, tempFile.renameTo(outputFile));
			
		} finally {
			Closeables.close(out, true);
			Closeables.closeQuietly(in);
		}
	}
}
