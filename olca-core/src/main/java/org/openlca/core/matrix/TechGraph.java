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
	 * Adds a link between the given link flow and index flow.
	 */
	public void putLink(LongPair linkFlow, LongPair indexFlow) {
		index.put(indexFlow);
		techLinks.put(linkFlow, indexFlow);
	}

	/**
	 * Returns true if the given flow is a product input or a waste output that
	 * is linked to a flow of this index.
	 */
	public boolean isLinked(LongPair linkFlow) {
		return techLinks.containsKey(linkFlow);
	}

	/**
	 * Returns the index flow (product output or waste input) for the given link
	 * flow (product input or waste output).
	 */
	public LongPair getIndexFlow(LongPair linkFlow) {
		return techLinks.get(linkFlow);
	}

	/**
	 * Returns all input products or waste outputs that are linked.
	 */
	public Set<LongPair> getLinkFlows() {
		return techLinks.keySet();
	}

}
