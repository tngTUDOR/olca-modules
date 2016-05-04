package org.openlca.core.matrix.cache;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CalcImpactFactor;
import org.openlca.core.matrix.dbtables.ConversionTable;
import org.openlca.core.model.ModelType;

import com.google.common.cache.LoadingCache;

@Deprecated
public final class MatrixCache {

	private final boolean lazy;
	private final IDatabase database;

	private ConversionTable conversionTable;
	private ProcessTable processTable;

	private LoadingCache<Long, List<CalcImpactFactor>> impactCache;

	public static MatrixCache createEager(IDatabase database) {
		return new MatrixCache(database, false);
	}

	public static MatrixCache createLazy(IDatabase database) {
		return new MatrixCache(database, true);
	}

	private MatrixCache(IDatabase database, boolean lazy) {
		this.database = database;
		this.lazy = lazy;
		if (!lazy) {
			conversionTable = ConversionTable.create(database);
			processTable = ProcessTable.create(database);
			impactCache = ImpactFactorCache.create(database, conversionTable);
		}
	}

	public IDatabase getDatabase() {
		return database;
	}

	private ConversionTable getConversionTable() {
		if (conversionTable == null)
			conversionTable = ConversionTable.create(database);
		return conversionTable;
	}

	public ProcessTable getProcessTable() {
		if (processTable == null)
			processTable = ProcessTable.create(database);
		return processTable;
	}

	public LoadingCache<Long, List<CalcImpactFactor>> getImpactCache() {
		if (impactCache == null)
			impactCache = ImpactFactorCache.create(database,
					getConversionTable());
		return impactCache;
	}

	public synchronized void evictAll() {
		if (conversionTable != null)
			conversionTable.reload();
		if (processTable != null)
			processTable.reload();
		if (impactCache != null)
			impactCache.invalidateAll();
	}

	public synchronized void evict(ModelType type, long id) {
		if (type == null)
			return;
		switch (type) {
		case FLOW:
			baseEviction();
			break;
		case FLOW_PROPERTY:
			baseEviction();
			break;
		case IMPACT_CATEGORY:
			if (impactCache != null)
				impactCache.invalidate(id);
			break;
		case IMPACT_METHOD:
			if (impactCache != null)
				impactCache.invalidateAll();
			break;
		case PROCESS:
			evictProcess(id);
			break;
		case UNIT:
			baseEviction();
			break;
		case UNIT_GROUP:
			baseEviction();
			break;
		default:
			break;
		}
	}

	private void baseEviction() {
		if (conversionTable == null)
			return; // there cannot be an exchange or impact cache
		if (lazy) {
			conversionTable = null;
			impactCache = null;
		} else {
			conversionTable.reload();
			impactCache.invalidateAll();
		}
	}

	private void evictProcess(long id) {
		reloadProcessTable();
	}

	private void reloadProcessTable() {
		if (lazy)
			processTable = null;
		else
			processTable.reload();
	}

	public synchronized void registerNew(ModelType type, long id) {
		if (type == null)
			return;
		switch (type) {
		case FLOW:
			baseEviction();
			break;
		case FLOW_PROPERTY:
			baseEviction();
			break;
		case PROCESS:
			reloadProcessTable();
			break;
		case UNIT:
			baseEviction();
			break;
		case UNIT_GROUP:
			baseEviction();
			break;
		default:
			break;
		}
	}

}
