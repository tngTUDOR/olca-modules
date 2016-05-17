package org.openlca.core.matrix;

import java.util.HashMap;
import java.util.Set;

/**
 * The TechGraph manages a TechIndex and the links between product inputs and
 * waste outputs to the corresponding flows in the TechIndex. It is used for
 * building product systems and mapping product systems to matrices.
 */
public class TechGraph {

	public final TechIndex index;

	/**
	 * Stores the links between the processes: for products it stores the link
	 * input -> output of the product and for waste flows it stores the link
	 * output -> input.
	 */
	private final HashMap<LongPair, LongPair> techLinks = new HashMap<>();

	/**
	 * Creates a new product index.
	 * 
	 * @param refFlow
	 *            the reference flow which is a product output are waste input.
	 */
	public TechGraph(LongPair refFlow) {
		this.index = new TechIndex(refFlow);
	}

	/**
	 * Adds a link between the given exchanges to this index. For products the
	 * output is added to the index if it is not yet contained. Product inputs
	 * are not part of the index. For waste flows it is the other way around:
	 * The waste input is part of the index and is added if it is not yet
	 * contained but the waste output is not part of the index.
	 */
	public void putLink(LongPair input, LongPair output) {
		index.put(output);
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

}
