package uk.nhs.ciao.docs.parser.transformer;

import uk.nhs.ciao.docs.parser.PropertyName;

import com.google.common.base.Preconditions;

public class NestedTransformationRecorder implements TransformationRecorder {
	private final PropertyName fromPrefix;
	private final TransformationRecorder delegate;
	
	public NestedTransformationRecorder(final PropertyName fromPrefix, final TransformationRecorder delegate) {
		this.fromPrefix = Preconditions.checkNotNull(fromPrefix);
		Preconditions.checkArgument(!fromPrefix.isRoot());
		
		this.delegate = Preconditions.checkNotNull(delegate);
	}
	
	@Override
	public void record(final PropertyName from, final PropertyName to) {
		if (from != null) {
			delegate.record(fromPrefix.getChild(from), to);
		}
	}
}
