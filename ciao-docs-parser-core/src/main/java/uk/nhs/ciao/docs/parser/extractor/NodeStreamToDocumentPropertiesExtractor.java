package uk.nhs.ciao.docs.parser.extractor;

import java.util.Map;

import org.w3c.dom.Document;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

import com.google.common.base.Preconditions;

/**
 * Adaptor to a node stream properties extractor into a document properties extractor
 */
public class NodeStreamToDocumentPropertiesExtractor implements PropertiesExtractor<Document> {
	private PropertiesExtractor<? super NodeStream> propertiesExtractor;
	
	public NodeStreamToDocumentPropertiesExtractor(final PropertiesExtractor<? super NodeStream> propertiesExtractor) {
		this.propertiesExtractor = Preconditions.checkNotNull(propertiesExtractor);
	}
	
	@Override
	public Map<String, Object> extractProperties(final Document document) throws UnsupportedDocumentTypeException {
		final NodeStream nodeStream = document == null ? null : NodeStream.createStream(document.getDocumentElement());
		return propertiesExtractor.extractProperties(nodeStream);
	}
}