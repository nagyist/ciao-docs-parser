package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * Tests to check that {@link ParsedDocument} can be serialized/deserialized by jackson
 */
public class ParsedDocumentTest {
	private ObjectMapper objectMapper;
	
	@Before
	public void setup() {
		this.objectMapper = new ObjectMapper();
	}

	@Test
	public void testRoundtrip() throws Exception {
		final Map<String, Object> properties = Maps.newLinkedHashMap();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		
		final Document originalDocument = new Document("somedoc.pdf", new byte[]{1, 2, 3, 4});
		originalDocument.setMediaType("application/xml");
		
		final ParsedDocument expected = new ParsedDocument(originalDocument, properties);		
		final String json = objectMapper.writeValueAsString(expected);
		final ParsedDocument actual = objectMapper.readValue(json, ParsedDocument.class);
		
		ReflectionAssert.assertReflectionEquals(expected, actual, ReflectionComparatorMode.LENIENT_ORDER);
	}

}
