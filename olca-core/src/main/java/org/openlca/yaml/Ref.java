package org.openlca.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Strings;

class Ref {

	/** Get the referenced object from the document. */
	public static <T extends RootEntity> T get(Document doc, Object ref,
			Class<T> type) {
		if (doc == null || ref == null || type == null)
			return null;
		List<T> all = list(doc, type);
		if (all.isEmpty())
			return null;
		if (ref instanceof String)
			return byName(all, (String) ref);
		if (ref instanceof Map)
			return byRef(all, (Map<?, ?>) ref, type);
		return null;
	}

	private static <T extends RootEntity> T byRef(List<T> all, Map<?, ?> ref,
			Class<T> type) {
		String id = Util.getId(ref, type);
		return Util.get(all, id);
	}

	private static <T extends RootEntity> T byName(List<T> all, String name) {
		for (T e : all) {
			if (Strings.nullOrEqual(name, e.getName()))
				return e;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T extends RootEntity> List<T> list(Document doc, Class<T> type) {
		if (doc == null || type == null)
			return Collections.emptyList();
		if (type.equals(Unit.class))
			return (List<T>) units(doc);
		if (type.equals(UnitGroup.class))
			return (List<T>) doc.unitGroups;
		if (type.equals(FlowProperty.class))
			return (List<T>) doc.flowProperties;
		if (type.equals(Flow.class))
			return (List<T>) doc.flows;
		if (type.equals(Process.class))
			return (List<T>) doc.processes;
		return Collections.emptyList();
	}

	private static List<Unit> units(Document doc) {
		if (doc == null)
			return Collections.emptyList();
		List<Unit> units = new ArrayList<>();
		for (UnitGroup g : doc.unitGroups) {
			units.addAll(g.getUnits());
		}
		return units;
	}
}
