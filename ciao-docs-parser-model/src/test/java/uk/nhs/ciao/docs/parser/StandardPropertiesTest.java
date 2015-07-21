package uk.nhs.ciao.docs.parser;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.nhs.ciao.docs.parser.StandardProperties.Metadata;

import com.google.common.collect.Maps;

/**
 * Unit tests for {@link StandardProperties}
 */
public class StandardPropertiesTest {
	private Map<String, Object> properties;
	private StandardProperties standardProperties;
	
	@Before
	public void setup() {
		properties = Maps.newLinkedHashMap();
		standardProperties = new StandardProperties(properties);
	}
	
	@Test
	public void testGetCreatesMetadataIfMissing() {
		final Metadata metadata = standardProperties.getMetadata();
		metadata.setContentType("value");
		
		Assert.assertEquals("value", metadata.getContentType());
		Assert.assertNotNull(properties.get(PropertyNames.METADATA));
	}
	
	@Test
	public void testGetReturnsMetadataIfProvided() {
		final Map<String, Object> metadataMap = Maps.newLinkedHashMap();
		properties.put(PropertyNames.METADATA, metadataMap);
		metadataMap.put(PropertyNames.CONTENT_TYPE, "value");
		
		final Metadata metadata = standardProperties.getMetadata();
		Assert.assertEquals("value", metadata.getContentType());
	}
	
	@Test
	public void testGetReturnsNullOnUnexpectedMetadataStructure() {
		properties.put(PropertyNames.METADATA, "should be a map");
		final Metadata metadata = standardProperties.getMetadata();
		Assert.assertNull(metadata.getContentType());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testSetThrowsExceptionOnUnexpectedMetadataStructure() {
		properties.put(PropertyNames.METADATA, "should be a map");
		final Metadata metadata = standardProperties.getMetadata();
		metadata.setContentType("value");
	}
}
