package org.openlca.core.matrix;

import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.dbtables.ExchangeTable;
import org.openlca.core.matrix.dbtables.PicoExchange;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InventoryBuilder {

	private final TechIndex techIndex;
	private final IDatabase db;

	private AllocationMethod allocation;
	private AllocationIndex allocationIndex;
	private ExchangeTable exchanges;
	private FlowIndex flowIndex;

	private ExchangeMatrix technologyMatrix;
	private ExchangeMatrix interventionMatrix;

	InventoryBuilder(TechIndex techIndex, IDatabase db) {
		this.techIndex = techIndex;
		this.db = db;
	}

	InventoryBuilder allocation(AllocationMethod method) {
		this.allocation = method;
		return this;
	}

	InventoryBuilder exchanges(ExchangeTable table) {
		this.exchanges = table;
		return this;
	}

	Inventory build() {
		if (allocation != null && allocation != AllocationMethod.NONE)
			allocationIndex = AllocationIndex.create(techIndex, allocation, db);
		if (exchanges == null)
			exchanges = ExchangeTable.create(db);
		flowIndex = FlowIndex.build(exchanges, techIndex, allocation);
		technologyMatrix = new ExchangeMatrix(techIndex.size(),
				techIndex.size());
		interventionMatrix = new ExchangeMatrix(flowIndex.size(),
				techIndex.size());
		return createInventory();
	}

	private Inventory createInventory() {
		Inventory inventory = new Inventory();
		inventory.allocationMethod = allocation;
		inventory.flowIndex = flowIndex;
		inventory.interventionMatrix = interventionMatrix;
		inventory.productIndex = techIndex;
		inventory.technologyMatrix = technologyMatrix;
		fillMatrices();
		return inventory;
	}

	private void fillMatrices() {
		try {
			Map<Long, List<PicoExchange>> map = exchanges.get(
					techIndex.getProcessIds());
			for (Long processID : techIndex.getProcessIds()) {
				List<PicoExchange> exchanges = map.get(processID);
				List<LongPair> processProducts = techIndex
						.getIndexFlows(processID);
				for (LongPair processProduct : processProducts) {
					for (PicoExchange exchange : exchanges) {
						putExchangeValue(processProduct, exchange);
					}
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
		}
	}

	private void putExchangeValue(LongPair processProduct, PicoExchange e) {
		if (!e.isInput && processProduct.equals(e.processID, e.flowID)) {
			// the reference product
			int idx = techIndex.getIndex(processProduct);
			add(idx, processProduct, technologyMatrix, e);

		} else if (e.flowType == FlowType.ELEMENTARY_FLOW) {
			// elementary exchanges
			addIntervention(processProduct, e);

		} else if (e.isInput) {

			LongPair inputProduct = new LongPair(e.processID, e.flowID);
			if (techIndex.isLinked(inputProduct)) {
				// linked product inputs
				addProcessLink(processProduct, e, inputProduct);
			} else {
				// an unlinked product input
				addIntervention(processProduct, e);
			}

		} else if (allocation == null
				|| allocation == AllocationMethod.NONE) {
			// non allocated output products
			addIntervention(processProduct, e);
		}
	}

	private void addProcessLink(LongPair processProduct, PicoExchange e,
			LongPair inputProduct) {
		LongPair linkedOutput = techIndex.getLinkedTarget(inputProduct);
		int row = techIndex.getIndex(linkedOutput);
		add(row, processProduct, technologyMatrix, e);
	}

	private void addIntervention(LongPair processProduct, PicoExchange e) {
		int row = flowIndex.getIndex(e.flowID);
		add(row, processProduct, interventionMatrix, e);
	}

	private void add(int row, LongPair processProduct, ExchangeMatrix matrix,
			PicoExchange exchange) {
		int col = techIndex.getIndex(processProduct);
		if (row < 0 || col < 0)
			return;
		ExchangeCell existingCell = matrix.getEntry(row, col);
		if (existingCell != null) {
			// self loops or double entries
			exchange = mergeExchanges(existingCell, exchange);
		}
		ExchangeCell cell = new ExchangeCell(exchange);
		if (allocationIndex != null) {
			// note that the allocation table assures that the factor is 1.0 for
			// reference products
			double factor = allocationIndex.getFactor(processProduct, exchange);
			cell.allocationFactor = factor;
		}
		matrix.setEntry(row, col, cell);
	}

	private PicoExchange mergeExchanges(ExchangeCell existingCell,
			PicoExchange addExchange) {
		// a possible allocation factor is handled outside of this function
		PicoExchange exExchange = existingCell.exchange;
		double existingVal = getMergeValue(exExchange);
		double addVal = getMergeValue(addExchange);
		double val = existingVal + addVal;
		PicoExchange newExchange = new PicoExchange();
		newExchange.isInput = val < 0;
		newExchange.conversionFactor = 1;
		newExchange.flowID = addExchange.flowID;
		newExchange.flowType = addExchange.flowType;
		newExchange.processID = addExchange.processID;
		newExchange.amount = Math.abs(val);
		if (exExchange.amountFormula != null
				&& addExchange.amountFormula != null) {
			newExchange.amountFormula = "abs( " + getMergeFormula(exExchange)
					+ " + " + getMergeFormula(addExchange) + ")";
		}
		newExchange.costValue = getMergeCosts(exExchange, addExchange);
		// TODO: adding up uncertainty information (with formulas!) is not yet
		// handled
		return newExchange;
	}

	private double getMergeValue(PicoExchange e) {
		double v = e.amount * e.conversionFactor;
		if (e.isInput && !e.isAvoidedProduct)
			return -v;
		else
			return v;
	}

	private String getMergeFormula(PicoExchange e) {
		String f;
		if (e.amountFormula == null)
			f = Double.toString(e.amount);
		else
			f = "(" + e.amountFormula + ")";
		if (e.conversionFactor != 1)
			f += " * " + e.conversionFactor;
		if (e.isInput && !e.isAvoidedProduct)
			f = "( -1 * (" + f + "))";
		return f;
	}

	private double getMergeCosts(PicoExchange e1, PicoExchange e2) {
		if (e1.costValue == 0)
			return e2.costValue;
		if (e2.costValue == 0)
			return e1.costValue;
		// TODO: this would be rarely the case but if the same flow in a single
		// process is given in different currencies with different conversion
		// the following would be not correct.
		double v1 = e1.isInput ? e1.costValue : -e1.costValue;
		double v2 = e2.isInput ? e2.costValue : -e2.costValue;
		// TODO: cost formulas
		return Math.abs(v1 + v2);
	}
}
