package org.openlca.yaml;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.yaml.Document;

public class FlowPropertyTest {

	private FlowProperty property;

	@Before
	public void setUp() {
		InputStream is = getClass().getResourceAsStream("FlowPropertyTest.yaml");
		Document doc = Document.read(is);
		property = doc.flowProperties.get(0);
	}

	@Test
	public void testAttributes() {
		Assert.assertEquals("Mass", property.getName());
	}

	@Test
	public void testUnitGroup() {
		UnitGroup group = property.getUnitGroup();
		Assert.assertNotNull(group);
		Assert.assertEquals("Units of mass", group.getName());
	}
}