package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.docs.parser.RegexPropertiesExtractor.*;
import uk.nhs.ciao.docs.parser.RegexPropertiesExtractor;

public class KingsPropertiesExtractorFactory {
	private KingsPropertiesExtractorFactory() {
		// Suppress default constructor
	}
	
	public static RegexPropertiesExtractor createEDDischargeExtractor() {
		final RegexPropertiesExtractor extractor = new RegexPropertiesExtractor();
		
		extractor.addPropertyFinders(
				propertyFinder("Re").to("ED No").build(),
				propertyFinder("ED No").to("DOB").build(),
				propertyFinder("DOB").to("Hosp No").build(),
				propertyFinder("Hosp No").to("Address").build(),
				propertyFinder("Address").to("NHS No").build(),
				propertyFinder("NHS No").to("The patient").build(),
				propertyFinder("Seen By").to("Investigations").build(),
				propertyFinder("Investigations").to("Working Diagnosis").build(),
				propertyFinder("Working Diagnosis").to("Referrals").build(),
				propertyFinder("Referrals").to("Outcome").build(),
				propertyFinder("Outcome").to("Comments for GP").build(),
				propertyFinder("Comments for GP").to("If you have any").build()
			);
		
		return extractor;
	}
	
	
	public static RegexPropertiesExtractor createDischargeNotificationExtractor() {		
		final RegexPropertiesExtractor extractor = new RegexPropertiesExtractor();
		
		extractor.addPropertyFinders(
				propertyFinder("Ward").to("Hospital Number").build(),
				propertyFinder("Hospital Number").to("NHS Number").build(),
				propertyFinder("NHS Number").to("Ward Tel").build(),
				propertyFinder("Ward Tel").to("Patient Name").build(),
				propertyFinder("Patient Name").to("Consultant").build(),
				propertyFinder("Consultant").to("D.O.B").build(),
				propertyFinder("D.O.B").to("Speciality").build(),
				propertyFinder("Speciality").to("Date of Admission").build(),
				propertyFinder("Date of Admission").to("Discharged by").build(),
				propertyFinder("Discharged by").to("Date of Discharge").build(),
				propertyFinder("Date of Discharge").to("Role / Bleep").build(),
				propertyFinder("Role / Bleep").to("Discharge Address").build(),
				propertyFinder("Discharge Address").to("GP").build(),
				propertyFinder("GP").build()
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
