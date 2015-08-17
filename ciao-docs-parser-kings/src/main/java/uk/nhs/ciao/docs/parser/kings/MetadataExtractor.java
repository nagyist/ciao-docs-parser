package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.StandardProperties;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class MetadataExtractor implements PropertiesExtractor<NodeStream> {
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		final StandardProperties standardProperties = new StandardProperties(properties);
		
		while (nodes.hasNext()) {
			final Node node = nodes.take();
			if (node instanceof Element) {
				final Element element = (Element)node;
				
				final String name = element.getAttribute("name");
				final String value = element.getAttribute("content");
				
				if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(value)) {
					standardProperties.getMetadata().set(name, value);
				}
			}
		}
		
		return properties;
	}
}