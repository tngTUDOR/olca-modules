package org.openlca.core.matrix.dbtables;

import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ProcessType;
import org.openlca.yaml.Document;

public class ProviderTableTest {

	private IDatabase db = Tests.getDb();

	@Before
	public void setUp() {
		Tests.clearDb();
		InputStream is = this.getClass().getResourceAsStream(
				"ProviderTableTest.yaml");
		Document.read(is).sync(db);
	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testUnitType() {
		ProviderTable table = ProviderTable.create(db, ProcessType.UNIT_PROCESS);
		long productProvider = table.get(exchange("Product")).getFirst();
		Assert.assertEquals(processId("Producer"), productProvider);
		long wasteTreatment = table.get(exchange("Waste")).getFirst();
		Assert.assertEquals(processId("Waste treatment"), wasteTreatment);
		Assert.assertNull(table.get(exchange("CO2")));
	}

	@Test
	public void testSystemType() {
		ProviderTable table = ProviderTable.create(db, ProcessType.LCI_RESULT);
		long productProvider = table.get(exchange("Product")).getFirst();
		Assert.assertEquals(processId("Producer (LCI)"), productProvider);
		long wasteTreatment = table.get(exchange("Waste")).getFirst();
		Assert.assertEquals(processId("Waste treatment"), wasteTreatment);
		Assert.assertNull(table.get(exchange("CO2")));
	}

	private PicoExchange exchange(String flowName) {
		FlowDao dao = new FlowDao(db);
		Flow flow = dao.getForName(flowName).get(0);
		PicoExchange e = new PicoExchange();
		e.flowID = flow.getId();
		return e;
	}

	private long processId(String processName) {
		ProcessDao dao = new ProcessDao(db);
		return dao.getForName(processName).get(0).getId();
	}

}
