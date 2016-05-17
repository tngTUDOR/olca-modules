package org.openlca.core.math;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.TechGraph;
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
	public static TechGraph createProductIndex(ProductSystem system) {
		Process refProcess = system.getReferenceProcess();
		Exchange refExchange = system.getReferenceExchange();
		Flow refFlow = refExchange.getFlow();
		LongPair refProduct = new LongPair(refProcess.getId(), refFlow.getId());
		double demand = ReferenceAmount.get(system);
		TechGraph graph = new TechGraph(refProduct);
		graph.index.demand = demand;
		for (ProcessLink link : system.getProcessLinks()) {
			long flow = link.getFlowId();
			long provider = link.getProviderId();
			long recipient = link.getRecipientId();
			LongPair processProduct = new LongPair(provider, flow);
			graph.index.put(processProduct);
			LongPair input = new LongPair(recipient, flow);
			graph.putLink(input, processProduct);
		}
		return graph;
	}

	public static Inventory createInventory(ProductSystem system, IDatabase db) {
		TechGraph index = createProductIndex(system);
		AllocationMethod method = AllocationMethod.USE_DEFAULT;
		return Inventory.build(index, db, method);
	}

	public static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, IDatabase db) {
		TechGraph index = createProductIndex(system);
		return Inventory.build(index, db, allocationMethod);
	}

	public static Inventory createInventory(CalculationSetup setup, IDatabase db) {
		ProductSystem system = setup.productSystem;
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			method = AllocationMethod.NONE;
		TechGraph graph = createProductIndex(system);
		graph.index.demand = ReferenceAmount.get(setup);
		return Inventory.build(graph, db, method);
	}

	public static ParameterTable createParameterTable(IDatabase db,
			CalculationSetup setup, Inventory inventory) {
		Set<Long> contexts = new HashSet<>();
		if (setup.impactMethod != null)
			contexts.add(setup.impactMethod.getId());
		if (inventory.techGraph != null)
			contexts.addAll(inventory.techGraph.index.getProcessIds());
		ParameterTable table = ParameterTable.build(db, contexts);
		table.apply(setup.parameterRedefs);
		return table;
	}

}
