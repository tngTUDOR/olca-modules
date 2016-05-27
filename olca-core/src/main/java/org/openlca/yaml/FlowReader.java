package org.openlca.yaml;

import java.util.Collection;
import java.util.Map;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;

class FlowReader {

	private Document doc;

	FlowReader(Document doc) {
		this.doc = doc;
	}

	void read(Map<?, ?> root) {
		Map<?, ?> map = Util.map(root, "flow");
		if (doc == null || map.isEmpty())
			return;
		Flow flow = new Flow();
		Util.setBaseAttributes(flow, map);
		flow.setFlowType(getType(map));
		FlowProperty prop = Ref.get(doc, map.get("refQuantity"), FlowProperty.class);
		flow.setReferenceFlowProperty(prop);
		Collection<?> quantities = Util.list(map, "quanties");
		if (!quantities.isEmpty()) {
			addQuantities(quantities, flow);
		} else {
			FlowPropertyFactor factor = new FlowPropertyFactor();
			factor.setConversionFactor(1.0);
			factor.setFlowProperty(flow.getReferenceFlowProperty());
			flow.getFlowPropertyFactors().add(factor);
		}
		doc.flows.add(flow);
	}

	private FlowType getType(Map<?, ?> map) {
		String type = Util.str(map, "type");
		if (type == null)
			return FlowType.ELEMENTARY_FLOW;
		switch (type) {
		case "product":
			return FlowType.PRODUCT_FLOW;
		case "waste":
			return FlowType.WASTE_FLOW;
		case "elementary":
			return FlowType.ELEMENTARY_FLOW;
		default:
			return FlowType.ELEMENTARY_FLOW;
		}
	}

	private void addQuantities(Collection<?> list, Flow flow) {
		// TODO: support multiple quantities for flows.
	}

}