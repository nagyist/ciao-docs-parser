package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.util.Whitespace.collapseWhitespaceAndTrim;

import com.google.common.base.Strings;

/**
 * Strategy to use when normalising whitespace in a string
 */
public enum WhitespaceMode {
	/**
	 * Inner whitespace is collapsed into a single space and outer whitespace is trimmed
	 */
	COLLAPSE_AND_TRIM {
		@Override
		public String normalizeWhitespace(final String text) {
			return collapseWhitespaceAndTrim(text);
		}
	},
	
	/**
	 * Inner whitespace is left as-is and outer whitespace is trimmed
	 */
	TRIM {
		@Override
		public String normalizeWhitespace(final String text) {
			return Strings.nullToEmpty(text).trim();
		}
	};
	
	/**
	 * Normalises the whitespace in the specified string
	 */
	public abstract String normalizeWhitespace(final String text);
}