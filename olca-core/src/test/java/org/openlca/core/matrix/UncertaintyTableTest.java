package org.openlca.core.matrix;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.dbtables.ExchangeTable;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.matrix.dbtables.PicoUncertainty;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;

/**
 * Tests if we get all uncertainty information from the matrix-table loaders.
 */
public class UncertaintyTableTest {

	private IDatabase db = Tests.getDb();

	@Test
	public void testForExchange() throws Exception {

		Exchange exchange = new Exchange();
		exchange.setUncertainty(create());
		Process process = new Process();
		process.getExchanges().add(exchange);
		ProcessDao dao = new ProcessDao(db);
		dao.insert(process);

		long id = process.getId();
		ExchangeTable table = ExchangeTable.createWithUncertainty(db);
		List<PicoExchange> exchanges = table.get(Arrays.asList(id)).get(id);
		check(exchanges.get(0).uncertainty);
		dao.delete(process);
	}

	// TODO: add test for LCIA factors
	// @Test
	// public void testForImpactFactor() throws Exception {
	// ImpactFactor factor = new ImpactFactor();
	// factor.setUncertainty(createUncertainty());
	// ImpactCategory category = new ImpactCategory();
	// category.getImpactFactors().add(factor);
	// ImpactCategoryDao dao = new ImpactCategoryDao(database);
	// dao.insert(category);
	// List<PicoImpactFactor> factors = cache.getImpactCache().get(
	// category.getId());
	// checkFactor(factors.get(0));
	// dao.delete(category);
	// }

	private void check(PicoUncertainty u) {
		Assert.assertEquals(1, u.parameter1, 1e-16);
		Assert.assertEquals(2, u.parameter2, 1e-16);
		Assert.assertEquals(3, u.parameter3, 1e-16);
		Assert.assertEquals("0.5 * 2", u.parameter1Formula);
		Assert.assertEquals("4 / 2", u.parameter2Formula);
		Assert.assertEquals("4 / 2", u.parameter3Formula);
	}

	private Uncertainty create() {
		Uncertainty uncertainty = Uncertainty.triangle(1, 2, 3);
		uncertainty.setParameter1Formula("0.5 * 2");
		uncertainty.setParameter2Formula("4 / 2");
		uncertainty.setParameter3Formula("4 / 2");
		return uncertainty;
	}

}
