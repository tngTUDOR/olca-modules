package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The `TechIndex` maps product outputs and waste inputs of a product system to
 * an ordinal index which maps these flows to the respective rows and columns of
 * the technology matrix. Each product output and waste input is described by a
 * pair of process ID and exchange ID. The index also stores the demand value of
 * the product system.
 */
public class TechIndex {

	/**
	 * The demand value, this is the amount of the reference flow given in the
	 * reference unit and flow property of this flow. The default value is 1.0.
	 */
	public double demand = 1;

	/** Maps the product outputs and waste inputs to an ordinal index. */
	private final HashMap<LongPair, Integer> index = new HashMap<>();

	/** The ordered product outputs and waste inputs in the index. */
	private final ArrayList<LongPair> flows = new ArrayList<>();

	/**
	 * Maps a process ID to the flows in this index that belong to this process.
	 */
	private final HashMap<Long, List<LongPair>> processFlows = new HashMap<>();

	public TechIndex(LongPair refFlow) {
		put(refFlow);
	}

	/**
	 * Returns the ordinal index of the given flow.
	 */
	public int getIndex(LongPair flow) {
		Integer idx = index.get(flow);
		if (idx == null)
			return -1;
		return idx;
	}

	/**
	 * Returns true if the given flow is contained in this index.
	 */
	public boolean contains(LongPair flow) {
		return index.containsKey(flow);
	}

	/**
	 * Adds the given flow to this index. Does nothing if the given flow is
	 * already contained in this index.
	 */
	public void put(LongPair flow) {
		if (contains(flow))
			return;
		int idx = index.size();
		index.put(flow, idx);
		flows.add(flow);
		List<LongPair> list = processFlows.get(flow.getFirst());
		if (list == null) {
			list = new ArrayList<>();
			processFlows.put(flow.getFirst(), list);
		}
		list.add(flow);
	}

	/**
	 * Returns the flow at the given index.
	 */
	public LongPair getFlowAt(int index) {
		return flows.get(index);
	}

	/**
	 * Returns the number of flows in the index. This is equal to the number of
	 * rows and columns in a technology matrix.
	 */
	public int size() {
		return index.size();
	}

	/**
	 * Returns the index flows for the process with the given ID.
	 */
	public List<LongPair> getProcessFlows(long processId) {
		List<LongPair> products = processFlows.get(processId);
		if (products == null)
			return Collections.emptyList();
		return products;
	}

	public Set<Long> getProcessIds() {
		HashSet<Long> set = new HashSet<>();
		for (LongPair product : flows)
			set.add(product.getFirst());
		return set;
	}
}
