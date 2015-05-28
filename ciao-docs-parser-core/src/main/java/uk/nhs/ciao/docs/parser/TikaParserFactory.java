package uk.nhs.ciao.docs.parser;

import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.DefaultParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;

/**
 * Constructs and configures Tika parsers
 */
public final class TikaParserFactory {
	private static final MediaType APPLICATION_PDF = MediaType.application("pdf");
	
	private TikaParserFactory() {
		// Suppress default constructor
	}
	
	/**
	 * Creates a new parser using the default configuration
	 */
	public static Parser createParser() {
		return new AutoDetectParser(createDefaultParser());
	}
	
	/**
	 * Creates a new default parser using the default configuration.
	 * <p>
	 * In particular, a PDFBox parser is added and configured.
	 * 
	 * @see #configurePdfParser(PDFParser)
	 */
	public static DefaultParser createDefaultParser() {
		final DefaultParser defaultParser = new DefaultParser();
		
		/*
		 * Try to configure the default PDFParser
		 * <p>
		 * We cannot just use AutoDetectParser configured with a new PDFParser instance: 
		 * since there are two parsers supporting PDF, ours may not be called - see
		 * http://wiki.apache.org/tika/CompositeParserDiscussion and
		 * https://issues.apache.org/jira/browse/TIKA-1509
		 */
		final Parser pdfParser = defaultParser.getParsers().get(APPLICATION_PDF);
		if (pdfParser instanceof PDFParser) {
			configurePdfParser((PDFParser)pdfParser);
		}
		
		return defaultParser;
	}
	
	/**
	 * Creates and configures a new PDFBox parser
	 * 
	 * @see #configurePdfParser(PDFParser)
	 */
	public static PDFParser createPdfParser() {
		final PDFParser pdfParser = new PDFParser();
		configurePdfParser(pdfParser);
		return pdfParser;
	}
	
	/**
	 * Configures the specified PDFBox parser so that:
	 * <ul>
	 * <li>{@link PDFParserConfig#getSortByPosition() is enabled</li>
	 * </ul>
	 * <p>
	 * A pdf document does not contain structure elements like paragraphs, tables,
	 * headers etc that other documents may have. Enabling the sort by position
	 * algorithm alters the Tika XHTML output so that the text flows in a guaranteed
	 * order from left to right.
	 *
	 * @param pdfParser The parser to configure
	 * @see PDFParserConfig
	 */
	public static void configurePdfParser(final PDFParser pdfParser) {
		final PDFParserConfig parserConfig = pdfParser.getPDFParserConfig();
		parserConfig.setSortByPosition(true);
	}
}
