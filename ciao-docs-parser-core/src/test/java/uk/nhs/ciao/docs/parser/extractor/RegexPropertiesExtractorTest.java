package uk.nhs.ciao.docs.parser.extractor;

import static org.junit.Assert.*;
import static uk.nhs.ciao.docs.parser.extractor.RegexPropertyFinder.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.extractor.RegexPropertiesExtractor;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

public class RegexPropertiesExtractorTest {
	private RegexPropertiesExtractor extractor;
	
	private Document loadDocument(final String name) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = RegexPropertiesExtractorTest.class.getResourceAsStream(name);
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(new InputSource(inputStream));			
		} finally {
			Closeables.closeQuietly(inputStream);
		}
	}
	
	@Test
	public void testFullExtraction() throws Exception {
		extractor = new RegexPropertiesExtractor();
		extractor.addPropertyFinder(
				builder("first-property").to("second-property").build());
		extractor.addPropertyFinders(
				builder("second-property").to("third-property").build());
		extractor.addPropertyFinders(Arrays.asList(
				builder("third-property").to("last-property").build(),
				builder("last-property").to("token!").build()));
		
		final Map<String, Object> expected = Maps.newHashMap();
		expected.put("first-property", "property1-value");
		expected.put("second-property", "has a value");
		expected.put("third-property", "value 3");
		expected.put("last-property", "some value");
		
		final Document document = loadDocument("regex-test-1.html");
		final Map<String, Object> actual = extractor.extractProperties(document);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testExtractionWithTextFilter() throws Exception {
		extractor = new RegexPropertiesExtractor(
				builder("first-property").to("second-property").build(),
				builder("second-property").to("third-property").build(),
				builder("third-property").to("last-property").build(),
				builder("last-property").build()); // no ending tag - rely on text filter

		extractor.setTextFilter("second-property", "last-property");
		final Map<String, Object> expected = Maps.newHashMap();
		expected.put("second-property", "has a value");
		expected.put("third-property", "value 3");
		expected.put("last-property", "some value");
		
		final Document document = loadDocument("regex-test-1.html");
		final Map<String, Object> actual = extractor.extractProperties(document);
		assertEquals(expected, actual);
	}

	@Test(expected=UnsupportedDocumentTypeException.class)
	public void whenNotExtractorsAreRegisteredThenUnsupportedDocumentTypeShouldBeThrow() throws Exception {
		extractor = new RegexPropertiesExtractor();
		
		final Document document = loadDocument("regex-test-1.html");
		extractor.extractProperties(document);
	}
}
