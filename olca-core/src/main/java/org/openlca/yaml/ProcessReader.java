package org.openlca.yaml;

import java.util.Collection;
import java.util.Map;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessReader {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Document doc;

	ProcessReader(Document doc) {
		this.doc = doc;
	}

	void read(Map<?, ?> root) {
		Map<?, ?> map = Util.map(root, "process");
		if (doc == null || map.isEmpty())
			return;
		Process p = new Process();
		Util.setBaseAttributes(p, map);
		p.setProcessType(getType(map));
		addExchanges(p, Util.list(map, "inputs"), true);
		addExchanges(p, Util.list(map, "outputs"), false);
		doc.processes.add(p);
	}

	private ProcessType getType(Map<?, ?> map) {
		String type = Util.str(map, "type");
		if (type == null)
			return ProcessType.UNIT_PROCESS;
		if (type.equalsIgnoreCase("lci"))
			return ProcessType.LCI_RESULT;
		else
			return ProcessType.UNIT_PROCESS;
	}

	private void addExchanges(Process p, Collection<?> maps, boolean inputs) {
		if (maps == null)
			return;
		for (Object obj : maps) {
			if (!(obj instanceof Map))
				continue;
			Map<?, ?> map = (Map<?, ?>) obj;
			Exchange e = new Exchange();
			e.setInput(inputs);
			e.setAmountValue(Util.decimal(map, "amount", 0));
			Flow flow = Ref.get(doc, map.get("flow"), Flow.class);
			if (flow == null) {
				log.error("Invalid exchange {} -> flow not found", map);
				continue;
			}
			e.setFlow(flow);
			Unit unit = Ref.get(doc, map.get("unit"), Unit.class);
			Quan quan = Quan.find(flow, unit);
			if (quan == null) {
				log.error("Invalid exchange {} -> no quantity/unit", map);
				continue;
			}
			e.setFlowPropertyFactor(quan.factor);
			e.setUnit(quan.unit);
			p.getExchanges().add(e);
		}
	}

	private static class Quan {

		FlowPropertyFactor factor;
		Unit unit;

		Quan(FlowPropertyFactor factor, Unit unit) {
			this.factor = factor;
			this.unit = unit;
		}

		static Quan find(Flow flow, Unit unit) {
			if (flow == null || unit == null)
				return null;
			Quan q = find(flow.getReferenceFactor(), unit);
			if (q != null)
				return q;
			for (FlowPropertyFactor f : flow.getFlowPropertyFactors()) {
				q = find(f, unit);
				if (q != null)
					return q;
			}
			return null;
		}

		private static Quan find(FlowPropertyFactor f, Unit unit) {
			if (f == null || f.getFlowProperty() == null)
				return null;
			UnitGroup ug = f.getFlowProperty().getUnitGroup();
			if (ug == null)
				return null;
			for (Unit u : ug.getUnits()) {
				if (Strings.nullOrEqual(u.getRefId(), unit.getRefId()))
					return new Quan(f, u);
			}
			return null;
		}

	}

}
