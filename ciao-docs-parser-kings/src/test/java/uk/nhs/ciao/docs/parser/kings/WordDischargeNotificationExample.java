package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;

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
