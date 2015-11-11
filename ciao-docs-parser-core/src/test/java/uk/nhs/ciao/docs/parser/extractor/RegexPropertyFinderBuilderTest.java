package uk.nhs.ciao.docs.parser.extractor;

import org.junit.Test;

import uk.nhs.ciao.docs.parser.extractor.RegexPropertyFinder;
import static org.junit.Assert.*;
import static uk.nhs.ciao.docs.parser.extractor.RegexPropertyFinder.*;

/**
 * Unit tests for {@link RegexPropertyFinder.Builder}
 */
public class RegexPropertyFinderBuilderTest {
	@Test
	public void whenFromIsNotSpecifiedThenNameShouldBeUsed() {
		final RegexPropertyFinder finder = builder("description").build();
		
		assertEquals("description", finder.getName());
		final String expected = "property value";
		final String actual = finder.findValue("description : property value");
		assertEquals(expected, actual);
	}
	
	@Test
	public void whenFromIsSpecifiedThenNameShouldNotBeUsed() {
		final RegexPropertyFinder finder = builder("description")
				.from("description-1")
				.build();
		
		assertEquals("description", finder.getName());
		final String expected = "property value";
		final String actual = finder.findValue("description-1 : property value");
		assertEquals(expected, actual);
	}
	
	@Test
	public void whenToIsSpecifiedThenItShouldNotBePartOfTheValue() {
		final RegexPropertyFinder finder = builder("description")
				.to("title").build();
		
		assertEquals("description", finder.getName());
		final String expected = "property value";
		final String actual = finder.findValue("description : property value title");
		assertEquals(expected, actual);
	}
	
	@Test
	public void whenValueAndLiteralsAreSeparatedByWhitespaceThenItShouldBeTrimmmed() {
		final RegexPropertyFinder finder = builder("description")
				.to("title").build();
		
		assertEquals("description", finder.getName());
		final String expected = "property value";
		final String actual = finder.findValue("\tdescription   :\t property value\t \n title");
		assertEquals(expected, actual);
	}
	
	@Test
	public void whenThereIsNoWhitespaceThenTheValueShouldStillBeExtracted() {
		final RegexPropertyFinder finder = builder("description")
				.to("title").build();
		
		assertEquals("description", finder.getName());
		final String expected = "property value";
		final String actual = finder.findValue("description:property valuetitle");
		assertEquals(expected, actual);
	}
}
