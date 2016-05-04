package org.openlca.core.matrix.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.dbtables.PicoAllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Provides allocation factors from the database. The table is backed by a
 * loading cache.
 */
class AllocationCache {

	public static LoadingCache<Long, List<PicoAllocationFactor>> create(
			IDatabase database) {
		return CacheBuilder.newBuilder().build(new FactorLoader(database));
	}

	/**
	 * Loads all allocation factors for a process from the database.
	 */
	private static class FactorLoader extends
			CacheLoader<Long, List<PicoAllocationFactor>> {

		private Logger log = LoggerFactory.getLogger(getClass());
		private IDatabase database;

		public FactorLoader(IDatabase database) {
			this.database = database;
		}

		@Override
		public List<PicoAllocationFactor> load(Long processId) throws Exception {
			log.trace("load allocation factors for process {}", processId);
			try (Connection con = database.createConnection()) {
				String sql = "select * from tbl_allocation_factors where f_process = "
						+ processId;
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				ArrayList<PicoAllocationFactor> factors = new ArrayList<>();
				while (rs.next()) {
					PicoAllocationFactor factor = fetchFactor(rs);
					factors.add(factor);
				}
				stmt.close();
				rs.close();
				return factors;
			} catch (Exception e) {
				log.error("failed to load allocation factors for " + processId,
						e);
				return Collections.emptyList();
			}
		}

		@Override
		public Map<Long, List<PicoAllocationFactor>> loadAll(
				Iterable<? extends Long> processIds) throws Exception {
			log.trace("load allocation factors");
			try (Connection con = database.createConnection()) {
				String sql = "select * from tbl_allocation_factors where f_process in "
						+ CacheUtil.asSql(processIds);
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				Map<Long, List<PicoAllocationFactor>> map = new HashMap<>();
				while (rs.next()) {
					PicoAllocationFactor factor = fetchFactor(rs);
					CacheUtil.addListEntry(map, factor, factor.processID);
				}
				stmt.close();
				rs.close();
				CacheUtil.fillEmptyEntries(processIds, map);
				return map;
			} catch (Exception e) {
				log.error("failed to load allocation factors", e);
				return Collections.emptyMap();
			}
		}

		private PicoAllocationFactor fetchFactor(ResultSet rs) throws Exception {
			PicoAllocationFactor factor = new PicoAllocationFactor();
			String typeStr = rs.getString("allocation_type");
			factor.method = AllocationMethod.valueOf(typeStr);
			factor.processID = rs.getLong("f_process");
			factor.productID = rs.getLong("f_product");
			factor.value = rs.getDouble("value");
			long exchangeId = rs.getLong("f_exchange");
			if (!rs.wasNull())
				factor.exchangeID = exchangeId;
			return factor;
		}

	}

}
