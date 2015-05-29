package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link SAXContentToDOMHandler}
 */
public class SAXContentToDOMHandlerTest {
	private SAXContentToDOMHandler handler;
	
	private String uri;
	private Attributes attributes;
	private String whitespace;
	
	@Before
	public void setup() throws ParserConfigurationException {
		final boolean whitespaceNormalisationEnabled = true;
		initHandler(whitespaceNormalisationEnabled);
		
		uri = "some-string";
		attributes = mock(Attributes.class);
		whitespace = "  \t  \n ";
	}
	
	private void initHandler(final boolean whitespaceNormalisationEnabled) throws ParserConfigurationException {
		final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();		
		handler = new SAXContentToDOMHandler(documentBuilder, whitespaceNormalisationEnabled);
	}
	
	@Test
	public void testEmptyDocument() throws Exception {
		handler.startDocument();
		handler.endDocument();
		
		final Document document = handler.getDocument();
		assertNotNull(document);
		assertEquals(0, document.getChildNodes().getLength());
	}
	
	@Test
	public void testMultipleElements() throws Exception {
		handler.startDocument();
		startElement("html");
		startElement("p");
		endElement("p");
		endElement("html");
		handler.endDocument();
		
		final Document document = handler.getDocument();
		assertNotNull(document);
		final Element root = document.getDocumentElement();
		assertEquals("html", root.getTagName());
		assertEquals(1, root.getChildNodes().getLength());
		
		final Element child = (Element)root.getChildNodes().item(0);
		assertEquals("p", child.getTagName());
	}
	
	@Test
	public void testWhitespaceNodesAreCombined() throws Exception {
		handler.startDocument();
		startElement("html");
		characters("   start");
		characters(whitespace);
		characters(whitespace);
		characters("end   ");
		endElement("html");
		handler.endDocument();
		
		final Document document = handler.getDocument();
		assertNotNull(document);
		
		final Element root = document.getDocumentElement();
		assertNotNull(root);
		
		assertEquals(1, root.getChildNodes().getLength());
		final Text node = (Text)root.getChildNodes().item(0);
		
		final String expected = "start" + whitespace + whitespace + "end";
		assertEquals(expected, node.getTextContent());
	}
	
	private void characters(final String value) throws SAXException {
		handler.characters(value.toCharArray(), 0, value.length());
	}
	
	private void startElement(final String name) throws SAXException {
		handler.startElement(uri, name, name, attributes);
	}
	
	private void endElement(final String name) throws SAXException {
		handler.endElement(uri, name, name);
	}
}
