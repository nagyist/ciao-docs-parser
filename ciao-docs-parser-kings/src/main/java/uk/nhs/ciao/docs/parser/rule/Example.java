package uk.nhs.ciao.docs.parser.rule;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.StandardProperties;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;

public class Example {
	public static void main(final String[] args) throws Exception {
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final Document document = builder.parse(Example.class.getResourceAsStream("example.xml"));
		
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final SplitterPropertiesExtractor splitter = new SplitterPropertiesExtractor();
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/head/meta"),  metadataExtractor());

		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/p[position()=1]"),
				singlePropertyExtractor("hospitalAddress"));
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[position()=2]/*/tr/td/p"),
				propertyTableExtractor());
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[position()=3]/*/tr/td/p"),
				propertyTableExtractor());
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[count(preceding::b[text()='Consultant follow up:']) = 1 and count(following::b[text()='Discharge Medication']) = 1]/*/tr"),
				objectTableExtractor(xpath, "allergens"));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[count(preceding::b[text()='Discharge Medication']) = 1 and count(following::b[text()='Prescriber:']) = 1]/*/tr"),
				objectTableExtractor(xpath, "dischargeMedication"));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::b[text()='Prescriber:']]/*/tr/td/p"),
				propertyTableExtractor());
		
		System.out.println(splitter.extractProperties(NodeStream.createStream(document.getDocumentElement())));
	}
	
	private static PropertiesExtractor<NodeStream> metadataExtractor() {
		return new PropertiesExtractor<NodeStream>() {
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
		};
	}
	
	private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");
	private static String normaliseWhitespace(final String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		
		return MULTIPLE_WHITESPACE.matcher(text).replaceAll(" ").trim();
	}
	
	private static PropertiesExtractor<NodeStream> singlePropertyExtractor(final String name)  {
		return new PropertiesExtractor<NodeStream>() {
			@Override
			public Map<String, Object> extractProperties(final NodeStream nodes)
					throws UnsupportedDocumentTypeException {
				if (!nodes.hasNext()) {
					return null;
				}
				
				final Map<String, Object> properties = Maps.newHashMap();
				final String text = normaliseWhitespace(nodes.take().getTextContent());
				properties.put(name, text);
				return properties;
			}
		};
	}
	
	private static PropertiesExtractor<NodeStream> propertyTableExtractor() {
		return new PropertiesExtractor<NodeStream>() {
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
						final String text = normaliseWhitespace(node.getTextContent());
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
		};
	}
	
	private static PropertiesExtractor<NodeStream> objectTableExtractor(final XPath xpath, final String property) throws XPathExpressionException {
		final XPathExpression expression = xpath.compile("./td/p");
		
		return new PropertiesExtractor<NodeStream>() {
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
							final String value = normaliseWhitespace(nodeList.item(index).getTextContent());

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
				properties.put(property, objects);
				return properties;
			}
		};
	}
}
