package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link NHSNumber}
 */
public class NHSNumberTest {
	@Test
	public void testNormalise() {
		final NHSNumber number = NHSNumber.valueOf("123 456 789 0 ");
		final NHSNumber expected = NHSNumber.valueOf("1234567890");
		final NHSNumber actual = number.normalise();
		
		assertFalse(number.isNormalised());
		assertTrue(expected.isNormalised());
		assertEquals(actual,  expected);
	}
	
	@Test
	public void testNHSNumberPassesMod11Algorithm() {
		final String number = "123 456 789 1 ";
		assertFalse(NHSNumber.valueOf(number).isValid());
		assertFalse(NHSNumber.isValid(number));
	}
	
	@Test
	public void testNHSNumberFailsMod11Algorithm() {
		final String number = "123 456 789 0 ";
		assertFalse(NHSNumber.valueOf(number).isValid());
		assertFalse(NHSNumber.isValid(number));
	}
	
	@Test
	public void testValueOfNullStringShouldBeNull() {
		final NHSNumber number = NHSNumber.valueOf(null);
		assertNull(number);
	}
	
	@Test
	public void testValueOfEmptyStringShouldBeEmpty() {
		final NHSNumber number = NHSNumber.valueOf("");
		assertNotNull(number);
		assertTrue(number.isEmpty());
	}
}
