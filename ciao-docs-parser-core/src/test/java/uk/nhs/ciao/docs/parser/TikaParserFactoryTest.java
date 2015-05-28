package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;

import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.junit.Test;

/**
 * Added unit tests for {@link TikaParserFactory}
 */
public class TikaParserFactoryTest {
	
	@Test
	public void whenDefaultParserIsCreatedThenItShouldContainAPdfParser() {
		for (final Parser parser: TikaParserFactory.createDefaultParser().getAllComponentParsers()) {
			if (parser instanceof PDFParser) {
				assertSortByPositionIsEnabled((PDFParser)parser);
				return;
			}
		}
		
		fail("Did not contain an instance of PDFParser");
	}

	@Test
	public void whenPdfParserIsCreatedThenSortByPositionShouldBeEnabled() {
		assertSortByPositionIsEnabled(TikaParserFactory.createPdfParser());
		
		
	}

	private void assertSortByPositionIsEnabled(final PDFParser parser) {
		assertTrue(parser.getPDFParserConfig().getSortByPosition());
	}
}
