package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link RegexPropertyFinder}
 */
public class RegexPropertyFinderTest {
	private RegexPropertyFinder finder;
	
	@Before
	public void setup() {
		this.finder = new RegexPropertyFinder("description",
				Pattern.compile("\\(description = (.+)\\)"));
	}
	
	@Test
	public void whenAPatternIsNotFoundThenTheEmptyStringShouldBeReturned() {
		final String expected = "";
		final String actual = finder.findValue("does not match");
		assertEquals(expected, actual);
	}
	
	@Test
	public void whenAPatternMatchesTheWholeStringThenTheValueShouldBeReturned() {
		final String expected = "has content";
		final String actual = finder.findValue("(description = has content)");
		assertEquals(expected, actual);
	}
	
	@Test
	public void whenAPatternMatchesPartOfTheStringThenTheValueShouldBeReturned() {
		final String expected = "has content";
		final String actual = finder.findValue("some initial text (description = has content) and some more after");
		assertEquals(expected, actual);
	}
}
