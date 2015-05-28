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
import com.google.common.base.Preconditions;

/**
 * Application to parse and extract property values from discharge summary PDF documents
 */
public class KingsDischargeSummaryParser implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(KingsDischargeSummaryParser.class);
	
	public static void main(final String[] args) throws Exception {
		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("/uk/nhs/ciao/docs/parser/kings/beans.xml");
		
		try {
			if (args.length < 2) {
				runGuiApp(applicationContext);
			} else {
				runConsoleApp(applicationContext, args);
			}
		} finally {
			applicationContext.close();
		}
	}
	
	/**
	 * Runs the parser in console mode - the first two arguments are the 
	 * inputFolder and the outputFolder
	 */
	private static void runConsoleApp(final ApplicationContext applicationContext, final String[] args) throws Exception {
		final File inputFolder = new File(args[0]);
		final File outputFolder = new File(args[1]);
		final Listener listener = new Listener();
		new KingsDischargeSummaryParser(applicationContext, listener, inputFolder, outputFolder).run();
	}
	
	/**
	 * Runs the parser in GUI mode - the inputFolder and outputFolder are determined
	 * via GUI file choosers
	 */
	private static void runGuiApp(final ApplicationContext applicationContext) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		final JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		chooser.setDialogTitle("Select input folder (PDF)");
		if (chooser.showDialog(null, "Select input") == JFileChooser.CANCEL_OPTION) {
			LOGGER.info("Input folder selection was cancelled");
			return;
		}		
		final File inputFolder = chooser.getSelectedFile();
		
		chooser.setDialogTitle("Select output folder (TXT)");
		if (chooser.showDialog(null, "Select output") == JFileChooser.CANCEL_OPTION) {
			LOGGER.info("Output folder selection was cancelled");
			return;
		}
		final File outputFolder = chooser.getSelectedFile();
		
		final Listener listener = new Listener() {
			@Override
			public void completed(final int fileCount, final  File outputFolder) {
				super.completed(fileCount, outputFolder);
				
				final String message = String.format("Parsed %s files - check %s for the output",
						fileCount, outputFolder);
				JOptionPane.showMessageDialog(null, message);
			}
		};
		new KingsDischargeSummaryParser(applicationContext, listener, inputFolder, outputFolder).run();
	}
	
	/**
	 * Listens to events that occur while running a parse
	 * <p>
	 * Default behaviour is to log details of the event
	 */
	public static class Listener {
		public void fileParseStarted(final File file) {
			LOGGER.info("Parsing file: {}", file);
		}
		
		public void completed(final int fileCount, final File outputFolder) {
			LOGGER.info("Parsed {} files - check {} for the output",
					fileCount, outputFolder);
		}
	}
	
	private final Listener listener;
	private final File inputFolder;
	private final File outputFolder;
	private final DocumentParser documentParser;
	private final ObjectMapper objectMapper;
	
	public KingsDischargeSummaryParser(final ApplicationContext applicationContext, final Listener listener,
			final File inputFolder, final File outputFolder) throws ParserConfigurationException {
		this.listener = Preconditions.checkNotNull(listener);
		this.inputFolder = Preconditions.checkNotNull(inputFolder);
		this.outputFolder = Preconditions.checkNotNull(outputFolder);
		
		Preconditions.checkState(inputFolder.isDirectory());
		outputFolder.mkdirs();
		Preconditions.checkState(outputFolder.isDirectory());
		
		documentParser = applicationContext.getBean("documentParser", DocumentParser.class);
		objectMapper = new ObjectMapper();
	}
	
	/**
	 * Runs the parser
	 * <p>
	 * Each file in the input folder is parsed (if possible) and the result is stored
	 * in the output folder
	 */
	@Override
	public void run() {
		int count = 0;
		
		for (final File file: inputFolder.listFiles()) {			
			if (parseInputFile(file)) {
				count++;
			}
		}
		
		listener.completed(count, outputFolder.getAbsoluteFile());
	}
	
	private boolean parseInputFile(final File file) {
		if (!file.isFile() || !file.canRead()) {
			return false;
		}
		
		listener.fileParseStarted(file);
		
		boolean parsedFile = false;
		InputStream in = null;
		
		try {			
			in = new FileInputStream(file);
			final Map<String, Object> properties = documentParser.parseDocument(in);
			
			final String filename = getBaseName(file) + ".txt";
			final File outputFile = new File(outputFolder, filename);				
			writeJsonPropertiesToFile(outputFile, properties);
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
}
