package uk.nhs.ciao.io;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Unit tests for {@link MultiCauseIOException}
 */
public class MultiCauseIOExceptionTest {

	@Test
	public void whenASingleCauseIsSpecifiedThenItShouldBeTheMainCause() {
		final IOException singleCause = new IOException();
		final MultiCauseIOException multiException = new MultiCauseIOException("message", Lists.newArrayList(singleCause));
		
		assertEquals(singleCause, multiException.getCause());
	}

	@Test
	public void whenASingleCauseIsSpecifiedThenItShouldBeAddedToTheCauses() {
		final Exception[] singleCause = new Exception[]{
				new IOException()}
		;
		final MultiCauseIOException multiException = new MultiCauseIOException("message", Lists.newArrayList(singleCause));
		
		assertArrayEquals(singleCause, multiException.getCauses());
	}
	
	@Test
	public void whenMultipleCausesAreSpecifiedThenTheyShouldBeAddedToTheCauses() {
		final Exception[] multipleCauses = new Exception[]{
			new IOException(), new IOException()	
		};
		final MultiCauseIOException multiException = new MultiCauseIOException("message", Lists.newArrayList(multipleCauses));
		
		assertArrayEquals(multipleCauses, multiException.getCauses());
	}
}
