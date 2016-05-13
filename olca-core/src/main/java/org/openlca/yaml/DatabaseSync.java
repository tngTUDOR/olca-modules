package org.openlca.yaml;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.UnitGroup;

class DatabaseSync {

	private IDatabase db;
	private Document doc;

	DatabaseSync(IDatabase db, Document doc) {
		this.db = db;
		this.doc = doc;
	}

	void run() {
		unitGroups();
		flowProperties();
		flows();
	}

	private void unitGroups() {
		UnitGroupDao dao = new UnitGroupDao(db);
		for (int i = 0; i < doc.unitGroups.size(); i++) {
			UnitGroup docGroup = doc.unitGroups.get(i);
			UnitGroup dbGroup = dao.getForRefId(docGroup.getRefId());
			if (dbGroup != null) {
				doc.unitGroups.set(i, dbGroup);
			} else {
				dao.insert(docGroup);
			}
		}
	}

	private void flowProperties() {
		FlowPropertyDao dao = new FlowPropertyDao(db);
		for (int i = 0; i < doc.flowProperties.size(); i++) {
			FlowProperty docProp = doc.flowProperties.get(i);
			FlowProperty dbProp = dao.getForRefId(docProp.getRefId());
			if (dbProp != null) {
				doc.flowProperties.set(i, dbProp);
				continue;
			}
			if (docProp.getUnitGroup() != null) {
				UnitGroup dbGroup = Util.get(doc.unitGroups, docProp.getUnitGroup());
				docProp.setUnitGroup(dbGroup);
			}
			dao.insert(docProp);
		}
	}

	private void flows() {
		FlowDao dao = new FlowDao(db);
		for (int i = 0; i < doc.flows.size(); i++) {
			Flow docFlow = doc.flows.get(i);
			Flow dbFlow = dao.getForRefId(docFlow.getRefId());
			if (dbFlow != null) {
				doc.flows.set(i, dbFlow);
				continue;
			}
			if (docFlow.getReferenceFlowProperty() != null) {
				FlowProperty refProp = Util.get(doc.flowProperties,
						docFlow.getReferenceFlowProperty());
				docFlow.setReferenceFlowProperty(refProp);
			}
			for (FlowPropertyFactor fac : docFlow.getFlowPropertyFactors()) {
				FlowProperty prop = Util.get(doc.flowProperties,
						fac.getFlowProperty());
				fac.setFlowProperty(prop);
			}
			dao.insert(docFlow);
		}
	}

}
