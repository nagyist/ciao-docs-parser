package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.util.Whitespace.collapseAndTrimWhitespace;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ObjectTableExtractor implements PropertiesExtractor<NodeStream> {
	private final XPathExpression expression;
	private final String propertyName;
	
	public ObjectTableExtractor(final XPath xpath, final String propertyName) throws XPathExpressionException {
		this.expression = xpath.compile("./td/p");
		this.propertyName = propertyName;
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final List<String> names = Lists.newArrayList();
		final List<Map<String, Object>> objects = Lists.newArrayList();
		boolean addedNonEmptyName = false;
		
		while (nodes.hasNext()) {
			final Node node = nodes.take();
			
			boolean addingNames = names.isEmpty();					
			try {
				final NodeList nodeList = (NodeList)expression.evaluate(node, XPathConstants.NODESET);
				Map<String, Object> object = null;
				
				for (int index = 0; index < nodeList.getLength(); index++) {
					final String value = collapseWhitespaceAndTrim(nodeList.item(index).getTextContent());

					if (addingNames) {
						names.add(value);
						if (!Strings.isNullOrEmpty(value)) {
							addedNonEmptyName = true;
						}
					} else if (names.size() > index) {
						final String name = names.get(index);
						if (!Strings.isNullOrEmpty(name)) {
							if (object == null) {
								object = Maps.newLinkedHashMap();
							}
							object.put(name, Strings.nullToEmpty(value));
						}
					}
				}
				
				if (object != null) {
					objects.add(object);
				}
				
				if (!addedNonEmptyName) {
					names.clear();
				}
			} catch (XPathExpressionException e) {
				throw Throwables.propagate(e);
			}
		}
		
		if (objects.isEmpty()) {
			return null;
		}
		
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		properties.put(propertyName, objects);
		return properties;
	}
}