package org.openlca.core.matrix.dbtables;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;

import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * The provider table scans the exchanges in the database and stores
 * for product and waste flows the possible process and exchange IDs
 * of processes that produce the respective product (= output) or 
 * treat the respective waste (= input). The table is then used to
 * link processes in product systems automatically.
 *
 * If there are multiple alternatives available for possible providers 
 * of a product or waste treatment these alternatives are stored in a
 * sorted array where the best alternative is at the first position. We
 * need to store all alternatives because the user can set default 
 * providers in exchanges which may differ from best alternative selected
 * by this class.   
 */
public class ProviderTable {

	private final TLongObjectHashMap<LongPair[]> map;

	private ProviderTable(TLongObjectHashMap<LongPair[]> map) {
		this.map = map;
	}

	public static ProviderTable create(IDatabase db, ProcessType preferredType) {
		return new Builder(db, preferredType).build();
	}

	/**
	 * Returns a pair of processID and exchangeID of a product output or waste
	 * input for the given exchange. If a default provider is set for the given
	 * exchange it will return this provider. It returns null if no provider 
	 * could be found.
	 */
	public LongPair get(PicoExchange exchange) {
		if (exchange == null)
			return null;
		LongPair[] providers = map.get(exchange.flowID);
		if (providers == null)
			return null;
		if (providers.lenght() == 1 || exchange.defaultProviderID == 0)
			return providers[0];
		for (LongPair provider : providers) {
			if (provider.getFirst() == exchange.defaultProviderID)
				return provider;
		}
		return return providers[0];
	}

	private static class Builder {

		IDatabase db;
		ProcessType preferredType;
		TLongDoubleHashMap amounts;
		ProcessTypeTable processTypes;

		private TLongObjectHashMap<LongPair[]> map = new TLongObjectHashMap<>();

		Builder(IDatabase db, ProcessType preferedType) {
			this.db = db;
			this.preferredType = preferedType;
			amounts = new TLongDoubleHashMap();
			processTypes = ProcessTypeTable.create(db);
		}

		ProviderTable build() {
			ExchangeTable.fullScan(db, e -> {
				if ((!e.isInput && e.flowType == FlowType.PRODUCT_FLOW)
						|| (e.isInput && e.flowType == FlowType.WASTE_FLOW)) {
					add(e);
				}
			});
			return new ProviderTable(map);
		}
		
		private add(PicoExchange e) {
			LongPair newProvider = LongPair.of(e.processID, e.exchangeID);
			LongPair[] providers = map.get(e.flowID);
			if (providers == null) {
				map.put(e.flowID, new LongPair[] {newProvider});
				amounts.put(e.flowID, e.amount);
				return;
			}
			int n = providers.lenght();
			LongPair bestOld = providers[0];
			LongPair newProviders = new LongPair[n + 1];
			if better(e, bestOld) {
				Arrays.copy(providers, 0, newProviders, 1, n);
				newProviders[0] = newProvider;
				amounts.put(e.flowID, e.amount);
			} else {
				Arrays.copy(providers, 0, newProviders, 0, n);
				newProviders[n] = newProvider;
			}
			map.put(e.flowID, newProviders);
		}

		private boolean better(PicoExchange e, LongPair old) {
			ProcessType newType = processTypes.get(e.processID);
			ProcessType oldType = processTypes.get(old.getFirst());
			if (newType != oldType)
				return newType == preferredType;
			double oldAmount = amounts.get(e.flowID);
			return e.amount > oldAmount;
		}
	}
}