package org.openlca.core.matrix.dbtables;

public class PicoImpactFactor {

	public long imactCategoryID;
	public long flowID;
	public double conversionFactor;
	public double amount;

	public String amountFormula;
	public PicoUncertainty uncertainty;

}
