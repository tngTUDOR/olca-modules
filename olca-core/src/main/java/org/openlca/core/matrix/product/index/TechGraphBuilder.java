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
			Map<Long, List<PicoExchange>> exchanges = fetchExchanges(block);
			for (LongPair recipient : block) {
				handled.add(recipient);
				List<PicoExchange> processExchanges = exchanges.get(recipient
						.getFirst());
				List<PicoExchange> productInputs = getProductInputs(
						processExchanges);
				for (PicoExchange productInput : productInputs) {
					LongPair provider = providers.get(productInput);
					if (provider == null)
						continue;
					LongPair recipientInput = new LongPair(
							recipient.getFirst(), productInput.exchangeID);
					graph.putLink(recipientInput, provider);
					if (!handled.contains(provider)
							&& !nextBlock.contains(provider))
						nextBlock.add(provider);
				}
			}
			block = nextBlock;
		}
		return graph;
	}

	private List<PicoExchange> getProductInputs(
			List<PicoExchange> processExchanges) {
		if (processExchanges == null || processExchanges.isEmpty())
			return Collections.emptyList();
		List<PicoExchange> productInputs = new ArrayList<>();
		for (PicoExchange exchange : processExchanges) {
			if (!exchange.isInput)
				continue;
			if (exchange.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			productInputs.add(exchange);
		}
		return productInputs;
	}

	private Map<Long, List<PicoExchange>> fetchExchanges(List<LongPair> block) {
		if (block.isEmpty())
			return Collections.emptyMap();
		Set<Long> processIds = new HashSet<>();
		for (LongPair pair : block)
			processIds.add(pair.getFirst());
		try {
			return exchanges.get(processIds);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

}
