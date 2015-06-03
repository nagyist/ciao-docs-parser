package uk.nhs.ciao.docs.parser.kings;

import org.junit.Test;

/**
 * Tests for {@link KingsDischargeSummaryParser} via the main method (console or gui mode)
 * <p>
 * Tests are performed by running some input examples through the parser and checking the extracted
 * JSON content against corresponding expectation documents. The input and expectation documents
 * are on the classpath under the test resources.
 */
public class KingsDischargeSummaryParserConsoleTest extends KingsDischargeSummaryParserTestBase {	
	@Test
	public void testPdfExamplesWithConsole() throws Exception {
		runMainTest(inputFolder.getPath(), outputFolder.getPath());
	}
}
