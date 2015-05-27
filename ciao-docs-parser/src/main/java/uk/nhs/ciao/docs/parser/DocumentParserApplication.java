package uk.nhs.ciao.docs.parser;

import uk.nhs.ciao.camel.CamelApplication;
import uk.nhs.ciao.camel.CamelApplicationRunner;
import uk.nhs.ciao.exceptions.CIAOConfigurationException;

/**
 * The main ciao-docs-parser application
 */
public class DocumentParserApplication extends CamelApplication {
	/**
	 * Runs the document parser application
	 */
	public static void main(final String[] args) throws Exception {
		final CamelApplication application = new DocumentParserApplication(args);
		CamelApplicationRunner.runApplication(application);
	}
	
	public DocumentParserApplication(final String[] args) throws CIAOConfigurationException {
		super("ciao-docs-parser.properties", args);
	}
}
