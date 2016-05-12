package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.Assessment;
import org.openlca.core.matrix.AssessmentMatrix;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimulationResult;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A calculator for Monte-Carlo-Simulations.
 */
public class Simulator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase db;
	private ImpactMethodDescriptor impactMethod;
	private final IMatrixFactory<?> factory;
	private final IMatrixSolver matrixSolver;

	private SimulationResult result;
	private Inventory inventory;
	private ParameterTable parameterTable;
	private InventoryMatrix inventoryMatrix;
	private Assessment impactTable;
	private AssessmentMatrix impactMatrix;
	private CalculationSetup setup;

	public Simulator(CalculationSetup setup, IDatabase db, IMatrixSolver solver) {
		this.impactMethod = setup.impactMethod;
		this.db = db;
		this.setup = setup;
		this.factory = solver.getMatrixFactory();
		this.matrixSolver = solver;
	}

	public SimulationResult getResult() {
		return result;
	}

	/**
	 * Generates random numbers and calculates the product system. Returns true
	 * if the calculation was successfully done, otherwise false (this is the
	 * case when the resulting matrix is singular).
	 */
	public boolean nextRun() {
		if (inventory == null || inventoryMatrix == null)
			setUp();
		try {
			log.trace("next simulation run");
			FormulaInterpreter interpreter = parameterTable.simulate();
			inventory.simulate(inventoryMatrix, interpreter);
			LcaCalculator solver = new LcaCalculator(matrixSolver,
					inventoryMatrix);
			if (impactMatrix != null) {
				impactTable.simulate(impactMatrix, interpreter);
				solver.setImpactMatrix(impactMatrix);
			}
			SimpleResult result = solver.calculateSimple();
			appendResults(result);
			return true;
		} catch (Throwable e) {
			log.trace("simulation run failed", e);
			return false;
		}
	}

	private void appendResults(SimpleResult result) {
		this.result.appendFlowResults(result.totalFlowResults);
		if (this.result.hasImpactResults())
			this.result.appendImpactResults(result.totalImpactResults);
	}

	private void setUp() {
		log.trace("set up inventory");
		inventory = DataStructures.createInventory(setup, db);
		parameterTable = DataStructures.createParameterTable(db,
				setup, inventory);
		inventoryMatrix = inventory.createMatrix(factory);
		result = new SimulationResult();
		result.productIndex = inventory.techIndex;
		result.flowIndex = inventory.flowIndex;
		if (impactMethod != null) {
			Assessment impactTable = Assessment.build(db,
					impactMethod.getId(), inventory.flowIndex);
			if (impactTable.isEmpty()) {
				return;
			}
			this.impactTable = impactTable;
			this.impactMatrix = impactTable.createMatrix(factory);
			result.impactIndex = impactTable.categoryIndex;
		}
	}
}
