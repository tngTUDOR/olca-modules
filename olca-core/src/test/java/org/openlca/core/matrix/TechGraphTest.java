package org.openlca.core.matrix;

import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.product.index.TechGraphBuilder;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.yaml.Document;

public class TechGraphTest {

	private IDatabase db = Tests.getDb();
	private TechGraph graph;

	@Before
	public void setUp() {
		Tests.clearDb();
		InputStream is = this.getClass().getResourceAsStream(
				"TechGraphTest.yaml");
		Document.read(is).sync(db);
		TechGraphBuilder builder = new TechGraphBuilder(db,
				ProcessType.UNIT_PROCESS);
		Process refProc = Tests.getProcess("Ref. process");
		Exchange refFlow = Tests.getExchange(refProc, "Product 2");
		LongPair ref = LongPair.of(refProc.getId(), refFlow.getId());
		graph = builder.build(ref);
	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testIndex() {
		Assert.assertEquals(3, graph.index.size());
		indexContains("Ref. process", "Product 2");
		indexContains("Supplier", "Product 1");
		indexContains("Waste treatment", "Waste");
	}

	private void indexContains(String processName, String flowName) {
		Process p = Tests.getProcess(processName);
		Exchange e = Tests.getExchange(p, flowName);
		LongPair flow = LongPair.of(p.getId(), e.getId());
		Assert.assertTrue(graph.index.contains(flow));
	}

}