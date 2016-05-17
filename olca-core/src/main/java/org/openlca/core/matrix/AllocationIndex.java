package org.openlca.core.matrix;

import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.dbtables.AllocationTable;
import org.openlca.core.matrix.dbtables.PicoAllocationFactor;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongDoubleHashMap;

class AllocationIndex {

	private AllocationTable table;
	private AllocationMethod method;

	/**
	 * Used for physical and economic allocation: directly stores the the
	 * allocation factors for the given process-products.
	 */
	private HashMap<LongPair, Double> productFactors;

	/**
	 * Used for causal allocation: stores the relation process-product ->
	 * exchange -> allocation factor.
	 */
	private HashMap<LongPair, TLongDoubleHashMap> exchangeFactors;

	public static AllocationIndex create(TechGraph productIndex,
			AllocationMethod method, IDatabase db) {
		return new AllocationIndex(productIndex, method, db);
	}

	private AllocationIndex(TechGraph productIndex, AllocationMethod method,
			IDatabase db) {
		this.method = method;
		AllocationTable table = AllocationTable.create(db);
		for (Long processID : productIndex.getProcessIds()) {
			for (PicoAllocationFactor f : table.get(processID)) {
				index(f);
			}
		}
	}

	private void index(PicoAllocationFactor factor) {
		LongPair processProduct = new LongPair(factor.processID,
				factor.productID);
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = table.getDefaultMethod(factor.processID);
		if (_method == null)
			return;
		switch (_method) {
		case CAUSAL:
			tryIndexCausal(processProduct, factor);
			break;
		case ECONOMIC:
			tryIndexForProduct(processProduct, factor, _method);
			break;
		case PHYSICAL:
			tryIndexForProduct(processProduct, factor, _method);
			break;
		default:
			break;
		}
	}

	private void tryIndexCausal(LongPair processProduct,
			PicoAllocationFactor factor) {
		if (factor.method != AllocationMethod.CAUSAL
				|| factor.exchangeID == null)
			return;
		if (exchangeFactors == null)
			exchangeFactors = new HashMap<>();
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null) {
			// 1.0 is the default value -> means no allocation
			map = new TLongDoubleHashMap(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					Constants.DEFAULT_LONG_NO_ENTRY_VALUE, 1d);
			exchangeFactors.put(processProduct, map);
		}
		map.put(factor.exchangeID, factor.value);
	}

	private void tryIndexForProduct(LongPair processProduct,
			PicoAllocationFactor factor, AllocationMethod method) {
		if (factor.method != method)
			return;
		if (method != AllocationMethod.ECONOMIC
				&& method != AllocationMethod.PHYSICAL)
			return;
		if (productFactors == null)
			productFactors = new HashMap<>();
		productFactors.put(processProduct, factor.value);
	}

	public double getFactor(LongPair processProduct,
			PicoExchange calcExchange) {
		if (!calcExchange.isInput && calcExchange.flowType == FlowType.PRODUCT_FLOW)
			return 1d; // TODO: this changes when we allow input-modelling
						// of waste-flows
		AllocationMethod _method = this.method;
		if (this.method == AllocationMethod.USE_DEFAULT)
			_method = table.getDefaultMethod(processProduct.getFirst());
		if (_method == null)
			return 1d;
		switch (_method) {
		case CAUSAL:
			return fetchCausal(processProduct, calcExchange);
		case ECONOMIC:
			return fetchForProduct(processProduct);
		case PHYSICAL:
			return fetchForProduct(processProduct);
		default:
			return 1d;
		}
	}

	private double fetchForProduct(LongPair processProduct) {
		if (productFactors == null)
			return 1d;
		Double factor = productFactors.get(processProduct);
		if (factor == null)
			return 1d;
		else
			return factor;
	}

	private double fetchCausal(LongPair processProduct, PicoExchange e) {
		if (exchangeFactors == null)
			return 1d;
		TLongDoubleHashMap map = exchangeFactors.get(processProduct);
		if (map == null)
			return 1d;
		return map.get(e.exchangeID); // default is 1.0
	}

}