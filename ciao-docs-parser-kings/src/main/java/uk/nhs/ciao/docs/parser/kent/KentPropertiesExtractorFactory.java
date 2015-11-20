package uk.nhs.ciao.docs.parser.kent;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import uk.nhs.ciao.docs.parser.extractor.DelegationMode;
import uk.nhs.ciao.docs.parser.extractor.KeyValuePropertyExtractor;
import uk.nhs.ciao.docs.parser.extractor.NestedObjectPropertyExtractor;
import uk.nhs.ciao.docs.parser.extractor.NodeStreamToDocumentPropertiesExtractor;
import uk.nhs.ciao.docs.parser.extractor.ObjectTableExtractor;
import uk.nhs.ciao.docs.parser.extractor.PrefixMode;
import uk.nhs.ciao.docs.parser.extractor.PrefixedPropertyExtractor;
import uk.nhs.ciao.docs.parser.extractor.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.extractor.PropertySplitTableExtractor;
import uk.nhs.ciao.docs.parser.extractor.PropertyTableExtractor;
import uk.nhs.ciao.docs.parser.extractor.SinglePropertyExtractor;
import uk.nhs.ciao.docs.parser.extractor.SplitterPropertiesExtractor;
import uk.nhs.ciao.docs.parser.extractor.ValueMode;
import uk.nhs.ciao.docs.parser.extractor.WhitespaceMode;
import uk.nhs.ciao.docs.parser.xml.XPathNodeSelector;

/**
 * Factory to create {@link PropertiesExtractor}s capable of
 * finding and extracting properties from Kent HTML documents.
 */
public class KentPropertiesExtractorFactory {
	private KentPropertiesExtractorFactory() {
		// Suppress default constructor
	}
	
	/**
	 * Properties extractor for the electronic discharge notification HTML documents
	 * <p>
	 * Some of the extracted properties are dynamic - the names are determined by the contents of the document. The document
	 * is processed in sections, where each section uses a given pattern to define name/value pairs.
	 */
	public static PropertiesExtractor<Document> createEDNExtractor() throws XPathExpressionException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		
		final SplitterPropertiesExtractor splitter = new SplitterPropertiesExtractor();
		
		splitter.addSelection(new XPathNodeSelector(xpath, "(/html/body/table[1]/tbody/tr/td/table/tbody/tr/td)[1]"),
				new SinglePropertyExtractor("trustName"));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='AMENDED VERSION']]/tbody/tr[2]/td"),
				new NestedObjectPropertyExtractor("amendedVersion", new PropertyTableExtractor()));
		
		final SplitterPropertiesExtractor hospitalDetailsSplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[starts-with(.,'Ward Tel:')]]/tbody"), hospitalDetailsSplitter);
		
		hospitalDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "(./tr/td/table)[1]/tbody/tr/td"),
				new SinglePropertyExtractor("hospitalName", WhitespaceMode.TRIM));
		
		hospitalDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "(./tr/td/table)[2]/tbody/tr/td"),
				new KeyValuePropertyExtractor(":"));
		
		hospitalDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "./tr/td[starts-with(.,'Dear')]"),
				new SinglePropertyExtractor("gpName"));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Medicines Reconcilation']]/tbody/tr/td/table/tbody/tr/td"),
				new PrefixedPropertyExtractor("medicinesReconcilation", new PropertyTableExtractor(), PrefixMode.CAMEL_CASE));
		
		final SplitterPropertiesExtractor summarySplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[starts-with(.,'Discharge') and contains(.,'Notification')]]"),
				summarySplitter);
		
		summarySplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td)[starts-with(., 'This patient')]"),
				new SinglePropertyExtractor("dischargeSummary"));
		
		final SplitterPropertiesExtractor patientDetailsSplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Patient:'] and descendant::td[text()='NHS No.:']]"),
				patientDetailsSplitter);
		
		patientDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "./tbody"),
				new PropertySplitTableExtractor(xpath, "(./tr/td/table)[1]/tbody/tr/td", "(./tr/td/table)[2]/tbody/tr/td"));
		
		patientDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td/table)[3]/tbody/tr/td"),
				new PropertyTableExtractor(WhitespaceMode.TRIM));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Allergies']]/*/tr/td"),
				new PropertyTableExtractor());
		// TODO: Currently there is no example document with a completed Allergies section
		
		final SplitterPropertiesExtractor dischargeMedicationSplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Medication on Discharge']]"),
				dischargeMedicationSplitter);
		
		final SplitterPropertiesExtractor dischargeMedicationStaffSplitter = new SplitterPropertiesExtractor();
		dischargeMedicationSplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td/table)[not(descendant::td[text()='Drug'])]/tbody/tr"),
				new NestedObjectPropertyExtractor("dischargeMedicationStaff", dischargeMedicationStaffSplitter, DelegationMode.ONCE_PER_NODE));
		
		dischargeMedicationStaffSplitter.addSelection(new XPathNodeSelector(xpath, "./td"),
				new PropertyTableExtractor());
		
		dischargeMedicationSplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td/table)[descendant::td[text()='Drug']]/tbody"),
				new ObjectTableExtractor(xpath, "./tr[1]/td", "dischargeMedication"));
		// TODO: Additional text details *may* be present in another mostly empty tr row!
						
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Clinical Assessment']]/tbody/tr/td"),
				new PropertyTableExtractor());
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[starts-with(.,'Notes')]]/tbody/tr/td/p"),
				new SinglePropertyExtractor("Notes", ValueMode.MULTIPLE_VALUES));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Management']]/tbody/tr/td"),
				new PropertyTableExtractor());
		
		return new NodeStreamToDocumentPropertiesExtractor(splitter);
	}
}
