package uk.nhs.ciao.docs.parser;

/**
 * A property converter which always returns the original value (i.e. NOOP)
 */
public final class NoopPropertyConverter implements PropertyConverter {
	/**
	 * Singleton instance
	 */
	private static final NoopPropertyConverter INSTANCE = new NoopPropertyConverter();
	
	/**
	 * Returns an instance of {@link NoopPropertyConverter}
	 */
	public static NoopPropertyConverter getInstance() {
		return INSTANCE;
	}
	
	private NoopPropertyConverter() {
		// Suppress default constructor
	}
	
	@Override
	public String convertProperty(final String name, final String value) {
		return value;
	}
}
