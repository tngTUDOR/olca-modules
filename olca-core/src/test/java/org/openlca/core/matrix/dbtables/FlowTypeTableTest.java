package org.openlca.core.matrix.dbtables;

import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.yaml.Document;

public class FlowTypeTableTest {

	private IDatabase db = Tests.getDb();

	@Before
	public void setUp() {
		Tests.clearDb();
		InputStream is = this.getClass().getResourceAsStream(
				"FlowTypeTableTest.yaml");
		Document.read(is).sync(db);
	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testTypes() {
		FlowTypeTable table = FlowTypeTable.create(db);
		checkType(table, "Steel", FlowType.PRODUCT_FLOW);
		checkType(table, "CO2", FlowType.ELEMENTARY_FLOW);
		checkType(table, "Waste paper", FlowType.WASTE_FLOW);
	}

	private void checkType(FlowTypeTable table, String name, FlowType type) {
		FlowDao dao = new FlowDao(db);
		Flow flow = dao.getForName(name).get(0);
		Assert.assertEquals(type, table.getType(flow.getId()));
	}
}
