package uk.nhs.ciao.docs.parser.kings;

/**
 * Strategy for how property values are calculated from a list of nodes
 */
public enum ValueMode {
	/**
	 * Multiple values are stored in a list
	 */
	MULTIPLE_VALUES,
	
	
	/**
	 * Multiple values are appended as text as a single property value
	 */
	SINGLE_VALUE,
	
	/**
	 * Only the initial (non-empty) value is used
	 */
	INITIAL_VALUE;
}