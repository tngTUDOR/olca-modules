package org.openlca.yaml;

import java.util.Map;

import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class UnitGroupReader {

	private Document doc;

	UnitGroupReader(Document doc) {
		this.doc = doc;
	}

	void read(Map<?, ?> root) {
		Map<?, ?> map = Util.map(root, "unitGroup");
		if (doc == null || map.isEmpty())
			return;
		UnitGroup group = new UnitGroup();
		Util.setBaseAttributes(group, map);
		for (Object elem : Util.list(map, "units")) {
			if (elem instanceof Map) {
				addUnit((Map<?, ?>) elem, group);
			}
		}
		refUnit(map, group);
		doc.unitGroups.add(group);
	}

	private void addUnit(Map<?, ?> m, UnitGroup group) {
		Unit unit = new Unit();
		Util.setBaseAttributes(unit, m);
		unit.setConversionFactor(Util.decimal(m, "factor", 1.0));
		group.getUnits().add(unit);
	}

	private void refUnit(Map<?, ?> m, UnitGroup group) {
		Map<?, ?> ref = Util.map(m, "refUnit");
		if (!ref.isEmpty()) {
			Unit refUnit = Util.get(group.getUnits(), Util.getId(ref, Unit.class));
			if (refUnit != null) {
				group.setReferenceUnit(refUnit);
				return;
			}
		}
		// no reference unit set -> select one
		Unit candidate = null;
		for (Unit unit : group.getUnits()) {
			if (candidate == null) {
				candidate = unit;
				continue;
			}
			if (unit.getConversionFactor() == 1d) {
				candidate = unit;
				break;
			}
		}
		group.setReferenceUnit(candidate);
	}
}
