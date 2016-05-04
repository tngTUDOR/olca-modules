package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.dbtables.ExchangeTable;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.matrix.dbtables.ProviderTable;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechIndexCutoffBuilder implements IProductIndexBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ExchangeTable exchanges;
	private ProviderTable providers;
	private double cutoff;

	public TechIndexCutoffBuilder(ExchangeTable exchanges, ProviderTable providers, double cutoff) {
		this.exchanges = exchanges;
		this.providers = providers;
		this.cutoff = cutoff;
	}

	@Override
	public TechIndex build(LongPair refProduct) {
		return build(refProduct, 1.0);
	}

	@Override
	public TechIndex build(LongPair refProduct, double demand) {
		log.trace("build product index for {} with cutoff=", refProduct,
				cutoff);
		TechIndex index = new TechIndex(refProduct);
		index.setDemand(demand);
		Graph g = new Graph(refProduct, demand);
		while (!g.next.isEmpty())
			g.handleNext();
		fillIndex(g, index);
		log.trace("created the index with {} products", index.size());
		return index;
	}

	private void fillIndex(Graph g, TechIndex index) {
		for (Node node : g.nodes.values()) {
			if (node.state != NodeState.FOLLOWED)
				continue;
			for (Link link : node.inputLinks) {
				if (link.demand < cutoff)
					continue;
				Node provider = link.provider;
				LongPair input = LongPair.of(node.product.getFirst(),
						provider.product.getSecond());
				index.putLink(input, provider.product);
			}
		}
	}

	private class Graph {

		Node root;

		List<Node> next = new ArrayList<>();
		HashMap<LongPair, Node> nodes = new HashMap<>();

		Graph(LongPair refProduct, double demand) {
			this.root = new Node(refProduct, demand);
			root.product = refProduct;
			root.state = NodeState.WAITING;
			nodes.put(refProduct, root);
			next.add(root);
		}

		void handleNext() {

			log.trace("handle next layer with {} product nodes", next.size());

			// to minimize the re-scale calls we first sort the nodes by their
			// demands (see the compareTo method in Node)
			Collections.sort(next);

			Map<Long, List<PicoExchange>> nextExchanges = fetchNextExchanges();
			List<Node> nextLayer = new ArrayList<>();
			for (Node node : next) {
				node.state = NodeState.PROGRESS;
				List<PicoExchange> exchanges = nextExchanges.get(
						node.product.getFirst());
				PicoExchange output = getOutput(node, exchanges);
				if (output == null)
					continue;
				node.outputAmount = output.amount;
				node.scalingFactor = node.demand / node.outputAmount;
				followInputs(node, exchanges, nextLayer);
				node.state = NodeState.FOLLOWED;
			}
			next.clear();
			next.addAll(nextLayer);
		}

		private void followInputs(Node node, List<PicoExchange> exchanges,
				List<Node> nextLayer) {
			for (PicoExchange input : getInputs(node, exchanges)) {
				LongPair inputProduct = providers.get(input);
				if (inputProduct == null)
					continue;
				double inputAmount = input.amount;
				double inputDemand = node.scalingFactor * inputAmount;
				Node providerNode = nodes.get(inputProduct);
				if (providerNode != null)
					checkSubGraph(inputDemand, providerNode, nextLayer, false);
				else {
					providerNode = createNode(inputDemand, inputProduct,
							nextLayer);
				}
				node.addLink(providerNode, inputAmount, inputDemand);
			}
		}

		private Node createNode(double inputDemand, LongPair product,
				List<Node> nextLayer) {
			Node node = new Node(product, inputDemand);
			nodes.put(product, node);
			if (inputDemand < cutoff)
				node.state = NodeState.EXCLUDED;
			else {
				node.state = NodeState.WAITING;
				nextLayer.add(node);
			}
			return node;
		}

		private void checkSubGraph(double demand, Node provider,
				List<Node> nextLayer, boolean recursion) {
			if (demand <= provider.demand || demand < cutoff)
				return;
			provider.demand = demand;
			if (provider.state == NodeState.EXCLUDED) {
				provider.state = NodeState.WAITING;
				nextLayer.add(provider);
			}
			if (provider.state == NodeState.FOLLOWED) {
				provider.state = NodeState.RESCALED;
				rescaleSubGraph(provider, nextLayer);
				if (!recursion) {
					log.trace("rescaled a sub graph");
					unsetScaleState();
				}
			}
		}

		private void rescaleSubGraph(Node start, List<Node> nextLayer) {
			start.scalingFactor = start.demand / start.outputAmount;
			for (Link link : start.inputLinks) {
				double inputDemand = link.inputAmount * start.scalingFactor;
				link.demand = inputDemand;
				Node provider = link.provider;
				checkSubGraph(inputDemand, provider, nextLayer, true);
			}
		}

		private void unsetScaleState() {
			for (Node node : nodes.values()) {
				if (node.state == NodeState.RESCALED) {
					node.state = NodeState.FOLLOWED;
				}
			}
		}

		private PicoExchange getOutput(Node node, List<PicoExchange> all) {
			for (PicoExchange e : all) {
				if (e.isInput || e.flowType != FlowType.PRODUCT_FLOW
						|| e.flowID != node.product.getSecond())
					continue;
				return e;
			}
			return null;
		}

		private List<PicoExchange> getInputs(Node node, List<PicoExchange> all) {
			List<PicoExchange> inputs = new ArrayList<>();
			for (PicoExchange e : all) {
				if (e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
					inputs.add(e);
			}
			return inputs;
		}

		private Map<Long, List<PicoExchange>> fetchNextExchanges() {
			if (next.isEmpty())
				return Collections.emptyMap();
			Set<Long> processIds = new HashSet<>();
			for (Node node : next)
				processIds.add(node.product.getFirst());
			try {
				return exchanges.get(processIds);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to load exchanges from cache", e);
				return Collections.emptyMap();
			}
		}
	}
}
