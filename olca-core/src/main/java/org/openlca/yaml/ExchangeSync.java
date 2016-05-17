package org.openlca.yaml;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Strings;

class ExchangeSync {

	private Process process;
	private Document doc;

	private ExchangeSync(Process process, Document doc) {
		this.process = process;
		this.doc = doc;
	}

	static void on(Process process, Document doc) {
		new ExchangeSync(process, doc).run();
	}

	private void run() {
		for (Exchange e : process.getExchanges()) {
			Flow flow = Util.get(doc.flows, e.getFlow());
			e.setFlow(flow);
			FlowPropertyFactor f = getFactor(e, flow);
			e.setFlowPropertyFactor(f);
			Unit unit = getUnit(e, f);
			e.setUnit(unit);
		}
	}

	private FlowPropertyFactor getFactor(Exchange e, Flow fromFlow) {
		if (e == null || fromFlow == null)
			return null;
		FlowPropertyFactor ef = e.getFlowPropertyFactor();
		if (ef == null || ef.getFlowProperty() == null)
			return null;
		String propId = ef.getFlowProperty().getRefId();
		for (FlowPropertyFactor flowFac : fromFlow.getFlowPropertyFactors()) {
			FlowProperty prop = flowFac.getFlowProperty();
			if (prop == null || prop.getRefId() == null)
				continue;
			if (Strings.nullOrEqual(propId, prop.getRefId()))
				return flowFac;
		}
		return null;
	}

	private Unit getUnit(Exchange e, FlowPropertyFactor f) {
		if (e == null || e.getUnit() == null || f == null
				|| f.getFlowProperty() == null)
			return null;
		UnitGroup group = f.getFlowProperty().getUnitGroup();
		if (group == null)
			return null;
		String unitId = e.getUnit().getRefId();
		for (Unit unit : group.getUnits()) {
			if (Strings.nullOrEqual(unitId, unit.getRefId()))
				return unit;
		}
		return null;
	}
}
