package uk.nhs.ciao.docs.parser;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link NHSNumber}
 */
public class NHSNumberTest {
	@Test
	public void testDisplayValueAndValueCanBeDifferent() {
		final String displayValue = "123 456 789 0 ";
		final String value = "1234567890";
		final NHSNumber number = NHSNumber.valueOf(displayValue);
		
		assertEquals(displayValue, number.getDisplayValue());
		assertEquals(displayValue, number.toString());
		assertEquals(value, number.getValue());
	}
	
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
		assertInvalid("123 456 789 0 ");
		assertInvalid("12345678901");
		assertInvalid(null);
	}
	
	private void assertInvalid(final String number) {
		final NHSNumber nhsNumber = NHSNumber.valueOf(number);
		if (nhsNumber != null) {
			assertFalse(nhsNumber.isValid());
		}
		
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
