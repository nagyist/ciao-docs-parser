package uk.nhs.ciao.docs.parser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class OriginalDocument {
	private final String name;
	private final byte[] body;
	
	public OriginalDocument(final String name, final byte[] body) {
		this.name = Preconditions.checkNotNull(name);
		this.body = Preconditions.checkNotNull(body);
	}
	
	public String getName() {
		return name;
	}
	
	public byte[] getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("size", body.length)
				.toString();
	}
}