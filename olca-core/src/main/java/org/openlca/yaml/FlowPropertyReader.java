package org.openlca.yaml;

import java.util.Map;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;

class FlowPropertyReader {

	private Document doc;

	FlowPropertyReader(Document doc) {
		this.doc = doc;
	}

	void read(Map<?, ?> root) {
		Map<?, ?> map = Util.map(root, "quantity");
		if (doc == null || map.isEmpty())
			return;
		FlowProperty prop = new FlowProperty();
		Util.setBaseAttributes(prop, map);
		UnitGroup group = Ref.get(doc, map.get("unitGroup"), UnitGroup.class);
		prop.setUnitGroup(group);
		doc.flowProperties.add(prop);
	}

}