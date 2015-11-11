package uk.nhs.ciao.docs.parser.extractor;

/**
 * Strategy to determine how properties should be
 * extracted via delegates.
 */
public enum DelegationMode {
	/**
	 * The delegate extractor is invoked for each node in the incoming stream
	 */
	ONCE_PER_NODE,
	
	/**
	 * The delegate extractor is invoked once for the entire incoming stream
	 */
	ONCE_PER_STREAM;
}