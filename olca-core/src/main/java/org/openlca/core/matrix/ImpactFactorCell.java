package org.openlca.core.matrix;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.dbtables.PicoUncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The cell value is negative if the factor is related to an input flow.
 */
class ImpactFactorCell {

	private final long methodId;
	private final boolean inputFlow;
	private final PicoImpactFactor factor;
	private NumberGenerator generator;

	ImpactFactorCell(PicoImpactFactor factor, long methodId, boolean inputFlow) {
		this.factor = factor;
		this.methodId = methodId;
		this.inputFlow = inputFlow;
	}

	void eval(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		try {
			Scope scope = interpreter.getScope(methodId);
			if (scope == null)
				scope = interpreter.getGlobalScope();
			tryEval(scope);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Formula evaluation failed, impact factor " + factor, e);
		}
	}

	private void tryEval(Scope scope) throws Exception {
		if (factor.getAmountFormula() != null) {
			double v = scope.eval(factor.getAmountFormula());
			factor.setAmount(v);
		}
		PicoUncertainty u = factor.uncertainty;
		if (u == null)
			return;
		if (u.parameter1Formula != null) {
			double v = scope.eval(u.parameter1Formula);
			u.parameter1 = v;
		}
		if (u.parameter2Formula != null) {
			double v = scope.eval(u.parameter2Formula);
			u.parameter2 = v;
		}
		if (u.parameter3Formula != null) {
			double v = scope.eval(u.parameter3Formula);
			u.parameter3 = v;
		}
	}

	double getMatrixValue() {
		if (factor == null)
			return 0;
		double amount = factor.getAmount() * factor.getConversionFactor();
		return inputFlow ? -amount : amount;
	}

	double getNextSimulationValue() {
		PicoUncertainty u = factor.uncertainty;
		if (u == null || u.type == null || u.type == UncertaintyType.NONE)
			return getMatrixValue();
		if (generator == null)
			generator = u.createGenerator();
		if (generator == null)
			return getMatrixValue();
		double amount = generator.next() * factor.getConversionFactor();
		return inputFlow ? -amount : amount;
	}
}
