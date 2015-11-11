package uk.nhs.ciao.docs.parser.extractor;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/**
 * Strategy to add a prefix to a string value
 */
public enum PrefixMode {
	/**
	 * The prefix and value are concatenated as-is
	 */
	CONCATENATE {
		public String addPrefix(final String prefix, final String value) {
			if (prefix == null && value == null) {
				return null;
			} else if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(prefix)) {
				return MoreObjects.firstNonNull(prefix, value);
			}
			return prefix + value;
		}
	},
	
	/**
	 * The first letter of the value is converted to upper case
	 */
	CAMEL_CASE {
		public String addPrefix(final String prefix, final String value) {
			String camelCaseValue;
			if (Strings.isNullOrEmpty(value)) {
				camelCaseValue = value;
			} else if (value.length() == 1) {
				camelCaseValue = Character.toString(Character.toUpperCase(value.charAt(0)));
			} else {
				camelCaseValue = Character.toString(Character.toUpperCase(value.charAt(0)))
						+ value.substring(1);
			}
			
			return CONCATENATE.addPrefix(prefix, camelCaseValue);
		}
	};
	
	public abstract String addPrefix(final String prefix, final String value);
}