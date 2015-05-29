package uk.nhs.ciao.docs.parser.kings;

import static org.junit.Assert.*;

import java.awt.Component;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;

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
@PrepareForTest(KingsDischargeSummaryParser.class)
@PowerMockIgnore("javax.swing.*")
public class KingsDischargeSummaryParserTest {
	static {
       PowerMockAgent.initializeIfNeeded();
   }
	
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String,Object>>(){};
	
	@Rule
    public PowerMockRule rule = new PowerMockRule();
	
	private ObjectMapper objectMapper;
	private File inputFolder;
	private File outputFolder;
	
	@Before
	public void setup() throws Exception {
		objectMapper = new ObjectMapper();
		
		inputFolder = new File("src/test/resources/" +
				getClass().getPackage().getName().replace('.', '/') + "/input");
		
		outputFolder = new File("target/pdf_output");
		delete(outputFolder);
	}
	
	@Test
	public void testPdfExamplesWithConsole() throws Exception {
		final String[] args = new String[] {
			inputFolder.getPath(),
			outputFolder.getPath()
		};
		KingsDischargeSummaryParser.main(args);
		
		assertExpectedOutput();
	}
	
	@Test
	public void testPdfExamplesWithGUI() throws Exception {
		final JFileChooser fileChooser = PowerMockito.mock(JFileChooser.class);
		final JOptionPane pane = PowerMockito.mock(JOptionPane.class);
		final JDialog dialog = PowerMockito.mock(JDialog.class);
		try {

			PowerMockito.whenNew(JFileChooser.class).withAnyArguments()
				.thenReturn(fileChooser);
			
			PowerMockito.whenNew(JOptionPane.class).withAnyArguments()
			.thenReturn(pane);
			
			PowerMockito.when(fileChooser.showDialog(Mockito.any(Component.class), Mockito.anyString()))
				.thenReturn(JFileChooser.APPROVE_OPTION);
			
			PowerMockito.when(fileChooser.getSelectedFile())
				.thenReturn(inputFolder, outputFolder);
			
			PowerMockito.when(pane.createDialog(Mockito.anyString()))
				.thenReturn(dialog);
			
			final String[] args = new String[0];
			KingsDischargeSummaryParser.main(args);
			
			assertExpectedOutput();
		} finally {			
			if (dialog != null) {
				dialog.dispose();
			}
		}
	}
	
	private void assertExpectedOutput() throws Exception {
		assertTrue(outputFolder.isDirectory());
		
		// Check against expectations
		for (final String name: Arrays.asList("Example2", "Example3")) {
			checkJsonOutput(name + ".txt");
		}
		
		// Example.pdf is not a supported type - so there should be no Example.txt in the output
		assertFalse(new File(outputFolder, "Example.txt").exists());
	}
	
	private void checkJsonOutput(final String name) throws Exception {
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
