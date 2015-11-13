package uk.nhs.ciao.docs.parser;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@Ignore("Requires wildcard parsing into PropertyPath to be merged")
	public void testDisallowedAnyKey() {
		PropertyPath.parse("*", false);
	}
	
	@Test
	public void testAnyIndex() {
		roundtrip("name[*]", true, "name", PropertyPath.ANY_INDEX);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDisallowedAnyIndex() {
		PropertyPath.parse("name[*]", false);
	}
	
	@Test
	@Ignore("Requires wildcard parsing into PropertyPath to be merged")
	public void testAnyKeyWithSpecialCharacters() {
		roundtrip("na\\[\\]me.*.k\\.ey", true, "na[]me", PropertyPath.ANY_KEY, "k.ey");
	}
	
	@Test
	@Ignore("Requires wildcard parsing into PropertyPath to be merged")
	public void testAnyIndexWithSpecialCharacters() {
		roundtrip("na\\\\me[*].tit\\*le", true, "na\\\\me", PropertyPath.ANY_INDEX, "tit*le");
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
	
	private void roundtrip(final String path, final boolean allowWildcards, final Object... expectedSegments) {
		LOGGER.info("Testing PropertyPath roundtrip for path: " + path + " => segments: " + Arrays.toString(expectedSegments));
		final Object[] actualSegments = PropertyPath.parse(path, allowWildcards);
		Assert.assertArrayEquals("path: " + path, expectedSegments, actualSegments);
		
		final String actualPath = PropertyPath.toString(actualSegments);
		Assert.assertEquals(path, actualPath);
	}
}
