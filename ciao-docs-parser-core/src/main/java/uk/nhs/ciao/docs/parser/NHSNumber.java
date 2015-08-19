package uk.nhs.ciao.docs.parser;

import com.google.common.base.Preconditions;

/**
 * Represents an NHS number
 */
public class NHSNumber {
	private final String displayValue;
	private final String value;
	
	private NHSNumber(final String displayValue, final String value) {
		this.displayValue = Preconditions.checkNotNull(displayValue);
		this.value = Preconditions.checkNotNull(value);
	}
	
	/**
	 * Returns an NHSNumber equivalent to this instance where the display
	 * value is in normalised / canonical form.
	 * 
	 * @return <code>this</code> is this instance is normalised, or a new instance
	 * 		in normalized form
	 */
	public NHSNumber normalise() {
		return isNormalised() ? this : new NHSNumber(value, value);
	}
	
	/**
	 * Tests if this instance is empty (i.e. contains no digits)
	 */
	public boolean isEmpty() {
		return value.isEmpty();
	}
	
	/**
	 * Tests if this instance represents a valid NHS number
	 */
	public boolean isValid() {
		return passesMod11Algorithm(value);
	}
	
	/**
	 * Tests if this numbers display value is normalised
	 */
	public boolean isNormalised() {
		return value.equals(displayValue);
	}
	
	/**
	 * Returns the display value of the NHS number
	 * 
	 * @see NHSNumber#toString()
	 */
	public String getDisplayValue() {
		return displayValue;
	}
	
	/**
	 * Returns the normalised 'digit' value of the NHS number
	 * <p>
	 * NHSNumber instances are considered equal if <code>value</code> is equal.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Returns the display value of the NHS number
	 */
	@Override
	public String toString() {
		return displayValue;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	/**
	 * NHSNumber instances are considered equal if <code>value</code> is equal.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final NHSNumber other = (NHSNumber)obj;
		return value.equals(other.value);
	}
	
	/**
	 * Returns an NHSNumber instance matching the specified value
	 */
	public static NHSNumber valueOf(final CharSequence value) {
		if (value == null) {
			return null;
		}
		
		return new NHSNumber(value.toString(), extractDigits(value));
	}

	/**
	 * Tests if the specified string value represents a valid NHS number
	 */
	public static boolean isValid(final CharSequence value) {
		final String digits = extractDigits(value);
		return passesMod11Algorithm(digits);
	}
	
	/**
	 * Extracts all digits from the specified string (ignoring all non-digit characters)
	 */
	private static String extractDigits(final CharSequence value) {
		if (value == null) {
			return null;
		}
		
		final StringBuilder digits = new StringBuilder();

		for (int index = 0; index < value.length(); index++) {
			final char c = value.charAt(index);
			if (c >= '0' && c <= '9') {
				digits.append(c);
			}
		}
		
		return digits.toString();
	}
	
	/**
	 * Tests if the specified string passes the mod 11 algorithm.
	 * 
	 * @see http://www.datadictionary.nhs.uk/version2/data_dictionary/data_field_notes/n/nhs_number_de.asp
	 */
	private static boolean passesMod11Algorithm(final String digits) {
		if (digits == null || digits.length() != 10) {
			return false;
		}
		
		int weightedSum = 0;
		for (int index = 0; index < 9; index++) {
			weightedSum += (digits.charAt(index) - '0') * (10 - index);
		}
		
		final int check = (11 - (weightedSum % 11)) % 11;
		return check == (digits.charAt(9) - '0');
	}
}
