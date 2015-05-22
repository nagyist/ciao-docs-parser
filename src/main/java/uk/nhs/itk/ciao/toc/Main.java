package uk.nhs.itk.ciao.toc;

import uk.nhs.itk.ciao.camel.CamelApplication;

public class Main {
	public static void main(final String[] args) throws Exception {
		CamelApplication.startApplication("ciao-transfer-of-care.properties", args);
	}
}
