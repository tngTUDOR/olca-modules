package org.openlca.yaml;

import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
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
}
