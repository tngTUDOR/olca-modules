package org.openlca.core.math;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

/**
 * A setup for a product system calculation.
 */
public class CalculationSetup {

	public static final int QUICK_RESULT = 1;
	public static final int ANALYSIS = 2;
	public static final int MONTE_CARLO_SIMULATION = 4;
	public static final int COST_CALCULATION = 8;

	private final int type;
	private final ProductSystem productSystem;
	private ImpactMethodDescriptor impactMethod;
	private NormalizationWeightingSet nwSet;
	private AllocationMethod allocationMethod;
	private int numberOfRuns = -1;
	private List<ParameterRedef> parameterRedefs = new ArrayList<>();

	/**
	 * Creates a new calculation setup for the given type. The type can be
	 * combination of the type constants defined in this class (combination via
	 * binary or, e.g. <code> new CalculationSetup(CalculationSetup.ANALYSIS |
	 * CalculationSetup.COST_CALCULATION). </code>
	 */
	public CalculationSetup(ProductSystem productSystem, int type) {
		this.productSystem = productSystem;
		this.type = type;
	}

	/**
	 * Returns true if the setup is a setup for the given type. The given type
	 * should be one of the constants defined in this class.
	 */
	public boolean hasType(int type) {
		return (type & this.type) == type;
	}

	public ProductSystem getProductSystem() {
		return productSystem;
	}

	public void setImpactMethod(ImpactMethodDescriptor impactMethod) {
		this.impactMethod = impactMethod;
	}

	public ImpactMethodDescriptor getImpactMethod() {
		return impactMethod;
	}

	public void setNwSet(NormalizationWeightingSet nwSet) {
		this.nwSet = nwSet;
	}

	public NormalizationWeightingSet getNwSet() {
		return nwSet;
	}

	public void setAllocationMethod(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	public AllocationMethod getAllocationMethod() {
		if (allocationMethod == null)
			return AllocationMethod.NONE;
		return allocationMethod;
	}

	/** Only valid for sensitivity analysis of Monte-Carlo-Simulations. */
	public void setNumberOfRuns(int numberOfRuns) {
		this.numberOfRuns = numberOfRuns;
	}

	/** Only valid for sensitivity analysis of Monte-Carlo-Simulations. */
	public int getNumberOfRuns() {
		return numberOfRuns;
	}

	public List<ParameterRedef> getParameterRedefs() {
		return parameterRedefs;
	}

}