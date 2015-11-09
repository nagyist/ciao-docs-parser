package uk.nhs.ciao.docs.parser.kings;

import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.nhs.ciao.docs.parser.DocumentParser;
import uk.nhs.ciao.docs.parser.NodeStreamToDocumentPropertiesExtractor;
import uk.nhs.ciao.docs.parser.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.SplitterPropertiesExtractor;
import uk.nhs.ciao.docs.parser.TikaDocumentParser;
import uk.nhs.ciao.docs.parser.TikaParserFactory;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.XPathNodeSelector;

public class HtmlExample {
	public static void main(final String[] args) throws Exception {
		final String name = "input/Example7.htm";
		
		final PropertiesExtractor<Document> delegate = createExtractor();
		final PropertiesExtractor<Document> propertiesExtractor = new PropertiesExtractor<Document>() {
			@Override
			public Map<String, Object> extractProperties(final Document document)
					throws UnsupportedDocumentTypeException {
				try {
					final TransformerFactory tFactory = TransformerFactory.newInstance();
				    final Transformer transformer = tFactory.newTransformer();
				    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				    
				    DOMSource source = new DOMSource(document);
				    StreamResult result = new StreamResult(System.out);			    
					transformer.transform(source, result);
				} catch (final TransformerException e) {
					e.printStackTrace();
				}
				
				return delegate.extractProperties(document);
			}
		};
		
		final DocumentParser parser = new TikaDocumentParser(TikaParserFactory.createParser(),
				propertiesExtractor);
		
		final Map<String, Object> properties = parser.parseDocument(HtmlExample.class.getResourceAsStream(name));
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		System.out.println();		
		System.out.println(mapper.writeValueAsString(properties));
	}
	
	/**
	 * Properties extractor for the example HTML documents
	 */
	public static PropertiesExtractor<Document> createExtractor() throws XPathExpressionException {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		
		final SplitterPropertiesExtractor splitter = new SplitterPropertiesExtractor();
		
		splitter.addSelection(new XPathNodeSelector(xpath, "(/html/body/table[1]/tbody/tr/td/table/tbody/tr/td)[1]"),
				new SinglePropertyExtractor("trustName"));
		
		final SplitterPropertiesExtractor hospitalDetailsSplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[2]/tbody"), hospitalDetailsSplitter);
		
		hospitalDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "(./tr/td/table)[1]/tbody/tr/td"),
				new SinglePropertyExtractor("hospitalName", WhitespaceMode.TRIM));
		
		hospitalDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "(./tr/td/table)[2]/tbody/tr/td"),
				new KeyValuePropertyExtractor(":"));
		
		hospitalDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "./tr/td[starts-with(.,'Dear')]"),
				new SinglePropertyExtractor("doctorName")); // TODO: Will need transformation
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Medicines Reconcilation']]/tbody/tr/td/table/tbody/tr/td"),
				new PropertyTableExtractor());
		
		final SplitterPropertiesExtractor summarySplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[starts-with(.,'Discharge') and contains(.,'Notification')]]"),
				summarySplitter);
		
		summarySplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td)[starts-with(., 'This patient')]"),
				new SinglePropertyExtractor("dischargeSummary")); // TODO: Will need transformation
		
		final SplitterPropertiesExtractor patientDetailsSplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Patient:'] and descendant::td[text()='NHS No.:']]"),
				patientDetailsSplitter);
		
		// TODO: table[1] and table[2] are SPLIT key/value pair tables!
		
		patientDetailsSplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td/table)[3]/tbody/tr/td"),
				new PropertyTableExtractor(WhitespaceMode.TRIM));
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Allergies']]/*/tr/td"),
				new PropertyTableExtractor());
		// TODO: Currently there is no example document with a completed Allergies section
		
		final SplitterPropertiesExtractor dischargeMedicationSplitter = new SplitterPropertiesExtractor();
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Medication on Discharge']]"),
				dischargeMedicationSplitter);
		
		dischargeMedicationSplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td/table)[not(descendant::td[text()='Drug'])]/tbody/tr/td"),
				new PropertyTableExtractor());
		
		dischargeMedicationSplitter.addSelection(new XPathNodeSelector(xpath, "(./tbody/tr/td/table)[descendant::td[text()='Drug']]/tbody"),
				new ObjectTableExtractor(xpath, "./tr[1]/td", "dischargeMedication"));
		// TODO: Additional text details *may* be present in another mostly empty tr row!
						
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Clinical Assessment']]/*/tr/td"),
				new PropertyTableExtractor());
		
		// TODO: Notes section
		
		splitter.addSelection(new XPathNodeSelector(xpath, "/html/body/table[descendant::td[text()='Management']]/*/tr/td"),
				new PropertyTableExtractor());
		
		return new NodeStreamToDocumentPropertiesExtractor(splitter);
	}
}
