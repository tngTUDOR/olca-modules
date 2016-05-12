package org.openlca.core.math;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.ProjectResultProvider;

public class ProjectCalculator {

	private final IMatrixSolver solver;
	private final IDatabase db;

	public ProjectCalculator(IDatabase db, IMatrixSolver solver) {
		this.db = db;
		this.solver = solver;
	}

	public ProjectResultProvider solve(Project project, EntityCache cache) {
		ProjectResultProvider result = new ProjectResultProvider(cache);
		SystemCalculator calculator = new SystemCalculator(db, solver);
		ImpactMethodDescriptor method = getImpactMethod(project);
		NwSetDescriptor nwSet = getNwSet(project);
		for (ProjectVariant v : project.getVariants()) {
			CalculationSetup setup = new CalculationSetup(v.getProductSystem());
			setup.setUnit(v.getUnit());
			setup.setFlowPropertyFactor(v.getFlowPropertyFactor());
			setup.setAmount(v.getAmount());
			setup.allocationMethod = v.getAllocationMethod();
			setup.impactMethod = method;
			setup.nwSet = nwSet;
			setup.parameterRedefs.addAll(v.getParameterRedefs());
			setup.withCosts = true;
			ContributionResult cr = calculator.calculateContributions(setup);
			result.addResult(v, cr);
		}
		return result;
	}

	private ImpactMethodDescriptor getImpactMethod(Project project) {
		if (project.getImpactMethodId() == null)
			return null;
		ImpactMethodDao dao = new ImpactMethodDao(db);
		return dao.getDescriptor(project.getImpactMethodId());
	}

	private NwSetDescriptor getNwSet(Project project) {
		if (project.getNwSetId() == null)
			return null;
		NwSetDao dao = new NwSetDao(db);
		return dao.getDescriptor(project.getNwSetId());
	}
}
