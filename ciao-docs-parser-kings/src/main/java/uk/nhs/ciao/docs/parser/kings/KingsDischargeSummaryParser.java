package uk.nhs.ciao.docs.parser.kings;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.nhs.ciao.docs.parser.DocumentParser;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Application to parse and extract property values from discharge summary PDF documents
 */
public abstract class KingsDischargeSummaryParser implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(KingsDischargeSummaryParser.class);
	
	/**
	 * Runs the parser
	 * <p>
	 * With two or more arguments (inputFolder, outputFolder), the parser is run in Console mode.
	 * Otherwise the parser is run in GUI mode using folder choosers
	 */
	public static void main(final String[] args) throws Exception {
		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("/uk/nhs/ciao/docs/parser/kings/beans.xml");
		
		try {
			final KingsDischargeSummaryParser parser;
			if (args.length < 2) {
				parser = new GUI(applicationContext);
			} else {
				parser = new Console(applicationContext, args);
			}
			parser.run();
		} finally {
			applicationContext.close();
		}
	}
	
	private final DocumentParser documentParser;
	private final ObjectMapper objectMapper;
	
	public KingsDischargeSummaryParser(final ApplicationContext applicationContext) throws ParserConfigurationException {
		documentParser = applicationContext.getBean("documentParser", DocumentParser.class);
		objectMapper = new ObjectMapper();
	}
	
	protected abstract File getInputFolder();
	protected abstract File getOutputFolder();
	
	/**
	 * Runs the parser
	 * <p>
	 * Each file in the input folder is parsed (if possible) and the result is stored
	 * in the output folder
	 */
	@Override
	public void run() {
		int count = 0;
		
		final File inputFolder = getInputFolder();
		if (!isValidFolder(inputFolder)) {
			return;
		}
		
		final File outputFolder = getOutputFolder();
		if (!isValidFolder(outputFolder)) {
			return;
		}
		
		for (final File file: inputFolder.listFiles()) {			
			if (parseInputFile(file, outputFolder)) {
				count++;
			}
		}
		
		completed(count, outputFolder.getAbsoluteFile());
	}
	
	private boolean isValidFolder(final File folder) {
		if (folder == null) {
			return false;
		} else if (!folder.exists()) {
			folder.mkdirs();
		}
		
		return folder.isDirectory();
	}
	
	// listener methods
	
	protected void fileParseStarted(final File file) {
		LOGGER.info("Parsing file: {}", file);
	}
	
	protected void completed(final int fileCount, final File outputFolder) {
		LOGGER.info("Parsed {} files - check {} for the output",
				fileCount, outputFolder);
	}
	
	private boolean parseInputFile(final File file, final File outputFolder) {
		if (!file.isFile() || !file.canRead()) {
			return false;
		}
		
		fileParseStarted(file);
		
		boolean parsedFile = false;
		InputStream in = null;
		
		try {			
			in = new FileInputStream(file);
			final Map<String, Object> properties = documentParser.parseDocument(in);
			
			final String filename = getBaseName(file) + ".txt";
			final File outputFile = new File(outputFolder, filename);				
			parsedFile = writeJsonPropertiesToFile(outputFile, properties);
		} catch (UnsupportedDocumentTypeException e) {
			LOGGER.warn("Unsupported document type: {}", file, e);
		} catch (IOException e) {
			LOGGER.warn("Unable to parse file: {}", file, e);
		} finally {
			closeQuietly(in);			
		}
		
		return parsedFile;
	}
	
	private boolean writeJsonPropertiesToFile(final File outputFile, final Map<String, Object> properties) {
		boolean result = false;
		OutputStream out = null;
		try {					
			out = new FileOutputStream(outputFile);
			objectMapper.writeValue(out, properties);
			out.flush();
			result = true;
		} catch (IOException e) {
			LOGGER.warn("Unable to write to output file {}", outputFile, e);
		} finally {
			closeQuietly(out);
		}
		
		return result;
	}
	
	/**
	 * Gets the 'base' name of the file (i.e. without the extension)
	 * <p>
	 * If a filename contains multiple extensions (e.g. abc.txt.zip)
	 * only the final extension is removed.
	 */
	private String getBaseName(final File file) {
		final String fullName = file.getName();
		final int index = fullName.lastIndexOf('.');
		return index < 0 ? fullName : fullName.substring(0, index);
	}
	
	private static void closeQuietly(final Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOGGER.debug("IOException while closing resource", e);
			}
		}
	}
	
	public static class Console extends KingsDischargeSummaryParser {
		private final File inputFolder;
		private final File outputFolder;
		
		public Console(final ApplicationContext applicationContext, final String[] args) throws ParserConfigurationException {
			super(applicationContext);
			
			this.inputFolder = new File(args[0]);
			this.outputFolder = new File(args[1]);
		}
		
		@Override
		protected File getInputFolder() {
			return inputFolder;
		}
		
		@Override
		protected File getOutputFolder() {
			return outputFolder;
		}
	}
	
	public static class GUI extends KingsDischargeSummaryParser {
		public GUI(final ApplicationContext applicationContext) throws Exception {
			super(applicationContext);
			
			initLookAndFeel();
		}
		
		@Override
		protected File getInputFolder() {
			final File inputFolder = chooseFolder("Select input folder (PDF)", "Select input");
			if (inputFolder == null) {
				LOGGER.info("Input folder selection was cancelled");
			}
			return inputFolder;
		}
		
		@Override
		protected File getOutputFolder() {
			final File outputFolder = chooseFolder("Select output folder (TXT)", "Select output");
			if (outputFolder == null) {
				LOGGER.info("Output folder selection was cancelled");
			}
			return outputFolder;
		}
		
		@Override
		public void completed(final int fileCount, final  File outputFolder) {
			super.completed(fileCount, outputFolder);
			
			final String message = String.format("Parsed %s files - check %s for the output",
					fileCount, outputFolder);
			showDialog("Parsing complete", message);
		}
		
		protected void initLookAndFeel() throws Exception {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		
		protected File chooseFolder(final String title, final String buttonText) {
			final JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			chooser.setDialogTitle(title);
			if (chooser.showDialog(null, buttonText) == JFileChooser.CANCEL_OPTION) {
				return null;
			}
			
			return chooser.getSelectedFile();
		}
		
		/**
		 * Shows a GUI message dialog
		 */
		protected void showDialog(final String title, final String message) {
			JOptionPane.showMessageDialog(null, title, message, JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
