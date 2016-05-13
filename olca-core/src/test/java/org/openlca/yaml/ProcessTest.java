package org.openlca.yaml;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;

public class ProcessTest {

	private Process process;

	@Before
	public void setUp() {
		InputStream is = getClass().getResourceAsStream("ProcessTest.yaml");
		Document doc = Document.read(is);
		process = doc.processes.get(0);
	}

	@Test
	public void testAttributes() {
		Assert.assertEquals("ABS", process.getName());
		Assert.assertEquals(ProcessType.LCI_RESULT, process.getProcessType());
	}

	@Test
	public void testExchanges() {
		Assert.assertEquals(3, process.getExchanges().size());
		for (Exchange e : process.getExchanges()) {
			Assert.assertEquals("kg", e.getUnit().getName());
			Assert.assertNotNull(e.getFlow());
			Assert.assertNotNull(e.getFlowPropertyFactor());
			if (e.isInput()) {
				Assert.assertEquals("Crude oil", e.getFlow().getName());
				Assert.assertEquals(0.98, e.getAmountValue(), 1e-16);
			}
		}
	}

}
