package uk.nhs.ciao.docs.parser.kings;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import uk.nhs.ciao.docs.parser.PropertiesExtractor;

public class WordDischargeNotificationExample {
	public static void main(final String[] args) throws Exception {
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final Document document = builder.parse(WordDischargeNotificationExample.class.getResourceAsStream("example.xml"));
		
//		Thread.sleep(20000);
		
		final PropertiesExtractor<Document> extractor = KingsPropertiesExtractorFactory.createWordDischargeNotificationExtractor();
		
		long start = System.nanoTime();
		final WordDischargeNotificationExample example = new WordDischargeNotificationExample(document, extractor);
		for (int i = 0; i < 1000; i++) {
			example.run();
		}
		System.out.println("Time per extraction: " + ((long)((double)(System.nanoTime() - start)  / (1000000d * 1000))) + " ms");
	}
	
	private final Document document;
	private final PropertiesExtractor<Document> extractor;
	
	public WordDischargeNotificationExample(final Document document, final PropertiesExtractor<Document> extractor) {
		this.document = document;
		this.extractor = extractor;
	}
	
	public void run() throws Exception {
		extractor.extractProperties(document);
	}
}
