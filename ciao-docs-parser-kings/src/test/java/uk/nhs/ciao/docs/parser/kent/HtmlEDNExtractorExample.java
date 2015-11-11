package uk.nhs.ciao.docs.parser.kent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

import uk.nhs.ciao.docs.parser.DocumentParser;
import uk.nhs.ciao.docs.parser.TikaDocumentParser;
import uk.nhs.ciao.docs.parser.TikaParserFactory;
import uk.nhs.ciao.docs.parser.UnsupportedDocumentTypeException;
import uk.nhs.ciao.docs.parser.extractor.PropertiesExtractor;
import uk.nhs.ciao.docs.parser.kent.KentPropertiesExtractorFactory;

/**
 * Example class which runs the Kent EDNExtractor against input resources.
 * <p>
 * The intermediate TIKA XHTML and the final extractor JSON are printed to standard out.
 */
public class HtmlEDNExtractorExample {
	public static void main(final String[] args) throws Exception {
		final HtmlEDNExtractorExample example = new HtmlEDNExtractorExample();
		
		for (final String name: Arrays.asList("Example6.htm", "Example7.htm", "Example8.htm", "Example9.htm", "Example10.htm")) {
			System.out.println("##################################");
			System.out.println("#");
			System.out.println("# " + name);
			System.out.println("#");
			System.out.println("##################################");
			
			System.out.println();
			example.parseResource("../kings/input/" + name);
			System.out.println();
		}
	}
	
	private DocumentParser parser;
	
	public HtmlEDNExtractorExample() throws XPathExpressionException, ParserConfigurationException {
		parser = new TikaDocumentParser(TikaParserFactory.createParser(),
				new WiretapPropertiesExtractor(KentPropertiesExtractorFactory.createEDNExtractor()));
	}
	
	public void parseResource(final String resourceName) throws UnsupportedDocumentTypeException, IOException {
		final InputStream resource = getClass().getResourceAsStream(resourceName);
		try {
			final Map<String, Object> properties = parser.parseDocument(resource);
			final ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			
			System.out.println();		
			System.out.println(mapper.writeValueAsString(properties));
		} finally {
			Closeables.closeQuietly(resource);
		}
	}
	
	private static class WiretapPropertiesExtractor implements PropertiesExtractor<Document> {
		private final PropertiesExtractor<Document> delegate;
		private final TransformerFactory factory;
		
		public WiretapPropertiesExtractor(final PropertiesExtractor<Document> delegate) {
			this.delegate = Preconditions.checkNotNull(delegate);
			this.factory = TransformerFactory.newInstance();
		}
		
		@Override
		public Map<String, Object> extractProperties(final Document document)
				throws UnsupportedDocumentTypeException {
			try {
				
			    final Transformer transformer = factory.newTransformer();
			    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			    
			    final DOMSource source = new DOMSource(document);
			    final StreamResult result = new StreamResult(System.out);			    
				transformer.transform(source, result);
			} catch (final TransformerException e) {
				throw new UnsupportedDocumentTypeException(e);
			}
			
			return delegate.extractProperties(document);
		}
	}
}
