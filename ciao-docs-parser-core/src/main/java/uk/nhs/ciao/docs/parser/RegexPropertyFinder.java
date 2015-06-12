package uk.nhs.ciao.docs.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Defines a document property and an associated regular
 * expression capable of finding the property in a stream of text
 */
public class RegexPropertyFinder {
	private final String name;
	private final Pattern pattern;
	private final PropertyConverter converter;
	
	/**
	 * Creates a new named property finder backed by the specified regex
	 * <p>
	 * The value to extract should be specified as group(1) in the pattern
	 * 
	 * @param name The name of the property to find
	 * @param pattern The pattern which will match the property value
	 */
	public RegexPropertyFinder(final String name, final Pattern pattern) {
		this(name, pattern, NoopPropertyConverter.getInstance());
	}
	
	/**
	 * Creates a new named property finder backed by the specified regex
	 * <p>
	 * The value to extract should be specified as group(1) in the pattern
	 * 
	 * @param name The name of the property to find
	 * @param pattern The pattern which will match the property value
	 * @param converter The (optional) converter to use when a property value is found
	 */
	public RegexPropertyFinder(final String name, final Pattern pattern, final PropertyConverter converter) {
		this.name = Preconditions.checkNotNull(name);
		this.pattern = Preconditions.checkNotNull(pattern);
		this.converter = converter == null ? NoopPropertyConverter.getInstance() : converter;
	}
	
	/**
	 * The name of the property this finder is searching for
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Attempts to find the property value in the specified text
	 * 
	 * @param text The text to search
	 * @return The associated value if one could be found, or the empty string otherwise.
	 */
	public String findValue(final String text) {
		final Matcher matcher = pattern.matcher(text);
		final String value = matcher.find() ? matcher.group(1).trim() : "";
		return converter.convertProperty(text, value);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("pattern", pattern)
				.toString();
	}
	
	/**
	 * Starts creating a finder with the specified name.
	 * <p>
	 * The value is used for both the name and the initial
	 * token to search for
	 */
	public static Builder builder(final String name) {
		return new Builder(name);
	}
	
	/**
	 * Fluent builder for creating regex property finders
	 * <p>
	 * Currently this builder creates patterns of the form:
	 * <code>{startLiteral} ':' {propertyValue} {endLiteral}?</code> and is
	 * tolerant of whitespace between tokens
	 */
	public static class Builder {
		private final String name;
		private String startLiteral;
		private String endLiteral;
		private PropertyConverter converter;

		/**
		 * Creates a new builder with the specified name / start token
		 */
		private Builder(final String name) {
			this.name = Preconditions.checkNotNull(name);
			this.startLiteral = name;
			this.converter = NoopPropertyConverter.getInstance();
		}
		
		/**
		 * Specifies the initial token to search for
		 */
		public Builder from(final String startLiteral) {
			setStartLiteral(startLiteral);
			return this;
		}
		
		/**
		 * Specifies the ending token to search for
		 */
		public Builder to(final String endLiteral) {
			setEndLiteral(endLiteral);
			return this;
		}
		
		/**
		 * Specifies the property value converter to use
		 */
		public Builder convert(final PropertyConverter converter) {
			setConverter(converter);
			return this;
		}
		
		/**
		 * Specifies the initial token to search for
		 */
		// bean setter for spring
		public void setStartLiteral(final String startLiteral) {
			this.startLiteral = Preconditions.checkNotNull(startLiteral);
		}
		
		/**
		 * Specifies the ending token to search for
		 */
		// bean setter for spring
		public void setEndLiteral(final String endLiteral) {
			this.endLiteral = endLiteral;
		}
		
		/**
		 * Specifies the property value converter to use
		 */
		// bean setter for spring
		public void setConverter(final PropertyConverter converter) {
			this.converter = converter == null ? NoopPropertyConverter.getInstance() : converter;
		}
		
		/**
		 * Builds a new property finder based on the configured properties
		 * <p>
		 * A structure of <code>{startLiteral} : {value} {endLiteral}</code> is
		 * used when generating the regex pattern
		 */
		public RegexPropertyFinder build() {
			return new RegexPropertyFinder(name, compilePattern(), converter);
		}

		/**
		 * Compiles the pattern to find
		 * <p>
		 * Group(1) matches the property value
		 */
		private Pattern compilePattern() {
			final String suffix = endLiteral == null ? "" :
				Pattern.quote(endLiteral);
			return Pattern.compile(Pattern.quote(startLiteral) +
					"\\s*:\\s*+(.*)\\s*+" + suffix);
		}
	}
}