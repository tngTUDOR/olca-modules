package org.openlca.core.math;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

/**
 * Provides helper methods for creating matrix-like data structures that can be
 * used in calculations (but also exports, validations, etc.).
 */
public class DataStructures {

	private DataStructures() {
	}

	/**
	 * Creates a product index from the given product system.
	 */
	public static TechIndex createProductIndex(ProductSystem system) {
		Process refProcess = system.getReferenceProcess();
		Exchange refExchange = system.getReferenceExchange();
		Flow refFlow = refExchange.getFlow();
		LongPair refProduct = new LongPair(refProcess.getId(), refFlow.getId());
		double demand = ReferenceAmount.get(system);
		TechIndex index = new TechIndex(refProduct);
		index.setDemand(demand);
		for (ProcessLink link : system.getProcessLinks()) {
			long flow = link.getFlowId();
			long provider = link.getProviderId();
			long recipient = link.getRecipientId();
			LongPair processProduct = new LongPair(provider, flow);
			index.put(processProduct);
			LongPair input = new LongPair(recipient, flow);
			index.putLink(input, processProduct);
		}
		return index;
	}

	public static Inventory createInventory(ProductSystem system, IDatabase db) {
		TechIndex index = createProductIndex(system);
		AllocationMethod method = AllocationMethod.USE_DEFAULT;
		return Inventory.build(index, db, method);
	}

	public static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, IDatabase db) {
		TechIndex index = createProductIndex(system);
		return Inventory.build(index, db, allocationMethod);
	}

	public static Inventory createInventory(CalculationSetup setup, IDatabase db) {
		ProductSystem system = setup.productSystem;
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			method = AllocationMethod.NONE;
		TechIndex index = createProductIndex(system);
		index.setDemand(ReferenceAmount.get(setup));
		return Inventory.build(index, db, method);
	}

	public static ParameterTable createParameterTable(IDatabase db,
			CalculationSetup setup, Inventory inventory) {
		Set<Long> contexts = new HashSet<>();
		if (setup.impactMethod != null)
			contexts.add(setup.impactMethod.getId());
		if (inventory.techIndex != null)
			contexts.addAll(inventory.techIndex.getProcessIds());
		ParameterTable table = ParameterTable.build(db, contexts);
		table.apply(setup.parameterRedefs);
		return table;
	}

}
