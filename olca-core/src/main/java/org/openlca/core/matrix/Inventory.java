package org.openlca.core.matrix;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.model.AllocationMethod;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Contains all the information of the inventory of a complete product system.
 */
public class Inventory {

	public TechGraph techIndex;
	public FlowIndex flowIndex;
	public ExchangeMatrix technologyMatrix;
	public ExchangeMatrix interventionMatrix;
	public AllocationMethod allocationMethod;

	public static Inventory build(TechGraph techIndex, IDatabase db,
			AllocationMethod allocationMethod) {
		return new InventoryBuilder(techIndex, db)
				.allocation(allocationMethod)
				.build();
	}

	public boolean isEmpty() {
		return techIndex == null || techIndex.size() == 0
				|| flowIndex == null || flowIndex.isEmpty()
				|| technologyMatrix == null || technologyMatrix.isEmpty()
				|| interventionMatrix == null || interventionMatrix.isEmpty();
	}

	public InventoryMatrix createMatrix(IMatrixFactory<?> factory) {
		return createMatrix(factory, null);
	}

	public InventoryMatrix createMatrix(IMatrixFactory<?> factory,
			FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		InventoryMatrix matrix = new InventoryMatrix();
		matrix.flowIndex = flowIndex;
		matrix.productIndex = techIndex;
		IMatrix enviMatrix = interventionMatrix.createRealMatrix(factory);
		matrix.interventionMatrix = enviMatrix;
		IMatrix techMatrix = technologyMatrix.createRealMatrix(factory);
		matrix.technologyMatrix = techMatrix;
		return matrix;
	}

	/**
	 * Re-evaluates the parameters and formulas in the inventory (because they
	 * may changed), generates new values for the entries that have an
	 * uncertainty distribution and set these values to the entries of the given
	 * matrix. The given matrix and this inventory have to match exactly in size
	 * (so normally you first call createMatrix and than simulate).
	 */
	public void simulate(InventoryMatrix matrix, FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		if (technologyMatrix != null)
			technologyMatrix.simulate(matrix.technologyMatrix);
		if (interventionMatrix != null)
			interventionMatrix.simulate(matrix.interventionMatrix);
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		if (technologyMatrix != null)
			technologyMatrix.eval(interpreter);
		if (interventionMatrix != null)
			interventionMatrix.eval(interpreter);
	}

}
