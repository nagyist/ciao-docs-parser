package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.w3c.dom.Document;

import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.SAXContentToDOMHandler;
import uk.nhs.ciao.docs.parser.TikaParserFactory;

public class WordDischargeNotificationExample {
	public static void main(final String[] args) throws Exception {
		final Parser parser = TikaParserFactory.createParser();

		final SAXContentToDOMHandler handler = new SAXContentToDOMHandler(
				DocumentBuilderFactory.newInstance().newDocumentBuilder(), true);
		parser.parse(WordDischargeNotificationExample.class.getResourceAsStream("input/Example4.docx"),
				handler, new Metadata(), new ParseContext());
		
		final Document document = handler.getDocument();
		
//		Thread.sleep(20000);
		
		final PropertiesExtractor<Document> extractor = KingsPropertiesExtractorFactory.createWordDischargeNotificationExtractor();
		
		final int iterations = 1;//000;
		long start = System.nanoTime();
		final WordDischargeNotificationExample example = new WordDischargeNotificationExample(document, extractor);
		for (int i = 0; i < iterations; i++) {
//			example.run();
			System.out.println(example.run());
		}
		System.out.println("Time per extraction: " + ((long)((double)(System.nanoTime() - start)  / (1000000d * iterations))) + " ms");
	}
	
	private final Document document;
	private final PropertiesExtractor<Document> extractor;
	
	public WordDischargeNotificationExample(final Document document, final PropertiesExtractor<Document> extractor) {
		this.document = document;
		this.extractor = extractor;
	}
	
	public Map<String, Object> run() throws Exception {
		return extractor.extractProperties(document);
	}
}
