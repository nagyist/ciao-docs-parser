package uk.nhs.ciao.docs.parser.kings;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests for {@link KingsDischargeSummaryParser} via the main method (console or gui mode)
 * <p>
 * Tests are performed by running some input examples through the parser and checking the extracted
 * JSON content against corresponding expectation documents. The input and expectation documents
 * are on the classpath under the test resources.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(KingsDischargeSummaryParser.class)
@PowerMockIgnore("javax.swing.*")
public class KingsDischargeSummaryParserGUITest extends KingsDischargeSummaryParserTestBase {	
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
			
			runTest();
		} finally {			
			if (dialog != null) {
				dialog.dispose();
			}
		}
	}
}
