package org.openlca.core.matrix.dbtables;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.UncertaintyType;

import gnu.trove.map.hash.TLongObjectHashMap;

public class ExchangeTable {

	private Query query;
	private TLongObjectHashMap<ArrayList<PicoExchange>> cache;

	private ExchangeTable(IDatabase db, boolean withUncertainty) {
		query = new Query(db, withUncertainty);
		this.cache = new TLongObjectHashMap<>();
	}

	public static ExchangeTable create(IDatabase db) {
		return new ExchangeTable(db, false);
	}

	public static ExchangeTable createWithUncertainty(IDatabase db) {
		return new ExchangeTable(db, true);
	}

	public Map<Long, List<PicoExchange>> get(Collection<Long> processIDs) {
		Map<Long, List<PicoExchange>> result = new HashMap<>();
		if (processIDs == null || processIDs.isEmpty())
			return result;
		ArrayList<Long> querySet = new ArrayList<>();
		for (Long processID : processIDs) {
			ArrayList<PicoExchange> cachedList = cache.get(processID);
			if (cachedList == null) {
				querySet.add(processID);
			} else {
				result.put(processID, cachedList);
			}
		}
		if (querySet.isEmpty())
			return result;
		fillCache(querySet);
		for (Long processID : querySet) {
			ArrayList<PicoExchange> cachedList = cache.get(processID);
			if (cachedList == null) {
				result.put(processID, new ArrayList<>());
			} else {
				result.put(processID, cachedList);
			}
		}
		return result;
	}

	private void fillCache(List<Long> processIDs) {
		String sql = "SELECT id, f_owner, f_flow, f_default_provider, f_currency, "
				+ "f_flow_property_factor, f_unit, resulting_amount_value, cost_value, "
				+ "resulting_amount_formula, cost_formula, is_input, avoided_product ";
		if (query.withUncertainty) {
			sql += ", distribution_type, parameter1_value, parameter2_value, parameter3_value, "
					+ "parameter1_formula, parameter2_formula, parameter3_formula ";
		}
		sql += "from tbl_exchanges where f_owner in " + Util.toSql(processIDs);
		query.exec(sql, e -> {
			ArrayList<PicoExchange> list = cache.get(e.processID);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(e);
		});
	}

	/**
	 * Iterates over each exchange in the exchanges table. This function is used
	 * to get the providers of product outputs and waste inputs (treatments)
	 * from the database.
	 */
	public static void fullScan(IDatabase db, Consumer<PicoExchange> fn) {
		String sql = "SELECT id, f_owner, f_flow, f_default_provider, f_currency, "
				+ "f_flow_property_factor, f_unit, resulting_amount_value, cost_value, "
				+ "resulting_amount_formula, cost_formula, is_input, avoided_product "
				+ "from tbl_exchanges";
		new Query(db, false).exec(sql, fn);
	}

	private static class Query {

		IDatabase db;
		ConversionTable conversions;
		FlowTypeTable flowTypes;
		boolean withUncertainty;

		Query(IDatabase db, boolean withUncertainty) {
			this.db = db;
			this.withUncertainty = withUncertainty;
			conversions = ConversionTable.create(db);
			flowTypes = FlowTypeTable.create(db);
		}

		void exec(String sql, Consumer<PicoExchange> fn) {
			try {
				NativeSql.on(db).query(sql, r -> {
					try {
						PicoExchange e = read(r);
						fn.accept(e);
						return true;
					} catch (Exception e) {
						throw new RuntimeException("failed to read exchange", e);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException("failed to scan exchange table", e);
			}
		}

		private PicoExchange read(ResultSet r) throws Exception {

			PicoExchange e = new PicoExchange();

			e.exchangeID = r.getLong(1);
			e.processID = r.getLong(2);
			e.flowID = r.getLong(3);
			e.flowType = flowTypes.getType(e.flowID);
			e.providerID = r.getLong(4);
			e.currencyID = r.getLong(5);

			long propertyID = r.getLong(6);
			long unitID = r.getLong(7);
			e.conversionFactor = conversions.getUnitFactor(unitID)
					/ conversions.getPropertyFactor(propertyID);
			e.amount = r.getDouble(8) * e.conversionFactor;
			e.costValue = r.getDouble(9);

			e.amountFormula = r.getString(10);
			e.costFormula = r.getString(11);
			e.isInput = r.getBoolean(12);
			e.isAvoidedProduct = r.getBoolean(13);

			if (withUncertainty) {
				uncertainty(r, e);
			}
			return e;
		}

		private void uncertainty(ResultSet r, PicoExchange e) throws Exception {
			int type = r.getInt(14);
			if (r.wasNull())
				return;
			PicoUncertainty u = new PicoUncertainty();
			e.uncertainty = u;
			u.type = UncertaintyType.values()[type];
			u.parameter1 = r.getDouble(15);
			u.parameter2 = r.getDouble(16);
			u.parameter3 = r.getDouble(17);
			u.parameter1Formula = r.getString(18);
			u.parameter2Formula = r.getString(19);
			u.parameter3Formula = r.getString(20);
		}
	}
}
