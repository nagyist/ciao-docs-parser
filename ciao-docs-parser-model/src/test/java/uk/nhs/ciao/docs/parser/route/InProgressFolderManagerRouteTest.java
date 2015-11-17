package uk.nhs.ciao.docs.parser.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.camel.CamelUtils;
import uk.nhs.ciao.docs.parser.route.InProgressFolderManagerRoute.Action;
import uk.nhs.ciao.docs.parser.route.InProgressFolderManagerRoute.EventType;
import uk.nhs.ciao.docs.parser.route.InProgressFolderManagerRoute.FileType;
import uk.nhs.ciao.docs.parser.route.InProgressFolderManagerRoute.Header;
import uk.nhs.ciao.docs.parser.route.InProgressFolderManagerRoute.MessageType;
import uk.nhs.ciao.util.Clock;

/**
 * Tests for {@link InProgressFolderManagerRoute}
 */
public class InProgressFolderManagerRouteTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(InProgressFolderManagerRoute.class);
	
	private CamelContext context;
	private ProducerTemplate producerTemplate;
	private InProgressFolderManagerRoute route;
	private Clock clock;
	private String timestamp;
	private MockEndpoint overrideExistingEndpoint;
	private MockEndpoint failExistingEndpoint;
	
	@Before
	public void setup() throws Exception {
		context = new DefaultCamelContext();
		producerTemplate = new DefaultProducerTemplate(context);
		
		route = new InProgressFolderManagerRoute();
		route.setInternalRoutePrefix("manager");
		route.setInProgressFolderManagerUri("direct:folder-manager");
		route.setInProgressFolderRootUri("mock:root-folder");
		
		clock = Mockito.mock(Clock.class, Mockito.CALLS_REAL_METHODS);
		route.setClock(clock);
		
		// Simulate running at a known date/time
		Mockito.when(clock.getMillis()).thenReturn(1447767609936L);
		timestamp = "20151117-134009936";
		
		context.addRoutes(route);
		
		overrideExistingEndpoint = MockEndpoint.resolve(context, "mock:root-folder?fileExist=Override");
		failExistingEndpoint = MockEndpoint.resolve(context, "mock:root-folder?fileExist=Fail");
		
		context.start();
		producerTemplate.start();
	}
	
	@After
	public void tearDown() {
		CamelUtils.stopQuietly(producerTemplate, context);
	}
	
	@Test
	public void testStoreControlFile() throws Exception {
		final String content = "text-content";
		overrideExistingEndpoint.expectedBodiesReceived(content);
		overrideExistingEndpoint.expectedHeaderReceived(Exchange.FILE_NAME, "123/control/example.txt");
		
		storeControlFile("123", "example.txt", content);
		
		MockEndpoint.assertIsSatisfied(context);
	}
	
	@Test
	public void testStoreEventFile() throws Exception {
		final String content = "event-content";
		failExistingEndpoint.expectedBodiesReceived(content);
		failExistingEndpoint.expectedHeaderReceived(Exchange.FILE_NAME, "1234/events/" + timestamp + "-bus-ack-sent");
		
		storeEventFile("1234", MessageType.BUSINESS_ACK, EventType.MESSAGE_SENT, content);
		
		MockEndpoint.assertIsSatisfied(context);
	}
	
	@Test
	public void testStoreEventFileWithRetries() throws Exception {
		final String content = "event-content";
		failExistingEndpoint.whenExchangeReceived(1, new Processor() {
			@Override
			public void process(final Exchange exchange) throws Exception {
				final String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
				LOGGER.info("Simulating file exists exception for: " + fileName);
				throw new Exception("File exists: " + fileName);
			}
		});
		failExistingEndpoint.expectedBodiesReceived(content, content); // message should be sent twice
		failExistingEndpoint.expectedHeaderReceived(Exchange.FILE_NAME, "1234/events/" + timestamp + "-bus-ack-sent");
		
		storeEventFile("1234", MessageType.BUSINESS_ACK, EventType.MESSAGE_SENT, content);
		
		MockEndpoint.assertIsSatisfied(context);
	}
	
	private void storeEventFile(final String correlationId, final String messageType, final String eventType,
			final Object body) throws Exception {
		final Exchange exchange = new DefaultExchange(context);
		
		final Message message = exchange.getIn();
		message.setHeader(Exchange.CORRELATION_ID, correlationId);
		message.setHeader(Header.ACTION, Action.STORE);
		message.setHeader(Header.FILE_TYPE, FileType.EVENT);
		message.setHeader(Header.EVENT_TYPE, eventType);
		message.setHeader(Exchange.FILE_NAME, messageType);
		message.setBody(body);
		
		send(exchange);
	}
	
	private void storeControlFile(final String correlationId, final String fileName, final Object body) throws Exception {
		final Exchange exchange = new DefaultExchange(context);
		
		final Message message = exchange.getIn();
		message.setHeader(Exchange.CORRELATION_ID, correlationId);
		message.setHeader(Header.ACTION, Action.STORE);
		message.setHeader(Header.FILE_TYPE, FileType.CONTROL);
		message.setHeader(Exchange.FILE_NAME, fileName);
		message.setBody(body);
		
		send(exchange);
	}
	
	private void send(final Exchange exchange) throws Exception {
		exchange.setPattern(ExchangePattern.InOut);
		producerTemplate.send("direct:folder-manager", exchange);
		if (exchange.getException() != null) {
			throw exchange.getException();
		}
	}
}
