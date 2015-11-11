package uk.nhs.ciao.docs.parser.extractor;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

/**
 * Extractor for properties where the key is defined in one table and the value in another
 * <p>
 * The extractor accepts two parallel selectors. If there are more key rows than value rows
 * the extra properties are added with blank values.
 */
public class PropertySplitTableExtractor implements PropertiesExtractor<NodeStream> {
	private final XPathExpression nameExpression;
	private final XPathExpression valueExpression;
	private final WhitespaceMode whitespaceMode;
	private final PropertyNameDetector propertyNameDetector;
	
	public PropertySplitTableExtractor(final XPath xpath, final String nameExpression,
			final String valueExpression) throws XPathExpressionException {
		this(xpath, nameExpression, valueExpression, WhitespaceMode.COLLAPSE_AND_TRIM);
	}
	
	public PropertySplitTableExtractor(final XPath xpath, final String nameExpression,
			final String valueExpression, final WhitespaceMode whitespaceMode) throws XPathExpressionException {
		this(xpath.compile(nameExpression), xpath.compile(valueExpression), whitespaceMode);
	}

	public PropertySplitTableExtractor(final XPathExpression nameExpression,
			final XPathExpression valueExpression, final WhitespaceMode whitespaceMode) {
		this(nameExpression, valueExpression, whitespaceMode, PropertyNameDetector.ENDS_WITH_COLON);
	}
	
	/**
	 * @param keyExpression The xpath expression used to find property name nodes
	 * @param valueExpression The xpath expression used to find property value nodes
	 */
	public PropertySplitTableExtractor(final XPathExpression nameExpression,
			final XPathExpression valueExpression, final WhitespaceMode whitespaceMode,
			final PropertyNameDetector propertyNameDetector) {
		this.nameExpression = Preconditions.checkNotNull(nameExpression);
		this.valueExpression = Preconditions.checkNotNull(valueExpression);
		this.whitespaceMode = Preconditions.checkNotNull(whitespaceMode);
		this.propertyNameDetector = Preconditions.checkNotNull(propertyNameDetector);
	}
	
	@Override
	public Map<String, Object> extractProperties(final NodeStream nodes)
			throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		
		while (nodes.hasNext()) {
			try {
				findProperties(properties, nodes.take());
			} catch (XPathExpressionException e) {
				Throwables.propagate(e);
			}
		}
		
		return properties;
	}
	
	private void findProperties(final Map<String, Object> properties, final Node root) throws XPathExpressionException {
		if (!(root instanceof Element)) {
			return;
		}
		
		final NodeList nameNodes = (NodeList)nameExpression.evaluate(root, XPathConstants.NODESET);
		final NodeList valueNodes = (NodeList)valueExpression.evaluate(root, XPathConstants.NODESET);

		for (int index = 0; index < nameNodes.getLength(); index++) {
			String name = Strings.nullToEmpty(nameNodes.item(index).getTextContent()).trim();
			if (propertyNameDetector.isPropertyName(name)) {
				name = propertyNameDetector.getPropertyName(name);
			}
			name = whitespaceMode.normalizeWhitespace(name);
			
			if (name.isEmpty()) {
				continue;
			}
			
			String value = "";
			if (index < valueNodes.getLength()) {
				final Node valueNode = valueNodes.item(index);
				value = whitespaceMode.normalizeWhitespace(valueNode.getTextContent());
			}
			properties.put(name, value);
		}
	}
}
