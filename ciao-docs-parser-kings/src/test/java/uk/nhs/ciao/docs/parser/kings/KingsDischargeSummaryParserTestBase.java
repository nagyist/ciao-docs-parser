package uk.nhs.ciao.docs.parser.kings;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;

import uk.nhs.ciao.docs.parser.PropertyNames;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closeables;

/**
 * Tests for {@link KingsDischargeSummaryParser} via the main method (console or gui mode)
 * <p>
 * Tests are performed by running some input examples through the parser and checking the extracted
 * JSON content against corresponding expectation documents. The input and expectation documents
 * are on the classpath under the test resources.
 */
public abstract class KingsDischargeSummaryParserTestBase {
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String,Object>>(){};
	
	private ObjectMapper objectMapper;
	File inputFolder;
	File outputFolder;
	
	@Before
	public void setup() throws Exception {
		objectMapper = new ObjectMapper();
		
		inputFolder = new File("src/test/resources/" +
				getClass().getPackage().getName().replace('.', '/') + "/input");
		
		outputFolder = new File("target/pdf_output");
		delete(outputFolder);
	}
	
	protected void runMainTest(final String... args) throws Exception {
		KingsDischargeSummaryParser.main(args);
		
		assertExpectedOutput();
	}
	
	protected void runTest(final KingsDischargeSummaryParser parser) throws Exception {
		parser.run();
		
		assertExpectedOutput();
	}
	
	private void assertExpectedOutput() throws Exception {
		assertTrue(outputFolder.isDirectory());
		
		// Check against expectations
		for (final String name: Arrays.asList("Example2", "Example3")) {
			checkJsonOutput(name + ".txt", "application/pdf");
		}
		
		for (final String name: Arrays.asList("Example4")) {
			checkJsonOutput(name + ".txt", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}
		
		// Example.pdf is not a supported type - so there should be no Example.txt in the output
		assertFalse(new File(outputFolder, "Example.txt").exists());
	}
	
	@SuppressWarnings("unchecked")
	private void checkJsonOutput(final String name, final String mediaType) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = getClass().getResourceAsStream("expected/" + name);
			final Map<String, Object> expected = objectMapper.readValue(inputStream, MAP_TYPE);
			
			final File actualFile = new File(outputFolder, name);
			final Map<String, Object> actual = objectMapper.readValue(actualFile, MAP_TYPE);

			assertTrue("Parsed content: " + name, actual.entrySet().containsAll(expected.entrySet()));
			assertTrue("Metadata expected: " + name, actual.containsKey(PropertyNames.METADATA));
			assertEquals("Media type: " + name, mediaType,
					((Map<String, Object>)actual.get(PropertyNames.METADATA)).get(PropertyNames.CONTENT_TYPE));
		} finally {
			Closeables.closeQuietly(inputStream);
		}
	}
	
	private void delete(final File file) {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			for (final File child: file.listFiles()) {
				delete(child);
			}
			file.delete();
		}
	}
}
