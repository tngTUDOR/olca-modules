package org.openlca.core.matrix;

import org.openlca.core.matrix.dbtables.PicoUncertainty;

public class PicoImpactFactor {

	private long imactCategoryId;
	private long flowId;
	private double conversionFactor;
	private double amount;
	private String amountFormula;

	public PicoUncertainty uncertainty;

	public long getImactCategoryId() {
		return imactCategoryId;
	}

	public void setImactCategoryId(long imactCategoryId) {
		this.imactCategoryId = imactCategoryId;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getAmountFormula() {
		return amountFormula;
	}

	public void setAmountFormula(String amountFormula) {
		this.amountFormula = amountFormula;
	}

}
