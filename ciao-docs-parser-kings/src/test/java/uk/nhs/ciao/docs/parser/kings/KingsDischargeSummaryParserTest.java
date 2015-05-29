package uk.nhs.ciao.docs.parser.kings;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closeables;

/**
 * Tests for {@link KingsDischargeSummaryParser} via the console main method
 * <p>
 * Tests are performed by running some input examples through the parser and checking the extracted
 * JSON content against corresponding expectation documents. The input and expectation documents
 * are on the classpath under the test resources.
 */
public class KingsDischargeSummaryParserTest {
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String,Object>>(){};
	
	private ObjectMapper objectMapper;
	
	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
	}
	
	@Test
	public void testPdfExamples() throws Exception {
		final File outputFolder = new File("target/pdf_output");
		delete(outputFolder);
		
		final String[] args = new String[] {
			"src/test/resources/" + getClass().getPackage().getName().replace('.', '/') + "/input",
			outputFolder.getPath()
		};
		KingsDischargeSummaryParser.main(args);
		
		assertTrue(outputFolder.isDirectory());
		
		// Check against expectations
		for (final String name: Arrays.asList("Example2", "Example3")) {
			checkJsonOutput(outputFolder, name + ".txt");
		}
		
		// Example.pdf is not a supported type - so there should be no Example.txt in the output
		assertFalse(new File(outputFolder, "Example.txt").exists());
	}
	
	private void checkJsonOutput(final File outputFolder, final String name) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = getClass().getResourceAsStream("expected/" + name);
			final Map<String, Object> expected = objectMapper.readValue(inputStream, MAP_TYPE);
			
			final File actualFile = new File(outputFolder, name);
			final Map<String, Object> actual = objectMapper.readValue(actualFile, MAP_TYPE);
			
			assertEquals("JSON content: " + name, expected, actual);
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
