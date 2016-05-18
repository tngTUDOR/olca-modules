package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechGraph;
import org.openlca.core.matrix.dbtables.ExchangeTable;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.matrix.dbtables.ProviderTable;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechGraphBuilder implements IProductIndexBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final ExchangeTable exchanges;
	private final ProviderTable providers;

	public TechGraphBuilder(IDatabase db, ProcessType preferredType) {
		this.exchanges = ExchangeTable.create(db);
		this.providers = ProviderTable.create(db, preferredType);
	}

	public TechGraphBuilder(ExchangeTable exchanges, ProviderTable providers) {
		this.exchanges = exchanges;
		this.providers = providers;
	}

	@Override
	public TechGraph build(LongPair refProduct) {
		return build(refProduct, 1.0);
	}

	@Override
	public TechGraph build(LongPair refProduct, double demand) {
		log.trace("build product index for {}", refProduct);
		TechGraph graph = new TechGraph(refProduct);
		graph.index.demand = demand;
		List<LongPair> block = new ArrayList<>();
		block.add(refProduct);
		HashSet<LongPair> handled = new HashSet<>();
		while (!block.isEmpty()) {
			List<LongPair> nextBlock = new ArrayList<>();
			log.trace("fetch next block with {} entries", block.size());
			Map<Long, List<PicoExchange>> allFlows = fetchExchanges(block);
			for (LongPair indexFlow : block) {
				handled.add(indexFlow);
				List<PicoExchange> flows = allFlows.get(indexFlow.getFirst());
				List<PicoExchange> linkFlows = getLinkFlows(flows);
				for (PicoExchange link : linkFlows) {
					LongPair provider = providers.get(link);
					if (provider == null)
						continue;
					LongPair l = LongPair.of(link.processID, link.exchangeID);
					graph.putLink(l, provider);
					if (!handled.contains(provider) && !nextBlock.contains(provider))
						nextBlock.add(provider);
				}
			}
			block = nextBlock;
		}
		return graph;
	}

	/**
	 * Returns product inputs or waste outputs from the given list that could be
	 * linked to product outputs or waste inputs.
	 */
	private List<PicoExchange> getLinkFlows(List<PicoExchange> flows) {
		if (flows == null || flows.isEmpty())
			return Collections.emptyList();
		List<PicoExchange> list = new ArrayList<>();
		for (PicoExchange e : flows) {
			if (e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
				list.add(e);
			if (!e.isInput && e.flowType == FlowType.WASTE_FLOW)
				list.add(e);
		}
		return list;
	}

	private Map<Long, List<PicoExchange>> fetchExchanges(List<LongPair> block) {
		if (block.isEmpty())
			return Collections.emptyMap();
		Set<Long> processIds = new HashSet<>();
		for (LongPair pair : block) {
			processIds.add(pair.getFirst());
		}
		return exchanges.get(processIds);
	}
}
