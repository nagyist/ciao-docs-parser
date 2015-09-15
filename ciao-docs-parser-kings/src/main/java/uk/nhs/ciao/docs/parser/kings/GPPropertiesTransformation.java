package uk.nhs.ciao.docs.parser.kings;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import uk.nhs.ciao.docs.parser.transformer.MappedProperties;
import uk.nhs.ciao.docs.parser.transformer.PropertiesTransformation;
import uk.nhs.ciao.docs.parser.transformer.PropertyMutator;
import uk.nhs.ciao.docs.parser.transformer.PropertyName;

/**
 * Custom {@link PropertiesTransformation} to handle the mapping of incoming
 * GP lines (as List), to transfer of care fields.
 * <p>
 * The expected format is
 * 
 * <ul>
 * <li>${GP name} -> *ignored*</li>
 * <li>${GP practice name} -> usualGPOrgName
 * <li>${address line 1} -> usualGPAddress.addressLine
 * <li>...
 * <li>${address line N} -> usualGPAddress.addressLine
 * <li>${postcode} -> usualGPAddress.postcode */
public class GPPropertiesTransformation implements PropertiesTransformation {
	private final PropertyMutator orgName = new PropertyMutator("usualGPOrgName");
	private final PropertyMutator addressLines = new PropertyMutator("usualGPAddress.addressLine");
	private final PropertyMutator postcode = new PropertyMutator("usualGPAddress.postcode");
	private final PropertyName gp = PropertyName.valueOf("GP");
	
	@Override
	public void apply(final Map<String, Object> source, final MappedProperties destination) {
		
		final Object candidate = source.get("GP");
		if (!(candidate instanceof List<?>)) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		final List<String> lines = (List<String>)candidate;
		if (lines.size() < 2) {
			return;
		}
		
		orgName.set(destination, gp.getChild(1), lines.get(1));
		if (lines.size() < 3) {
			return;
		}
		
		if (lines.size() > 3) {
			final List<String> addressLines = Lists.newArrayList(lines.subList(2, lines.size() - 1));
			final List<PropertyName> sources = Lists.newArrayList();
			for (int index = 2; index < lines.size() - 1; index++) {
				sources.add(gp.getChild(index));
			}
			
			this.addressLines.set(destination, sources, addressLines);
		}
		
		postcode.set(destination, gp.getChild(lines.size() - 1), lines.get(lines.size() - 1));
	}
}
