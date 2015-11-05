package uk.nhs.ciao.docs.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.nhs.ciao.util.SimpleEntry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

/**
 * Unit tests for {@link PropertySelector}
 */
public class PropertySelectorTest {
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String,Object>>() {};
	private Map<String, Object> properties;
	
	@Before
	public void setup() throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final InputStream inputStream = getClass().getResourceAsStream("property-selector-fixture.json");
		try {
			properties = objectMapper.readValue(inputStream, MAP_TYPE);
		} finally {
			Closeables.closeQuietly(inputStream);
		}
	}
	
	@Test
	public void testSimpleKey() {
		final PropertySelector selector = PropertySelector.valueOf("name");
		final Object actual = selector.selectValue(properties);
		Assert.assertEquals("Mr Example", actual);
		
		final Entry<String, Object> entry = selector.select(properties);
		Assert.assertEquals(SimpleEntry.valueOf("name", "Mr Example"), entry);
		Assert.assertEquals(1, selector.selectAll(properties).size());
	}
	
	@Test
	public void testSimpleKeyWithType() {
		final PropertySelector selector = PropertySelector.valueOf("name");

		final String value = selector.selectValue(String.class, properties);
		Assert.assertEquals("Mr Example", value);
		
		final Entry<String, String> entry = selector.select(String.class, properties);
		Assert.assertEquals(SimpleEntry.valueOf("name", "Mr Example"), entry);
		Assert.assertEquals(1, selector.selectAll(String.class, properties).size());
		
		Assert.assertNull(selector.selectValue(Long.class, properties));
	}
	
	@Test
	public void testIndexedKey() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[0]");
		
		final Object value = selector.selectValue(properties);
		Assert.assertEquals(getFirstAddress(), value);
		
		final Entry<String, Object> entry = selector.select(properties);
		Assert.assertEquals(SimpleEntry.valueOf("addresses[0]", getFirstAddress()), entry);
		Assert.assertEquals(1, selector.selectAll(properties).size());
	}
	
	@Test
	public void testIndexedKeyWithType() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[0]");
		
		final Object value = selector.selectValue(Map.class, properties);
		Assert.assertEquals(getFirstAddress(), value);
		
		final Entry<String, ?> entry = selector.select(Map.class, properties);
		Assert.assertEquals(SimpleEntry.valueOf("addresses[0]", getFirstAddress()), entry);
		Assert.assertEquals(1, selector.selectAll(Map.class, properties).size());
		
		Assert.assertNull(selector.selectValue(Long.class, properties));
		
		Assert.assertFalse(selector.isMulti());
		Assert.assertFalse(selector.isRoot());
	}
	
	@Test
	public void testWildcardIndex() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[*].city");
		
		final Object value = selector.selectValue(properties);
		Assert.assertEquals("London", value);
		
		final Collection<Object> values = selector.selectAllValues(properties);
		Assert.assertEquals(Arrays.asList("London", "Oxford"), Lists.newArrayList(values));
	}
	
	@Test
	public void testWildcardIndexWithType() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[*].city");
		
		final String value = selector.selectValue(String.class, properties);
		Assert.assertEquals("London", value);
		
		final Collection<String> values = selector.selectAllValues(String.class, properties);
		Assert.assertEquals(Arrays.asList("London", "Oxford"), Lists.newArrayList(values));
		
		final Map<String, String> expectedEntries = Maps.newLinkedHashMap();
		expectedEntries.put("addresses[0].city", "London");
		expectedEntries.put("addresses[1].city", "Oxford");
		
		Assert.assertEquals(expectedEntries, selector.selectAll(String.class, properties));
		
		Assert.assertEquals("addresses[*].city", selector.getPath());
		Assert.assertNull(selector.selectValue(Long.class, properties));
		Assert.assertTrue(selector.selectAllValues(Long.class, properties).isEmpty());
	}
	
	@Test
	public void testWildcardKey() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[0].*");
		
		final Object value = selector.selectValue(properties);
		Assert.assertEquals("17 Somewhere Road", value);
		
		final Entry<String, Object> entry = selector.select(properties);
		Assert.assertEquals(SimpleEntry.valueOf("addresses[0].addressLine", "17 Somewhere Road"), entry);
		
		final Collection<Object> values = selector.selectAllValues(properties);
		Assert.assertEquals(Arrays.asList("17 Somewhere Road", "London", "AB12 3CD"), Lists.newArrayList(values));
		
		Assert.assertEquals("addresses[0].*", selector.getPath());
		Assert.assertTrue(selector.isMulti());
		Assert.assertFalse(selector.isRoot());
	}
	
	@Test
	public void testOutOfBoundsIndex() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[2].*");
		Assert.assertNull(selector.selectValue(properties));
		Assert.assertNull(selector.select(properties));
		Assert.assertTrue(selector.selectAll(properties).isEmpty());
	}
	
	@Test
	public void testUnmappedKey() {
		final PropertySelector selector = PropertySelector.valueOf("addresses[1].unmapped");
		Assert.assertNull(selector.selectValue(properties));
		Assert.assertNull(selector.select(properties));
		Assert.assertTrue(selector.selectAll(properties).isEmpty());
	}
	
	@Test
	public void testRoot() {
		final PropertySelector selector = PropertySelector.valueOf(null);
		Assert.assertEquals("", selector.getPath());
		Assert.assertTrue(selector.isRoot());
		Assert.assertFalse(selector.isMulti());
		Assert.assertNull(selector.getParent());
		
		Assert.assertEquals(properties, selector.selectValue(properties));
	}
	
	@Test
	public void testParentChildCreation() {
		final PropertySelector selector = PropertySelector.valueOf("names[0]");
		Assert.assertEquals(PropertySelector.valueOf("names[0].*"), selector.getChild("*"));
		Assert.assertEquals(PropertySelector.valueOf("names[0].*"), selector.getChild("*"));
		Assert.assertEquals(PropertySelector.valueOf("names"), selector.getParent());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidChildCreation() {
		final PropertySelector selector = PropertySelector.valueOf("names[0]");
		selector.getChild(null);
	}
	
	private Map<String, Object> getFirstAddress() {
		final Map<String, Object> address = Maps.newLinkedHashMap();
		address.put("addressLine", "17 Somewhere Road");
		address.put("city", "London");
		address.put("postcode", "AB12 3CD");
		return address;
	}
}
