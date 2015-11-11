package uk.nhs.ciao.docs.parser.extractor;

import static uk.nhs.ciao.util.Whitespace.collapseWhitespaceAndTrim;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.xml.NodeStream;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Property extractor which returns a property containing a single object or list of objects
 * dynamically determined from a table.
 * <p>
 * The incoming node selection is treated as the table rows. Columns are selected from
 * the rows using the specified XPath expression.
 * <p>
 * The first row is used to determine the property names - subsequent rows determine the
 * object values. So two rows results in a single object, three or more rows results in a list
 * of objects.
 * <p>
 * Each object contains the same set of names/keys. If no corresponding column is found - an
 * empty string is used for the value.
 */
public class ObjectTableExtractor implements PropertiesExtractor<NodeStream> {
	private final XPathExpression expression;
	private final String propertyName;
	
	public ObjectTableExtractor(final XPath xpath, final String expression, final String propertyName) throws XPathExpressionException {
		this(xpath.compile(expression), propertyName);
	}
	
	public ObjectTableExtractor(final XPathExpression expression, final String propertyName) {
		this.expression = Preconditions.checkNotNull(expression);
		this.propertyName = Preconditions.checkNotNull(propertyName);
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