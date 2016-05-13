package org.openlca.yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.yaml.snakeyaml.Yaml;

public class Document {

	public final List<UnitGroup> unitGroups = new ArrayList<>();
	public final List<FlowProperty> flowProperties = new ArrayList<>();
	public final List<Flow> flows = new ArrayList<>();
	public final List<Process> processes = new ArrayList<>();

	public static Document read(InputStream is) {
		return parse(new Yaml().load(is));
	}

	public static Document read(Reader reader) {
		return parse(new Yaml().load(reader));
	}

	public static Document read(String text) {
		return parse(new Yaml().load(text));
	}

	private static Document parse(Object obj) {
		Document doc = new Document();
		if (!(obj instanceof Collection))
			return doc;
		Collection<?> collection = (Collection<?>) obj;
		List<Map<?, ?>> maps = new ArrayList<>();
		for (Object elem : collection) {
			if (elem instanceof Map) {
				maps.add((Map<?, ?>) elem);
			}
		}
		Collections.sort(maps, (m1, m2) -> {
			ModelType t1 = getType(m1);
			ModelType t2 = getType(m2);
			return getOrder(t1) - getOrder(t2);
		});
		for (Map<?, ?> map : maps) {
			convert(map, getType(map), doc);
		}
		return doc;
	}

	private static ModelType getType(Map<?, ?> map) {
		if (map == null)
			return null;
		if (map.containsKey("unitGroup"))
			return ModelType.UNIT_GROUP;
		if (map.containsKey("quantity"))
			return ModelType.FLOW_PROPERTY;
		if (map.containsKey("flow"))
			return ModelType.FLOW;
		if (map.containsKey("process"))
			return ModelType.PROCESS;
		if (map.containsKey("system"))
			return ModelType.PRODUCT_SYSTEM;
		return null;
	}

	private static int getOrder(ModelType type) {
		if (type == null)
			return 100;
		switch (type) {
		case CATEGORY:
			return 0;
		case ACTOR:
			return 1;
		case SOURCE:
			return 2;
		case LOCATION:
			return 3;
		case UNIT_GROUP:
			return 4;
		case FLOW_PROPERTY:
			return 5;
		case FLOW:
			return 6;
		case PROCESS:
			return 7;
		case PRODUCT_SYSTEM:
			return 8;
		default:
			return 100;
		}
	}

	private static void convert(Map<?, ?> map, ModelType type, Document doc) {
		if (map == null || type == null || doc == null)
			return;
		switch (type) {
		case UNIT_GROUP:
			new UnitGroupReader(doc).read(map);
			break;
		case FLOW_PROPERTY:
			new FlowPropertyReader(doc).read(map);
			break;
		case FLOW:
			new FlowReader(doc).read(map);
			break;
		case PROCESS:
			new ProcessReader(doc).read(map);
			break;
		default:
			System.err.println("No converter for model type " + type);
			break;
		}
	}

	/**
	 * Synchronizes the document with the given database.
	 */
	public void sync(IDatabase db) {
		new DatabaseSync(db, this).run();
	}

}
