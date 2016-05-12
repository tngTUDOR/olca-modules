package org.openlca.core.matrix.dbtables;

import java.util.List;

class Util {

	private Util() {
	}

	static String toSql(List<Long> list) {
		StringBuilder s = new StringBuilder();
		s.append('(');
		for (int i = 0; i < list.size(); i++) {
			s.append(list.get(i).toString());
			if (i < (list.size() - 1)) {
				s.append(',');
			}
		}
		s.append(')');
		return s.toString();
	}
}
