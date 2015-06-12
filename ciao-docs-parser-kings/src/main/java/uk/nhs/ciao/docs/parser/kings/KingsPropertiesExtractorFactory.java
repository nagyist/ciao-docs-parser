package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.docs.parser.RegexPropertyFinder.*;
import uk.nhs.ciao.docs.parser.DatePropertyConverter;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.RegexPropertiesExtractor;

/**
 * Factory to create {@link PropertiesExtractor}s capable of
 * finding and extracting properties from Kings PDF documents.
 */
public class KingsPropertiesExtractorFactory {
	private KingsPropertiesExtractorFactory() {
		// Suppress default constructor
	}
	
	/**
	 * Creates an extractor for finding properties in King's ED Discharge PDFs
	 * <p>
	 * The following properties are extracted:
	 * <ul>
	 * <li>Re</li>
	 * <li>ED No</li>
	 * <li>DOB</li>
	 * <li>Hosp No</li>
	 * <li>Address</li>
	 * <li>NHS No</li>
	 * <li>Seen By</li>
	 * <li>Investigations</li>
	 * <li>Working Diagnosis</li>
	 * <li>Referrals</li>
	 * <li>Outcome</li>
	 * <li>Comments for GP</li>
	 * </ul>
	 */
	public static RegexPropertiesExtractor createEDDischargeExtractor() {
		final RegexPropertiesExtractor extractor = new RegexPropertiesExtractor();
		
		final DatePropertyConverter dateConverter = new DatePropertyConverter("dd/MM/yyyy");
		
		extractor.addPropertyFinders(
				builder("patientFullName").from("Re").to("ED No").build(),
				builder("ED No").to("DOB").build(),
				builder("patientBirthDate").from("DOB").to("Hosp No").convert(dateConverter).build(),
				builder("Hosp No").to("Address").build(),
				builder("Address").to("NHS No").build(),
				builder("patientNHSNo").from("NHS No").to("The patient").build(),
				builder("Seen By").to("Investigations").build(),
				builder("Investigations").to("Working Diagnosis").build(),
				builder("Working Diagnosis").to("Referrals").build(),
				builder("Referrals").to("Outcome").build(),
				builder("Outcome").to("Comments for GP").build(),
				builder("Comments for GP").to("If you have any").build()
			);
		
		return extractor;
	}
	
	
	/**
	 * Creates an extractor for finding properties in King's Discharge Notification PDFs
	 * <p>
	 * The following properties are extracted:
	 * <ul>
	 * <li>Ward</li>
	 * <li>Hospital Number</li>
	 * <li>NHS Number</li>
	 * <li>Ward Tel</li>
	 * <li>Patient Name</li>
	 * <li>Consultant</li>
	 * <li>D.O.B</li>
	 * <li>Speciality</li>
	 * <li>Date of Admission</li>
	 * <li>Discharged by</li>
	 * <li>Date of Discharge</li>
	 * <li>Role / Bleep</li>
	 * <li>Discharge Address</li>
	 * <li>GP</li>
	 * </ul>
	 */
	public static RegexPropertiesExtractor createDischargeNotificationExtractor() {		
		final RegexPropertiesExtractor extractor = new RegexPropertiesExtractor();
		
		final DatePropertyConverter dateConverter = new DatePropertyConverter("dd-MMM-yyyy");
		
		extractor.addPropertyFinders(
				builder("Ward").to("Hospital Number").build(),
				builder("Hospital Number").to("NHS Number").build(),
				builder("patientNHSNo").from("NHS Number").to("Ward Tel").build(),
				builder("Ward Tel").to("Patient Name").build(),
				builder("patientFullName").from("Patient Name").to("Consultant").build(),
				builder("Consultant").to("D.O.B").build(),
				builder("patientBirthDate").from("D.O.B").to("Speciality").convert(dateConverter).build(),
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
