package org.openlca.yaml;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.yaml.Document;

public class UnitGroupTest {

	private UnitGroup unitGroup;

	@Before
	public void setUp() {
		InputStream is = getClass().getResourceAsStream("UnitGroupTest.yaml");
		Document doc = Document.read(is);
		unitGroup = doc.unitGroups.get(0);
	}

	@Test
	public void testAttributes() {
		Assert.assertEquals("Units of mass", unitGroup.getName());
		Assert.assertEquals("91d1919d-e58f-498b-b620-b3e2694f4f1d", unitGroup.getRefId());
	}

	@Test
	public void testUnits() {
		Assert.assertEquals(2, unitGroup.getUnits().size());
		boolean kgFound = false;
		boolean gFound = false;
		for (Unit unit : unitGroup.getUnits()) {
			if (unit.getName().equals("kg")) {
				kgFound = true;
				Assert.assertEquals(1.0, unit.getConversionFactor(), 1e-16);
			}
			if (unit.getName().equals("g")) {
				gFound = true;
				Assert.assertEquals(0.001, unit.getConversionFactor(), 1e-16);
			}
		}
		Assert.assertTrue(kgFound && gFound);
	}

	@Test
	public void testReferenceUnit() {
		Unit refUnit = unitGroup.getReferenceUnit();
		Assert.assertNotNull(refUnit);
		Assert.assertEquals("kg", refUnit.getName());
	}
}