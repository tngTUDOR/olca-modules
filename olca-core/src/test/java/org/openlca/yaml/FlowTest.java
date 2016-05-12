package org.openlca.yaml;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.yaml.Document;

public class FlowTest {

	private Flow flow;

	@Before
	public void setUp() {
		InputStream is = getClass().getResourceAsStream("FlowTest.yaml");
		Document doc = Document.read(is);
		flow = doc.flows.get(0);
	}

	@Test
	public void testAttributes() {
		Assert.assertEquals("Steel", flow.getName());
		Assert.assertEquals(FlowType.PRODUCT_FLOW, flow.getFlowType());
	}

	@Test
	public void testFlowProperty() {
		FlowPropertyFactor refFactor = flow.getReferenceFactor();
		Assert.assertEquals("Mass", refFactor.getFlowProperty().getName());
		Assert.assertEquals(1.0, refFactor.getConversionFactor(), 1e-16);
	}
}