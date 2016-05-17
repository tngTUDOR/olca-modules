package org.openlca.core.matrix;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.dbtables.ExchangeTable;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a flow index from a product index and exchange table. All flows that
 * are not contained in the product index will be added to the flow index
 * (except if they are allocated co-products).
 */
class FlowIndexBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final ExchangeTable exchanges;
	private final TechGraph productIndex;
	private final AllocationMethod allocationMethod;

	FlowIndexBuilder(ExchangeTable exchanges, TechGraph productIndex,
			AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
		this.exchanges = exchanges;
		this.productIndex = productIndex;
	}

	FlowIndex build() {
		FlowIndex index = new FlowIndex();
		Map<Long, List<PicoExchange>> map = loadExchanges();
		for (Long processId : productIndex.getProcessIds()) {
			List<PicoExchange> exchanges = map.get(processId);
			for (PicoExchange e : exchanges) {
				if (index.contains(e.flowID))
					continue; // already indexed as flow
				LongPair productCandidate = new LongPair(e.processID, e.flowID);
				if (productIndex.contains(productCandidate))
					continue; // the exchange is an output product
				if (productIndex.isLinked(productCandidate))
					continue; // the exchange is a linked input
				if (e.isInput || e.flowType == FlowType.ELEMENTARY_FLOW)
					indexFlow(e, index);
				else if (allocationMethod == null
						|| allocationMethod == AllocationMethod.NONE)
					indexFlow(e, index); // non-allocated co-product -> handle
											// like elementary flow
			}
		}
		return index;
	}

	private Map<Long, List<PicoExchange>> loadExchanges() {
		try {
			return exchanges.get(productIndex.getProcessIds());
		} catch (Exception e) {
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

	private void indexFlow(PicoExchange e, FlowIndex index) {
		if (e.isInput)
			index.putInputFlow(e.flowID);
		else
			index.putOutputFlow(e.flowID);
	}
}
