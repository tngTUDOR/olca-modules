package org.openlca.core.matrix;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.matrix.dbtables.PicoUncertainty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeCell {

	final PicoExchange exchange;
	public double allocationFactor = 1d;
	private NumberGenerator generator;

	ExchangeCell(PicoExchange exchange) {
		this.exchange = exchange;
	}

	void eval(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		try {
			Scope scope = interpreter.getScope(exchange.processID);
			if (scope == null)
				scope = interpreter.getGlobalScope();
			tryEval(scope);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Formula evaluation failed, exchange "
					+ exchange.exchangeID, e);
		}
	}

	private void tryEval(Scope scope) throws InterpreterException {
		if (exchange.amountFormula != null) {
			double v = scope.eval(exchange.amountFormula);
			exchange.amount = v * exchange.conversionFactor;
		}
		if (exchange.costFormula != null) {
			double v = scope.eval(exchange.costFormula);
			exchange.costValue = v;
		}
		PicoUncertainty u = exchange.uncertainty;
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
		if (exchange == null)
			return 0;
		double amount = exchange.amount * allocationFactor;
		if (exchange.isInput && !exchange.isAvoidedProduct)
			return -amount;
		else
			return amount;
	}

	double getCostValue() {
		if (exchange == null)
			return 0;
		double val = exchange.costValue * allocationFactor;
		if (exchange.flowType == FlowType.PRODUCT_FLOW && !exchange.isInput)
			return -val;
		else
			return val;
	}

	double getNextSimulationValue() {
		PicoUncertainty u = exchange.uncertainty;
		if (u == null || u.type == null || u.type == UncertaintyType.NONE)
			return getMatrixValue();
		if (generator == null)
			generator = createGenerator(u);
		double amount = generator.next() * allocationFactor
				* exchange.conversionFactor;
		if (exchange.isInput && !exchange.isAvoidedProduct)
			return -amount;
		else
			return amount;
	}

	private NumberGenerator createGenerator(PicoUncertainty u) {
		final PicoExchange e = exchange;
		switch (u.type) {
		case LOG_NORMAL:
			return NumberGenerator.logNormal(u.parameter1, u.parameter2);
		case NORMAL:
			return NumberGenerator.normal(u.parameter1, u.parameter2);
		case TRIANGLE:
			return NumberGenerator.triangular(u.parameter1,
					u.parameter2, u.parameter3);
		case UNIFORM:
			return NumberGenerator.uniform(u.parameter1, u.parameter2);
		default:
			return NumberGenerator.discrete(e.amount);
		}
	}
}
