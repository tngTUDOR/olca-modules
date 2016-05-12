package org.openlca.core.matrix.dbtables;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.UncertaintyType;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Loads all LCIA factors of a method into memory.
 */
public class ImpactFactorTable {

	private TLongObjectHashMap<ArrayList<PicoImpactFactor>> factors;

	private ImpactFactorTable(TLongObjectHashMap<ArrayList<PicoImpactFactor>> m) {
		factors = m;
	}

	public static ImpactFactorTable create(IDatabase db, long methodId) {
		return new Builder(db, methodId, false).create();
	}

	public static ImpactFactorTable createWithUncertainty(IDatabase db,
			long methodId) {
		return new Builder(null, methodId, true).create();
	}

	public List<PicoImpactFactor> get(long categoryId) {
		List<PicoImpactFactor> list = factors.get(categoryId);
		if (list == null)
			return Collections.emptyList();
		else
			return list;
	}

	public long[] getCategoryIds() {
		return factors.keys();
	}

	private static class Builder {

		IDatabase db;
		ConversionTable conversions;
		long methodId;
		boolean withUncertainty;

		TLongObjectHashMap<ArrayList<PicoImpactFactor>> m;

		Builder(IDatabase db, long methodId, boolean withUncertainty) {
			this.db = db;
			conversions = ConversionTable.create(db);
			this.methodId = methodId;
			this.withUncertainty = withUncertainty;
			m = new TLongObjectHashMap<>();
		}

		ImpactFactorTable create() {
			List<Long> categoryIds = queryCategoryIds();
			if (categoryIds.isEmpty())
				return new ImpactFactorTable(m);
			String sql = query(categoryIds);
			try {
				NativeSql.on(db).query(sql, r -> {
					addResult(r);
					return true;
				});
			} catch (Exception e) {
				throw new RuntimeException("query failed: " + sql, e);
			}
			return new ImpactFactorTable(m);
		}

		String query(List<Long> categoryIds) {
			String q = "SELECT f_impact_category, f_flow, value, formula, "
					+ "f_flow_property_factor, f_unit ";
			if (withUncertainty) {
				q += "distribution_type, parameter1_value, parameter2_value, "
						+ "parameter3_value, parameter1_formula, "
						+ "parameter2_formula, parameter3_formula ";
			}
			q += "FROM tbl_impact_factors WHERE f_impact_category IN "
					+ Util.toSql(categoryIds);
			return q;
		}

		List<Long> queryCategoryIds() {
			List<Long> list = new ArrayList<>();
			String query = "SELECT id FROM tbl_impact_categories WHERE "
					+ "f_impact_method = " + methodId;
			try {
				NativeSql.on(db).query(query, r -> {
					list.add(r.getLong(1));
					return true;
				});
				return list;
			} catch (Exception e) {
				throw new RuntimeException("query failed: " + query, e);
			}
		}

		void addResult(ResultSet r) {
			try {
				PicoImpactFactor f = new PicoImpactFactor();
				f.imactCategoryID = r.getLong(1);
				f.flowID = r.getLong(2);

				long propertyID = r.getLong(5);
				long unitID = r.getLong(6);
				f.conversionFactor = conversions.getPropertyFactor(propertyID)
						/ conversions.getUnitFactor(unitID);
				f.amount = r.getDouble(3) * f.conversionFactor;
				f.amountFormula = r.getString(4);

				if (withUncertainty) {
					uncertainty(r, f);
				}

				ArrayList<PicoImpactFactor> list = m.get(f.imactCategoryID);
				if (list == null) {
					list = new ArrayList<>();
					m.put(f.imactCategoryID, list);
				}
				list.add(f);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private void uncertainty(ResultSet r, PicoImpactFactor f) throws Exception {
			int type = r.getInt(7);
			if (r.wasNull())
				return;
			PicoUncertainty u = new PicoUncertainty();
			f.uncertainty = u;
			u.type = UncertaintyType.values()[type];
			u.parameter1 = r.getDouble(8);
			u.parameter2 = r.getDouble(9);
			u.parameter3 = r.getDouble(10);
			u.parameter1Formula = r.getString(11);
			u.parameter2Formula = r.getString(12);
			u.parameter3Formula = r.getString(13);
		}
	}
}
