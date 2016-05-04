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
 * Reads and caches all allocation factors from the database.
 */
public class AllocationTable {

	private TLongObjectHashMap<List<PicoAllocationFactor>> map;

	private AllocationTable() {
		map = new TLongObjectHashMap<>();
	}

	public static AllocationTable create(IDatabase db) {
		AllocationTable table = new AllocationTable();
		table.init(db);
		return table;
	}

	private void init(IDatabase db) {
		String sql = "select allocation_type, f_process, f_product, "
				+ "f_exchange, value  from tbl_allocation_factors";
		try {
			NativeSql.on(db).query(sql, r -> {
				add(r);
				return true;
			});
		} catch (Exception e) {
			String m = "Failed to query allocation factors";
			throw new RuntimeException(m, e);
		}
	}

	private void add(ResultSet r) {
		try {
			PicoAllocationFactor f = factor(r);
			List<PicoAllocationFactor> list = map.get(f.processID);
			if (list == null) {
				list = new ArrayList<>();
				map.put(f.processID, list);
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
		List<PicoAllocationFactor> list = map.get(processID);
		return list != null ? list : Collections.emptyList();
	}
}
