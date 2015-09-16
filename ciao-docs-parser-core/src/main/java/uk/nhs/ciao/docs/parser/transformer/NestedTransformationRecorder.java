package uk.nhs.ciao.docs.parser.transformer;

import joptsimple.internal.Strings;

import com.google.common.base.Preconditions;

public class NestedTransformationRecorder implements TransformationRecorder {
	private final String fromPrefix;
	private final TransformationRecorder delegate;
	
	public NestedTransformationRecorder(final String fromPrefix, final TransformationRecorder delegate) {
		this.fromPrefix = Preconditions.checkNotNull(fromPrefix);
		Preconditions.checkArgument(!fromPrefix.isEmpty());
		
		this.delegate = Preconditions.checkNotNull(delegate);
	}
	
	@Override
	public void record(final String from, final String to) {
		if (!Strings.isNullOrEmpty(from)) {
			delegate.record(PropertyName.valueOf(fromPrefix, from), to);
		}
	}
}
