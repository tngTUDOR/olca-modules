package org.openlca.core.math.data_quality;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.results.ContributionResult;

public class DQResultTest {
	private DQSystem dqSystem;
	private ProductSystem pSystem;
	private Process process1;
	private Process process2;
	private Flow pFlow1;
	private Flow pFlow2;
	private Flow eFlow1;
	private Flow eFlow2;
	private FlowProperty property;
	private UnitGroup unitGroup;
	private ImpactMethod method;

	@Before
	public void setup() {
		createUnitGroup();
		createProperty();
		pFlow1 = createFlow(FlowType.PRODUCT_FLOW);
		pFlow2 = createFlow(FlowType.PRODUCT_FLOW);
		eFlow1 = createFlow(FlowType.ELEMENTARY_FLOW);
		eFlow2 = createFlow(FlowType.ELEMENTARY_FLOW);
		createDQSystem();
		process1 = createProcess(pFlow1, "(1;2;3;4;5)", 2, pFlow2, 3, "(1;2;3;4;5)", 4, "(5;4;3;2;1)");
		process2 = createProcess(pFlow2, "(5;4;3;2;1)", 5, "(5;4;3;2;1)", 6, "(1;2;3;4;5)");
		createProductSystem();
		createImpactMethod();
	}

	private void createDQSystem() {
		dqSystem = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			DQIndicator indicator = new DQIndicator();
			indicator.position = i;
			dqSystem.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				DQScore score = new DQScore();
				score.position = j;
				indicator.scores.add(score);
			}
		}
		dqSystem = new DQSystemDao(Tests.getDb()).insert(dqSystem);
	}

	private void createProductSystem() {
		pSystem = new ProductSystem();
		pSystem.getProcesses().add(process1.getId());
		pSystem.getProcesses().add(process2.getId());
		ProcessLink link = new ProcessLink();
		link.setFlowId(pFlow2.getId());
		link.setProviderId(process2.getId());
		link.setRecipientId(process1.getId());
		pSystem.getProcessLinks().add(link);
		pSystem.setReferenceProcess(process1);
		pSystem.setReferenceExchange(process1.getQuantitativeReference());
		pSystem.setTargetAmount(1);
		pSystem.setTargetFlowPropertyFactor(pFlow1.getReferenceFactor());
		pSystem.setTargetUnit(unitGroup.getReferenceUnit());
		pSystem = new ProductSystemDao(Tests.getDb()).insert(pSystem);
	}

	private Process createProcess(Flow pFlow, String dqEntry1, double elemAmount1, String elemDqEntry1,
			double elemAmount2, String elemDqEntry2) {
		return createProcess(pFlow, dqEntry1, 0, null, elemAmount1, elemDqEntry1, elemAmount2, elemDqEntry2);
	}

	private Process createProcess(Flow pFlow, String dqEntry, double inputAmount, Flow inputFlow, double elemAmount1,
			String elemDqEntry1, double elemAmount2, String elemDqEntry2) {
		Process process = new Process();
		process.dqSystem = dqSystem;
		process.dqEntry = dqEntry;
		process.exchangeDqSystem = dqSystem;
		Exchange product = createExchange(1, null, pFlow, false);
		process.getExchanges().add(product);
		process.setQuantitativeReference(product);
		process.getExchanges().add(createExchange(elemAmount1, elemDqEntry1, eFlow1, true));
		process.getExchanges().add(createExchange(elemAmount2, elemDqEntry2, eFlow2, true));
		if (inputFlow == null)
			return new ProcessDao(Tests.getDb()).insert(process);
		process.getExchanges().add(createExchange(inputAmount, null, inputFlow, true));
		return new ProcessDao(Tests.getDb()).insert(process);
	}

	private Exchange createExchange(double amount, String dqEntry, Flow flow, boolean input) {
		Exchange e = new Exchange();
		e.setDqEntry(dqEntry);
		e.setFlow(flow);
		e.setInput(input);
		e.setFlowPropertyFactor(flow.getReferenceFactor());
		e.setUnit(unitGroup.getReferenceUnit());
		e.setAmountValue(amount);
		return e;
	}

	private Flow createFlow(FlowType type) {
		Flow flow = new Flow();
		flow.setFlowType(type);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(1d);
		factor.setFlowProperty(property);
		flow.getFlowPropertyFactors().add(factor);
		flow.setReferenceFlowProperty(property);
		return new FlowDao(Tests.getDb()).insert(flow);
	}

	private void createUnitGroup() {
		unitGroup = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		unit.setConversionFactor(1);
		unitGroup.getUnits().add(unit);
		unitGroup.setReferenceUnit(unit);
		unitGroup = new UnitGroupDao(Tests.getDb()).insert(unitGroup);
	}

	private void createProperty() {
		property = new FlowProperty();
		property.setUnitGroup(unitGroup);
		property = new FlowPropertyDao(Tests.getDb()).insert(property);
	}

	private void createImpactMethod() {
		method = new ImpactMethod();
		ImpactCategory c = new ImpactCategory();
		c.getImpactFactors().add(createFactor(2, eFlow1));
		c.getImpactFactors().add(createFactor(8, eFlow2));
		method.getImpactCategories().add(c);
		method = new ImpactMethodDao(Tests.getDb()).insert(method);
	}

	private ImpactFactor createFactor(double factor, Flow flow) {
		ImpactFactor f = new ImpactFactor();
		f.setValue(factor);
		f.setFlow(flow);
		f.setFlowPropertyFactor(flow.getReferenceFactor());
		f.setUnit(unitGroup.getReferenceUnit());
		return f;
	}

	@Test
	public void test() {
		SystemCalculator calculator = new SystemCalculator(MatrixCache.createEager(Tests.getDb()),
				Tests.getDefaultSolver());
		CalculationSetup setup = new CalculationSetup(pSystem);
		setup.setAmount(1);
		setup.impactMethod = Descriptors.toDescriptor(method);
		ContributionResult cResult = calculator.calculateContributions(setup);
		DQResult result = DQResult.calculate(Tests.getDb(), cResult, AggregationType.WEIGHTED_AVERAGE, pSystem.getId());
		ImpactCategory impactCategory = method.getImpactCategories().get(0);
		Assert.assertArrayEquals(new int[] { 4, 4, 3, 2, 2 }, result.get(toDesc(eFlow1)));
		Assert.assertArrayEquals(new int[] { 2, 3, 3, 4, 4 }, result.get(toDesc(eFlow2)));
		Assert.assertArrayEquals(new int[] { 2, 3, 3, 3, 4 }, result.get(toDesc(impactCategory)));
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, result.get(toDesc(process1), toDesc(eFlow1)));
		Assert.assertArrayEquals(new int[] { 5, 4, 3, 2, 1 }, result.get(toDesc(process2), toDesc(eFlow1)));
		Assert.assertArrayEquals(new int[] { 5, 4, 3, 2, 1 }, result.get(toDesc(process1), toDesc(eFlow2)));
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, result.get(toDesc(process2), toDesc(eFlow2)));
		Assert.assertArrayEquals(new int[] { 4, 4, 3, 2, 2 }, result.get(toDesc(process1), toDesc(impactCategory)));
		Assert.assertArrayEquals(new int[] { 2, 2, 3, 4, 4 }, result.get(toDesc(process2), toDesc(impactCategory)));
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, result.get(toDesc(process1)));
		Assert.assertArrayEquals(new int[] { 5, 4, 3, 2, 1 }, result.get(toDesc(process2)));
	}

	@SuppressWarnings("unchecked")
	private <T extends BaseDescriptor> T toDesc(RootEntity entity) {
		return (T) Descriptors.toDescriptor(entity);
	}

	@After
	public void shutdown() {
		new ImpactMethodDao(Tests.getDb()).delete(method);
		new ProductSystemDao(Tests.getDb()).delete(pSystem);
		new ProcessDao(Tests.getDb()).delete(process1);
		new ProcessDao(Tests.getDb()).delete(process2);
		new DQSystemDao(Tests.getDb()).delete(dqSystem);
		new FlowDao(Tests.getDb()).delete(pFlow1);
		new FlowDao(Tests.getDb()).delete(pFlow2);
		new FlowDao(Tests.getDb()).delete(eFlow1);
		new FlowDao(Tests.getDb()).delete(eFlow2);
		new FlowPropertyDao(Tests.getDb()).delete(property);
		new UnitGroupDao(Tests.getDb()).delete(unitGroup);
	}

}
