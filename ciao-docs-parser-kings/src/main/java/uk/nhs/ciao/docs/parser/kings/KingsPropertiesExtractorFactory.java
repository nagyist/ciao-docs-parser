package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.docs.parser.RegexPropertyFinder.*;
import uk.nhs.ciao.docs.parser.RegexPropertiesExtractor;

public class KingsPropertiesExtractorFactory {
	private KingsPropertiesExtractorFactory() {
		// Suppress default constructor
	}
	
	public static RegexPropertiesExtractor createEDDischargeExtractor() {
		final RegexPropertiesExtractor extractor = new RegexPropertiesExtractor();
		
		extractor.addPropertyFinders(
				builder("Re").to("ED No").build(),
				builder("ED No").to("DOB").build(),
				builder("DOB").to("Hosp No").build(),
				builder("Hosp No").to("Address").build(),
				builder("Address").to("NHS No").build(),
				builder("NHS No").to("The patient").build(),
				builder("Seen By").to("Investigations").build(),
				builder("Investigations").to("Working Diagnosis").build(),
				builder("Working Diagnosis").to("Referrals").build(),
				builder("Referrals").to("Outcome").build(),
				builder("Outcome").to("Comments for GP").build(),
				builder("Comments for GP").to("If you have any").build()
			);
		
		return extractor;
	}
	
	
	public static RegexPropertiesExtractor createDischargeNotificationExtractor() {		
		final RegexPropertiesExtractor extractor = new RegexPropertiesExtractor();
		
		extractor.addPropertyFinders(
				builder("Ward").to("Hospital Number").build(),
				builder("Hospital Number").to("NHS Number").build(),
				builder("NHS Number").to("Ward Tel").build(),
				builder("Ward Tel").to("Patient Name").build(),
				builder("Patient Name").to("Consultant").build(),
				builder("Consultant").to("D.O.B").build(),
				builder("D.O.B").to("Speciality").build(),
				builder("Speciality").to("Date of Admission").build(),
				builder("Date of Admission").to("Discharged by").build(),
				builder("Discharged by").to("Date of Discharge").build(),
				builder("Date of Discharge").to("Role / Bleep").build(),
				builder("Role / Bleep").to("Discharge Address").build(),
				builder("Discharge Address").to("GP").build(),
				builder("GP").build()
			);
		
		/*
		 * The default text content extraction is altered because there is
		 * no known terminator for the GP property (it varies from document
		 * to document). Instead the closing html p tag is used to find 
		 * the end.
		 */
		extractor.setTextFilter("Ward", "GP");
		
		return extractor;
	}
}
