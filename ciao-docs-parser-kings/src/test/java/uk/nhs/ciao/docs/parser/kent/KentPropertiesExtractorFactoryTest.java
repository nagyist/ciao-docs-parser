package uk.nhs.ciao.docs.parser.kent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.w3c.dom.Document;

import uk.nhs.ciao.docs.parser.DocumentParser;
import uk.nhs.ciao.docs.parser.TikaDocumentParser;
import uk.nhs.ciao.docs.parser.TikaParserFactory;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.extractor.PropertiesExtractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

/**
 * Tests for extractors created by {@link KentPropertiesExtractorFactory}
 */
public class KentPropertiesExtractorFactoryTest {
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String,Object>>() {};
	private PropertiesCaptor captor;
	private DocumentParser parser; 
	private ObjectMapper objectMapper;
	
	@Before
	public void setup() throws Exception {
		captor = new PropertiesCaptor();
		parser = new TikaDocumentParser(TikaParserFactory.createParser(), captor);
		objectMapper = new ObjectMapper();
	}
	
	@Test
	public void testHtmlExample6() throws Exception {
		parseAndAssert("Example6.htm", "Example6.txt");
	}
	
	@Test
	public void testHtmlExample7() throws Exception {
		parseAndAssert("Example7.htm", "Example7.txt");
	}
	
	@Test
	public void testHtmlExample8() throws Exception {
		parseAndAssert("Example8.htm", "Example8.txt");
	}
	
	@Test
	public void testHtmlExample9() throws Exception {
		parseAndAssert("Example9.htm", "Example9.txt");
	}
	
	@Test
	public void testHtmlExample10() throws Exception {
		parseAndAssert("Example10.htm", "Example10.txt");
	}
	
	private void parseAndAssert(final String inputName, final String expectedName) throws Exception {
		final Map<String, Object> expected = loadResource("./expected/" + expectedName);
		final Map<String, Object> actual = parseResource("../kings/input/" + inputName);	
		ReflectionAssert.assertReflectionEquals(expected, actual);
	}
	
	private Map<String, Object> parseResource(final String resourceName) throws UnsupportedDocumentTypeException, IOException {
		final InputStream in = getClass().getResourceAsStream(resourceName);
		try {
			parser.parseDocument(in);
			return captor.properties;
		} finally {
			Closeables.closeQuietly(in);
		}
	}
	
	private Map<String, Object> loadResource(final String resourceName) throws IOException {
		final InputStream in = getClass().getResourceAsStream(resourceName);
		try {
			return objectMapper.readValue(in, MAP_TYPE);
		} finally {
			Closeables.closeQuietly(in);
		}
	}
	
	private static class PropertiesCaptor implements PropertiesExtractor<Document> {
		private final PropertiesExtractor<Document> extractor;
		private Map<String, Object> properties;
		
		public PropertiesCaptor() throws XPathExpressionException {
			extractor = KentPropertiesExtractorFactory.createEDNExtractor();
		}
		
		@Override
		public Map<String, Object> extractProperties(final Document document)
				throws UnsupportedDocumentTypeException {
			properties = extractor.extractProperties(document);
			// We want to capture the properties untouched by later processing
			// so return a new empty instance
			return Maps.newLinkedHashMap();
		}
	}
}
