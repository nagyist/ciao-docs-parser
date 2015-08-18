package uk.nhs.ciao.docs.parser.kings;

import static uk.nhs.ciao.docs.parser.RegexPropertyFinder.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import uk.nhs.ciao.docs.parser.DatePropertyConverter;
import uk.nhs.ciao.docs.parser.NodeStream;
import uk.nhs.ciao.docs.parser.NodeStreamToDocumentPropertiesExtractor;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.PropertiesExtractorChain;
import uk.nhs.ciao.docs.parser.PropertiesTransformer;
import uk.nhs.ciao.docs.parser.PropertiesValidator;
import uk.nhs.ciao.docs.parser.RegexPropertiesExtractor;
import uk.nhs.ciao.docs.parser.SplitterPropertiesExtractor;
import uk.nhs.ciao.docs.parser.XPathNodeSelector;

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
	
	/**
	 * Creates an extractor which extracts properties from a MS Word format discharge notification
	 * <p>
	 * The default XPathFactory is used
	 * 
	 * @see #createWordDischargeNotificationExtractor(XPath)
	 */
	public static PropertiesExtractor<Document> createWordDischargeNotificationExtractor() throws XPathExpressionException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		return createWordDischargeNotificationExtractor(xpath);
	}
	
	/**
	 * Creates an extractor which extracts properties from a MS Word format discharge notification
	 * <p>
	 * The extracted properties are dynamic - the names are determined by the contents of the document. The document
	 * is processed in sections, where each section uses a given pattern to define name/value pairs.
	 */
	public static PropertiesExtractor<Document> createWordDischargeNotificationExtractor(final XPath xpath) throws XPathExpressionException {		
		final SplitterPropertiesExtractor splitter = new SplitterPropertiesExtractor();
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[position()=1]//p/b"), new WordDischargeNotificationDetector());

		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/p[position()=1]"),
				new SinglePropertyExtractor("hospitalAddress"));
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[position()=2]/*/tr/td/p"),
				new PropertyTableExtractor());
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[position()=3]/*/tr/td/p"),
				new PropertyTableExtractor());
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[count(preceding::b[text()='Consultant follow up:']) = 1 and count(following::b[text()='Discharge Medication']) = 1]/*/tr"),
				new ObjectTableExtractor(xpath, "allergens"));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[count(preceding::b[text()='Discharge Medication']) = 1 and count(following::b[text()='Prescriber:']) = 1]/*/tr"),
				new ObjectTableExtractor(xpath, "dischargeMedication"));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::b[text()='Prescriber:']]/*/tr/td/p"),
				new PropertyTableExtractor());
		
		// TODO: example property validator - perhaps these can be configured via a resource or spring etc?
		final PropertiesValidator validator = new PropertiesValidator();
		validator.requireNHSNumberProperty("NHS Number");
		validator.requireDateProperty("D.O.B", "dd/MM/yyyy", true);
		validator.requireNonEmptyProperty("Self Discharge");
		
		// TODO: example property transformation - perhaps these can be configured via a resource or spring etc?
		final PropertiesTransformer transformer = new PropertiesTransformer();
		transformer.renameProperty("NHS Number", "patientNHSNo");
		
		final PropertiesExtractorChain<NodeStream> chain = new PropertiesExtractorChain<NodeStream>(splitter);
		chain.addExtractor(validator);
		chain.addExtractor(transformer);
		
		return new NodeStreamToDocumentPropertiesExtractor(chain);
	}
}
