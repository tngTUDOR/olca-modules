package org.openlca.core.matrix;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.product.index.TechGraphBuilder;
import org.openlca.core.model.ProcessType;
import org.openlca.yaml.Document;

public class TechGraphTest {

	private IDatabase db = Tests.getDb();

	@Before
	public void setUp() {
		Tests.clearDb();
		InputStream is = this.getClass().getResourceAsStream(
				"TechGraphTest.yaml");
		Document.read(is).sync(db);
	}

	@After
	public void tearDown() {
		Tests.clearDb();
	}

	@Test
	public void testLinks() {
		TechGraphBuilder builder = new TechGraphBuilder(db,
				ProcessType.UNIT_PROCESS);

	}

}