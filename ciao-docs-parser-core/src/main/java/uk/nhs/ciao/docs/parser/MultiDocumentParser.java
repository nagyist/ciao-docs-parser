package uk.nhs.ciao.docs.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.ciao.io.MultiCauseIOException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

/**
 * A document parser which attempts to parse the document using multiple
 * delegate parsers.
 * <p>
 * The parsers are attempted in registration order until one parser completes a successful
 * parse.
 */
public final class MultiDocumentParser implements DocumentParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiDocumentParser.class);
	
	private final Set<DocumentParser> parsers;
	
	public MultiDocumentParser() {
		parsers = Sets.newLinkedHashSet();
	}
	
	public MultiDocumentParser(final DocumentParser... parsers) {
		this();
		addParsers(parsers);
	}
	
	public void addParser(final DocumentParser parser) {
		if (parser != null) {
			parsers.add(parser);
		}
	}
	
	public void addParsers(final Iterable<? extends DocumentParser> parsers) {
		for (final DocumentParser parser: parsers) {
			addParser(parser);
		}
	}
	
	public void addParsers(final DocumentParser... parsers) {
		for (final DocumentParser parser: parsers) {
			addParser(parser);
		}
	}
	
	@Override
	public Map<String, Object> parseDocument(final InputStream in) throws UnsupportedDocumentTypeException, IOException {
		if (parsers.isEmpty()) {
			throw new UnsupportedDocumentTypeException("No parsers are available");
		} else if (parsers.size() == 1) {
			final DocumentParser parser = parsers.iterator().next();
			return parser.parseDocument(in);
		}
		
		// cache the input stream (multiple reads may be required)
		final ByteArrayInputStream cachedInputStream = cacheInputStream(in);
		
		boolean onlyThrewUnsupportedDocumentType = true;
		final List<Exception> suppressedExceptions = Lists.newArrayList();
		for (final DocumentParser parser: parsers) {
			try {
				cachedInputStream.reset();
				return parser.parseDocument(cachedInputStream);
			} catch (final UnsupportedDocumentTypeException e) {
				LOGGER.trace("Parser {} does not support document type", parser, e);
				suppressedExceptions.add(e);				
			} catch (final Exception e) {
				LOGGER.trace("Parser {} failed to parse the document", parser, e);
				onlyThrewUnsupportedDocumentType = false;
				suppressedExceptions.add(e);
			}
		}

		if (onlyThrewUnsupportedDocumentType) {
			throw new UnsupportedDocumentTypeException("No parsers support the type of document");
		} else {
			throw new MultiCauseIOException("All parsers failed to parse the document", suppressedExceptions);
		}
	}
	
	/**
	 * Caches the input stream in memory.
	 * <p>
	 * This allows the input stream content to be read multiple times.
	 */
	private ByteArrayInputStream cacheInputStream(final InputStream in) throws IOException {
		final byte[] bytes = ByteStreams.toByteArray(in);
		return new ByteArrayInputStream(bytes);
	}
}
