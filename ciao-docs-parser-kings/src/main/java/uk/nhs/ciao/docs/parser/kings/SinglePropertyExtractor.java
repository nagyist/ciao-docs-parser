package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.util.Whitespace.collapseWhitespace;

import java.util.Map;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.collect.Maps;

public class SinglePropertyExtractor implements PropertiesExtractor<NodeStream> {
	private final String propertyName;
	
	public SinglePropertyExtractor(final String propertyName) {
		this.propertyName = propertyName;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		if (!nodes.hasNext()) {
			return null;
		}
		
		final Map<String, Object> properties = Maps.newHashMap();
		final String text = collapseWhitespace(nodes.take().getTextContent());
		properties.put(propertyName, text);
		return properties;
	}
}