package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.util.Whitespace.collapseWhitespace;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PropertyTableExtractor implements PropertiesExtractor<NodeStream> {
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		
		String name = null;
		String value = null;
		List<String> values = null;
		
		while (nodes.hasNext()) {
			final Node node = nodes.take();
			if (node instanceof Element) {
				final String text = collapseWhitespace(node.getTextContent());
				if (text.length() > 1 && text.endsWith(":")) {
					if (name != null && value != null) {
						properties.put(name, value);
					} else if (name != null && values != null) {
						properties.put(name, values);
					}
					
					name = text.substring(0, text.length() - 1);
					value = null;
					values = null;
				} else if (!text.isEmpty()) {
					if (value == null) {
						value = text;
					} else if (values == null) {
						values = Lists.newArrayList(value, text);
						value = null;
					} else {
						values.add(text);
					}
				}
			}
		}
		
		if (name != null && value != null) {
			properties.put(name, value);
		} else if (name != null && values != null) {
			properties.put(name, values);
		}
		
		return properties;
	}
}