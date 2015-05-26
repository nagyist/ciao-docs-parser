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
public class TikaParserFactory {
	private static final MediaType APPLICATION_PDF = MediaType.application("pdf");
	
	private TikaParserFactory() {
		// Suppress default constructor
	}
	
	public static Parser createParser() {
		return new AutoDetectParser(createDefaultParser());
	}
	
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
	
	public static PDFParser createPdfParser() {
		final PDFParser pdfParser = new PDFParser();
		configurePdfParser(pdfParser);
		return pdfParser;
	}
	
	public static void configurePdfParser(final PDFParser pdfParser) {
		final PDFParserConfig parserConfig = pdfParser.getPDFParserConfig();
		parserConfig.setSortByPosition(true);
	}
}
