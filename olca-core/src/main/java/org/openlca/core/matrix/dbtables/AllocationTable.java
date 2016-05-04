package org.openlca.core.matrix.dbtables;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.AllocationMethod;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Reads and caches all allocation factors and default allocation methods of the
 * processes in the database.
 */
public class AllocationTable {

	private TLongObjectHashMap<List<PicoAllocationFactor>> factors;
	private final TLongObjectHashMap<AllocationMethod> methods;

	private AllocationTable() {
		factors = new TLongObjectHashMap<>();
		methods = new TLongObjectHashMap<>();
	}

	public static AllocationTable create(IDatabase db) {
		AllocationTable table = new AllocationTable();
		table.initMethods(db);
		table.initFactors(db);
		return table;
	}

	private void initMethods(IDatabase db) {
		String sql = "select id, default_allocation_method from tbl_processes";
		try {
			NativeSql.on(db).query(sql, r -> {
				addMethod(r);
				return true;
			});
		} catch (Exception e) {
			String m = "failed to get default allocation methods";
			throw new RuntimeException(m, e);
		}
	}

	private void addMethod(ResultSet r) {
		try {
			long id = r.getLong(1);
			String m = r.getString(2);
			if (m == null)
				return;
			methods.put(id, AllocationMethod.valueOf(m));
		} catch (Exception e) {
			String m = "failed to get process method";
			throw new RuntimeException(m, e);
		}
	}

	private void initFactors(IDatabase db) {
		String sql = "select allocation_type, f_process, f_product, "
				+ "f_exchange, value  from tbl_allocation_factors";
		try {
			NativeSql.on(db).query(sql, r -> {
				addFactor(r);
				return true;
			});
		} catch (Exception e) {
			String m = "Failed to query allocation factors";
			throw new RuntimeException(m, e);
		}
	}

	private void addFactor(ResultSet r) {
		try {
			PicoAllocationFactor f = factor(r);
			List<PicoAllocationFactor> list = factors.get(f.processID);
			if (list == null) {
				list = new ArrayList<>();
				factors.put(f.processID, list);
			}
			list.add(f);
		} catch (Exception e) {
			String m = "Failed to fetch allocation factor";
			throw new RuntimeException(m, e);
		}
	}

	private PicoAllocationFactor factor(ResultSet r) throws Exception {
		PicoAllocationFactor f = new PicoAllocationFactor();
		String typeStr = r.getString(1);
		f.method = AllocationMethod.valueOf(typeStr);
		f.processID = r.getLong(2);
		f.productID = r.getLong(3);
		f.value = r.getDouble(5);
		long exchangeId = r.getLong(4);
		if (!r.wasNull())
			f.exchangeID = exchangeId;
		return f;
	}

	/** Returns all allocation factors for the given process. */
	public List<PicoAllocationFactor> get(long processID) {
		List<PicoAllocationFactor> list = factors.get(processID);
		return list != null ? list : Collections.emptyList();
	}

	/** Returns the default allocation method for the given process. */
	public AllocationMethod getDefaultMethod(long processID) {
		AllocationMethod m = methods.get(processID);
		return m != null ? m : AllocationMethod.NONE;
	}
}
