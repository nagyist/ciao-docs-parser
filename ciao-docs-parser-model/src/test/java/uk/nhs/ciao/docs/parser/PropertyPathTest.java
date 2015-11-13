package uk.nhs.ciao.docs.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closeables;

/**
 * Unit tests for {@link PropertyPath}
 */
public class PropertyPathTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyPathTest.class);

	@Test
	public void testSimpleKey() {
		roundtrip("name", false, "name");
	}
	
	@Test
	public void testSimpleIndex() {
		roundtrip("[2]", false, 2);
	}
	
	@Test
	public void testAnyKey() {
		roundtrip("*", true, PropertyPath.ANY_KEY);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDisallowedAnyKey() {
		PropertyPath.parse("*", false);
	}
	
	@Test
	public void testAnyIndex() {
		roundtrip("name[*]", true, "name", PropertyPath.ANY_INDEX);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDanglingDelimiter() {
		PropertyPath.parse("name\\", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnclosedIndex() {
		PropertyPath.parse("[0", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidIndex() {
		PropertyPath.parse("[abd]", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnescapedClosingBracket() {
		PropertyPath.parse("]", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDanglingKeySeparator() {
		PropertyPath.parse("names.", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDisallowedAnyIndex() {
		PropertyPath.parse("name[*]", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUnescapedWildcardCharacter() {
		PropertyPath.parse("na*me", false);
	}
	
	@Test
	public void testEscapedWildcardCharacter() {
		roundtrip("na\\*me", false, "na*me");
	}
	
	@Test
	public void testAnyKeyWithSpecialCharacters() {
		roundtrip("na\\[\\]me.*.k\\.ey", true, "na[]me", PropertyPath.ANY_KEY, "k.ey");
	}
	
	@Test
	public void testAnyIndexWithSpecialCharacters() {
		roundtrip("na\\\\me[*].tit\\*le", true, "na\\me", PropertyPath.ANY_INDEX, "tit*le");
	}
	
	@Test
	public void testKeyWithSpecialCharacters() {
		roundtrip("na\\.m\\\\e\\[\\].values[2]", false, "na.m\\e[]", "values", 2);
	}
	
	@Test
	public void testContainsWildcards() {
		Assert.assertTrue(PropertyPath.containsWildcard(PropertyPath.parse("value.*.key", true)));
		Assert.assertTrue(PropertyPath.containsWildcard(PropertyPath.parse("values[*].key", true)));
		Assert.assertFalse(PropertyPath.containsWildcard(PropertyPath.parse("values[0].key", true)));
	}
	
	@Test
	public void testGetValue() throws Exception {
		final InputStream resource = getClass().getResourceAsStream("property-selector-fixture.json");
		try {
			final Map<String, Object> source = new ObjectMapper().readValue(resource, new TypeReference<Map<String, Object>>(){});
			
			assertGet(source, "name", "Mr Example");
			assertGet(source, "age", 28);
			assertGet(source, "addresses[*].city", "London");
			assertGet(source, "addresses[1].*", "27 Somewhere Else");
			assertGet(source, "addresses[2].*", null);
			assertGet(source, "addresses[1].city[1]", null);
			assertGet(source, "addresses.city", null);
			assertGet(source, "[2]", null);
			assertGet(source, "", source);
			assertGet(source, "key\\.with\\[\\]\\.special\\.chars", "special-value");
		} finally {
			Closeables.closeQuietly(resource);
		}
	}
	
	private void assertGet(final Map<String, Object> source, final String path, final Object expected) {
		assertGet(source, PropertyPath.parse(path, true), expected);
	}
	
	private void assertGet(final Map<String, Object> source, final Object[] segments, final Object expected) {
		LOGGER.info("Testing get() for segments: " + Arrays.toString(segments));
		
		final Object actual = PropertyPath.getValue(Object.class, source, segments);
		Assert.assertEquals("segments: " + Arrays.toString(segments), expected, actual);
	}
	
	private void roundtrip(final String path, final boolean allowWildcards, final Object... expectedSegments) {
		LOGGER.info("Testing PropertyPath roundtrip for path: " + path + " => segments: " + Arrays.toString(expectedSegments));
		final Object[] actualSegments = PropertyPath.parse(path, allowWildcards);
		Assert.assertArrayEquals("path: " + path, expectedSegments, actualSegments);
		
		final String actualPath = PropertyPath.toString(actualSegments);
		Assert.assertEquals(path, actualPath);
	}
}
