package uk.nhs.ciao.docs.parser.kings;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.nhs.ciao.docs.parser.kings.KingsDischargeSummaryParser.GUI;

/**
 * Tests for {@link KingsDischargeSummaryParser} via the main method (console or gui mode)
 * <p>
 * Tests are performed by running some input examples through the parser and checking the extracted
 * JSON content against corresponding expectation documents. The input and expectation documents
 * are on the classpath under the test resources.
 */
public class KingsDischargeSummaryParserGUITest extends KingsDischargeSummaryParserTestBase {	
	@Test
	public void testPdfExamplesWithGUI() throws Exception {		
		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("/uk/nhs/ciao/docs/parser/kings/beans.xml");
		try {
			final StubbedGUI parser = new StubbedGUI(applicationContext, inputFolder, outputFolder);
			runTest(parser);
		} finally {
			applicationContext.close();
		}
	}
	
	private static class StubbedGUI extends GUI {
		private Iterator<File> folders;
		
		public StubbedGUI(final ApplicationContext applicationContext, final File... folders) throws Exception {
			super(applicationContext);
			this.folders = Arrays.asList(folders).iterator();
		}
		
		@Override
		protected void initLookAndFeel() throws Exception {
			// NOOP
		}
		
		@Override
		protected void showDialog(final String title, final String message) {
			// NOOP
		}
		
		// bypass swing components
		
		@Override
		protected File chooseFolder(final String title, final String buttonText) {
			return folders.hasNext() ? folders.next() : null;
		}
	}
}
