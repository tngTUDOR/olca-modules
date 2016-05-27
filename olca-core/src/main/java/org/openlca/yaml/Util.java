package org.openlca.yaml;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openlca.core.model.RootEntity;
import org.openlca.util.KeyGen;

class Util {

	static void setBaseAttributes(RootEntity entity, Map<?, ?> map) {
		if (entity == null || map == null)
			return;
		entity.setName(str(map, "name"));
		entity.setDescription(str(map, "description"));
		entity.setRefId(getId(map, entity.getClass()));
	}

	static String str(Map<?, ?> map, String key) {
		if (map == null || key == null)
			return null;
		Object val = map.get(key);
		return val == null ? null : val.toString();
	}

	static double decimal(Map<?, ?> map, String key, double defaultVal) {
		if (map == null || key == null)
			return defaultVal;
		Object val = map.get(key);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (!(val instanceof String))
			return defaultVal;
		try {
			return Double.parseDouble((String) val);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	static Map<?, ?> map(Map<?, ?> map, String key) {
		if (map == null || key == null)
			return Collections.emptyMap();
		Object val = map.get(key);
		if (!(val instanceof Map))
			return Collections.emptyMap();
		return (Map<?, ?>) val;
	}

	static Collection<?> list(Map<?, ?> map, String key) {
		if (map == null || key == null)
			return Collections.emptyList();
		Object val = map.get(key);
		if (!(val instanceof Collection))
			return Collections.emptyList();
		return (Collection<?>) val;
	}

	/**
	 * An entity should have an UUID or a unique name which is then used to
	 * generate an UUID.
	 */
	static String getId(Map<?, ?> map, Class<? extends RootEntity> type) {
		String refId = str(map, "uuid");
		if (refId != null)
			return refId;
		String name = str(map, "name");
		return KeyGen.get(type.getSimpleName() + "/" + name);
	}

	static <T extends RootEntity> T get(Collection<T> list, T entity) {
		if (list == null || entity == null)
			return null;
		else
			return get(list, entity.getRefId());
	}

	static <T extends RootEntity> T get(Collection<T> list, String id) {
		if (list == null || id == null)
			return null;
		for (T elem : list) {
			if (id.equalsIgnoreCase(elem.getRefId()))
				return elem;
		}
		return null;
	}
}
