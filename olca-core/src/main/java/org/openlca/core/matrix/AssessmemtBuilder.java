package org.openlca.core.matrix;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.dbtables.ImpactFactorTable;
import org.openlca.core.matrix.dbtables.PicoImpactFactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AssessmemtBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private final long methodId;
	private final FlowIndex flowIndex;

	AssessmemtBuilder(IDatabase db, long impactMethodId, FlowIndex flowIndex) {
		this.db = db;
		this.methodId = impactMethodId;
		this.flowIndex = flowIndex;
	}

	Assessment build() {
		log.trace("Build impact factor matrix for method {}", methodId);
		ImpactFactorTable factors = ImpactFactorTable.create(db, methodId);
		LongIndex index = buildCategoryIndex(factors);
		if (index.isEmpty() || flowIndex.isEmpty())
			return null;
		Assessment table = new Assessment();
		table.categoryIndex = index;
		table.flowIndex = flowIndex;
		ImpactFactorMatrix matrix = new ImpactFactorMatrix(index.size(),
				flowIndex.size());
		table.factorMatrix = matrix;
		fill(factors, index, matrix);
		log.trace("Impact factor matrix ready");
		return table;
	}

	private LongIndex buildCategoryIndex(ImpactFactorTable factors) {
		LongIndex index = new LongIndex();
		for (long categoryID : factors.getCategoryIds()) {
			index.put(categoryID);
		}
		return index;
	}

	private void fill(ImpactFactorTable table, LongIndex index, ImpactFactorMatrix matrix) {
		for (int row = 0; row < index.size(); row++) {
			long categoryId = index.getKeyAt(row);
			List<PicoImpactFactor> factors = table.get(categoryId);
			if (factors == null)
				continue;
			for (PicoImpactFactor factor : factors) {
				long flowId = factor.flowID;
				int col = flowIndex.getIndex(flowId);
				if (col < 0)
					continue;
				boolean input = flowIndex.isInput(flowId);
				ImpactFactorCell cell = new ImpactFactorCell(factor, methodId,
						input);
				matrix.setEntry(row, col, cell);
			}
		}
	}

}
