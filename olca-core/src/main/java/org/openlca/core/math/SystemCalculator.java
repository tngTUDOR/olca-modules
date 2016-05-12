package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.Assessment;
import org.openlca.core.matrix.AssessmentMatrix;
import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final IMatrixSolver solver;

	public SystemCalculator(IDatabase db, IMatrixSolver solver) {
		this.db = db;
		this.solver = solver;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate product system - simple result");
		return calculator(setup).calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		return calculator(setup).calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		return calculator(setup).calculateFull();
	}

	private LcaCalculator calculator(CalculationSetup setup) {
		Inventory inventory = DataStructures.createInventory(setup, db);
		ParameterTable parameterTable = DataStructures.createParameterTable(db,
				setup, inventory);
		FormulaInterpreter interpreter = parameterTable.createInterpreter();
		InventoryMatrix inventoryMatrix = inventory.createMatrix(
				solver.getMatrixFactory(), interpreter);
		LcaCalculator calculator = new LcaCalculator(solver, inventoryMatrix);
		if (setup.impactMethod != null) {
			Assessment impactTable = Assessment.build(db,
					setup.impactMethod.getId(), inventory.flowIndex);
			AssessmentMatrix impactMatrix = impactTable.createMatrix(
					solver.getMatrixFactory(), interpreter);
			calculator.setImpactMatrix(impactMatrix);
		}
		if (setup.withCosts) {
			CostVector costVector = CostVector.build(inventory, db);
			if (!costVector.isEmpty())
				calculator.setCostVector(costVector);
		}
		return calculator;
	}
}
