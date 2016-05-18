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
	private TechGraph g;

	@Before
	public void setUp() {
		Tests.clearDb();
		InputStream is = this.getClass().getResourceAsStream(
				"TechGraphTest.yaml");
		Document.read(is).sync(db);
		TechGraphBuilder builder = new TechGraphBuilder(db,
				ProcessType.UNIT_PROCESS);
		LongPair ref = f("Ref. process", "Product 2");
		g = builder.build(ref);
	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testIndex() {
		Assert.assertEquals(3, g.index.size());
		g.index.contains(f("Ref. process", "Product 2"));
		g.index.contains(f("Supplier", "Product 1"));
		g.index.contains(f("Waste treatment", "Waste"));
		Assert.assertEquals(g.index.getFlowAt(0), f("Ref. process", "Product 2"));
	}

	@Test
	public void testLinkFlows() {
		Assert.assertEquals(2, g.getLinkFlows().size());
		Assert.assertTrue(g.isLinked(f("Ref. process", "Product 1")));
		Assert.assertTrue(g.isLinked(f("Ref. process", "Waste")));
	}

	@Test
	public void testLinks() {
		Assert.assertNull(g.getIndexFlow(f("Ref. process", "Product 2")));
		Assert.assertEquals(f("Supplier", "Product 1"),
				g.getIndexFlow(f("Ref. process", "Product 1")));
		Assert.assertEquals(f("Waste treatment", "Waste"),
				g.getIndexFlow(f("Ref. process", "Waste")));
	}

	private LongPair f(String processName, String flowName) {
		Process p = Tests.getProcess(processName);
		Exchange e = Tests.getExchange(p, flowName);
		return LongPair.of(p.getId(), e.getId());
	}
}