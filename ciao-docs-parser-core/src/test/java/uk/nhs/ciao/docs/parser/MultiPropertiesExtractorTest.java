package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;

/**
 * Unit tests for {@link MultiPropertiesExtractor}
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiPropertiesExtractorTest {
	private MultiPropertiesExtractor<String> extractor;
	
	@Mock
	private PropertiesExtractor<String> delegate1;
	
	@Mock
	private PropertiesExtractor<String> delegate2;

	@Mock
	private PropertiesExtractor<String> delegate3;
	
	@Before
	public void setup() {
		extractor = new MultiPropertiesExtractor<String>();
		extractor.addExtractor(delegate1);
		extractor.addExtractor(delegate2);
		extractor.addExtractor(delegate3);
	}
	
	@Test(expected=UnsupportedDocumentTypeException.class)
	public void whenNoExtractorsAreRegisteredThenUnsupportedDocumentTypeShouldBeThrown() throws UnsupportedDocumentTypeException {
		extractor = new MultiPropertiesExtractor<String>();
		extractor.extractProperties("document text");
	}
	
	@Test
	public void whenAnExtractorSucceedsThenNoFurtherExtractorsShouldBeTried() throws UnsupportedDocumentTypeException {
		final Map<String, Object> properties = Maps.newHashMap();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		
		when(delegate1.extractProperties(anyString())).thenReturn(properties);
		
		final Map<String, Object> actual = extractor.extractProperties("document text");
		assertEquals(properties, actual);
		
		verifyZeroInteractions(delegate2, delegate3);
	}
	
	@Test
	public void whenAllExtractorsFailThenUnsupportedDocumentTypeShouldBeThrow() throws UnsupportedDocumentTypeException {
		setMockToFail(delegate1);
		setMockToFail(delegate2);
		setMockToFail(delegate3);
		
		try {
			extractor.extractProperties("document text");
			fail("UnsupportedDocumentTypeException should have been thrown");
		} catch (UnsupportedDocumentTypeException e) {
			// Verify that all extractors were tried
			verify(delegate1).extractProperties(anyString());
			verify(delegate2).extractProperties(anyString());
			verify(delegate3).extractProperties(anyString());
		}
	}
	
	private void setMockToFail(final PropertiesExtractor<String> delegate) throws UnsupportedDocumentTypeException {
		when(delegate.extractProperties(anyString())).thenThrow(new UnsupportedDocumentTypeException());
	}
}
