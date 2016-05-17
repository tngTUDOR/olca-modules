package org.openlca.yaml;

import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class DatabaseSyncTest {

	private IDatabase db = Tests.getDb();

	@Before
	public void setUp() {
		Tests.clearDb();
		InputStream is = getClass().getResourceAsStream("DatabaseSyncTest.yaml");
		Document.read(is).sync(db);
	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testUnitGroup() {
		UnitGroupDao dao = new UnitGroupDao(db);
		List<UnitGroup> list = dao.getAll();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("Units of mass", list.get(0).getName());
	}

	@Test
	public void testFlowProperty() {
		FlowPropertyDao dao = new FlowPropertyDao(db);
		List<FlowProperty> list = dao.getAll();
		Assert.assertEquals(1, list.size());
		FlowProperty p = list.get(0);
		Assert.assertEquals("Mass", p.getName());
		Assert.assertEquals("Units of mass", p.getUnitGroup().getName());
	}

	@Test
	public void testFlow() {
		FlowDao dao = new FlowDao(db);
		List<Flow> list = dao.getAll();
		Assert.assertEquals(2, list.size());
		Flow f = list.get(0);
		Assert.assertEquals("Mass", f.getReferenceFactor()
				.getFlowProperty().getName());
	}

	@Test
	public void testProcess() {
		ProcessDao dao = new ProcessDao(db);
		List<Process> list = dao.getAll();
		Assert.assertEquals(1, list.size());
		Process p = list.get(0);
		Assert.assertEquals("Steel production", p.getName());
		Assert.assertEquals(2, p.getExchanges().size());
	}
}
