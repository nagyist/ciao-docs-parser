package uk.nhs.ciao.docs.parser.extractor;

import com.google.common.base.Preconditions;

/**
 * Strategy for detecting if a text value represents a property name
 */
public abstract class PropertyNameDetector {
	/**
	 * Detects property names which end with <code>':'</code>
	 */
	public static final PropertyNameDetector ENDS_WITH_COLON = new SuffixPropertyNameDetected(":");
	
	/**
	 * Tests if the text represents a property name
	 */
	public abstract boolean isPropertyName(final String text);
	
	/**
	 * Returns the property name or null if the specified value does
	 * not represent a property name
	 */
	public abstract String getPropertyName(final String text);
	
	/**
	 * Strategy to detect property names by matching the specified suffix
	 * <p>
	 * To convert the text value to a property name, the suffix is removed
	 */
	public static class SuffixPropertyNameDetected extends PropertyNameDetector {
		private final String suffix;
		
		public SuffixPropertyNameDetected(final String suffix) {
			this.suffix = Preconditions.checkNotNull(suffix);
			Preconditions.checkArgument(!suffix.isEmpty());
		}
		
		@Override
		public boolean isPropertyName(final String text) {
			return text != null && text.length() > suffix.length() && text.endsWith(suffix);
		}
		
		@Override
		public String getPropertyName(final String text) {
			return isPropertyName(text) ? text.substring(0, text.length() - suffix.length()) : null;
		}
	}
}
