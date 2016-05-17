package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The TechIndex is used to map product outputs and waste inputs to rows and
 * columns in the matrices. A product or waste flow is represented by a
 * {@link LongPair}, where the first entry is the ID of the process and the
 * second ID the ID of the respective exchange.
 */
public class TechGraph {

	/** Maps the product outputs and waste inputs to an ordinal index. */
	private final HashMap<LongPair, Integer> index = new HashMap<>();

	/** The ordered product outputs and waste inputs in the index. */
	private final ArrayList<LongPair> indexFlows = new ArrayList<>();

	/**
	 * Stores the links between the processes: for products it stores the link
	 * input -> output of the product and for waste flows it stores the link
	 * output -> input.
	 */
	private final HashMap<LongPair, LongPair> techLinks = new HashMap<>();

	/**
	 * Maps a process ID to the flows in this index that belong to this process.
	 */
	private final HashMap<Long, List<LongPair>> processFlows = new HashMap<>();

	private LongPair refFlow;
	private double demand = 1d;

	/**
	 * Creates a new product index.
	 * 
	 * @param refFlow
	 *            the reference flow which is a product output are waste input.
	 */
	public TechGraph(LongPair refFlow) {
		this.refFlow = refFlow;
		put(refFlow);
	}

	public LongPair getRefFlow() {
		return refFlow;
	}

	/**
	 * The demand value, this is the amount of the reference flow given in the
	 * reference unit and flow property of this flow. The default value is 1.0.
	 */
	public void setDemand(double demand) {
		this.demand = demand;
	}

	/**
	 * The demand value, this is the amount of the reference flow given in the
	 * reference unit and flow property of this flow. The default value is 1.0.
	 */
	public double getDemand() {
		return demand;
	}

	/**
	 * Returns the number of flows in the index. This is equal to the number of
	 * rows and columns in a technology matrix.
	 */
	public int size() {
		return index.size();
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
		indexFlows.add(flow);
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
		return indexFlows.get(index);
	}

	/**
	 * Returns the index flows for the process with the given ID.
	 */
	public List<LongPair> getIndexFlows(long processId) {
		List<LongPair> products = processFlows.get(processId);
		if (products == null)
			return Collections.emptyList();
		return new ArrayList<>(products);
	}

	/**
	 * Adds a link between the given exchanges to this index. For products the
	 * output is added to the index if it is not yet contained. Product inputs
	 * are not part of the index. For waste flows it is the other way around:
	 * The waste input is part of the index and is added if it is not yet
	 * contained but the waste output is not part of the index.
	 */
	public void putLink(LongPair input, LongPair output) {
		put(output);
		techLinks.put(input, output);
		// TODO: check waste links -> use PicoExchange as type
	}

	/**
	 * Returns true if the given flow is a product input or a waste output that
	 * is linked to a flow of this index.
	 */
	public boolean isLinked(LongPair flow) {
		return techLinks.containsKey(flow);
	}

	/**
	 * Returns the output product or waste input to which the given flow is
	 * linked, or null if it is not linked.
	 */
	public LongPair getLinkedTarget(LongPair flow) {
		return techLinks.get(flow);
	}

	/**
	 * Returns all input products or waste outputs that are linked.
	 */
	public Set<LongPair> getLinkedFlows() {
		return techLinks.keySet();
	}

	public Set<Long> getProcessIds() {
		HashSet<Long> set = new HashSet<>();
		for (LongPair product : indexFlows)
			set.add(product.getFirst());
		return set;
	}

}
