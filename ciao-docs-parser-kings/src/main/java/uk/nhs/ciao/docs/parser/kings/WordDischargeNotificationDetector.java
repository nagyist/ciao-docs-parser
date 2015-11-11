package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.extractor.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

/**
 * Extractor to verify that the document contains the expected title for a word discharge notification document
 * <p>
 * This can be used as an initial test to check that the document is a word format discharge document before
 * attempting to parse more detail from it
 */
public class WordDischargeNotificationDetector implements PropertiesExtractor<NodeStream> {
	private final String title = "Discharge Notification";
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes) throws UnsupportedDocumentTypeException {
		if (nodes.remaining() != 1) {
			throw new UnsupportedDocumentTypeException();
		}
		
		final String text = nodes.take().getTextContent();
		if (!title.equals(text)) {
			throw new UnsupportedDocumentTypeException("Expected title: " + title + " - actual text: " + text);
		}
		
		return null;
	}
}