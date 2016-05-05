package org.openlca.core.matrix.dbtables;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.UncertaintyType;

public class PicoUncertainty {

	public double parameter1;
	public double parameter2;
	public double parameter3;

	public UncertaintyType type;

	public String parameter1Formula;
	public String parameter2Formula;
	public String parameter3Formula;

	/**
	 * Creates a number generator for the distribution type and parameters. It
	 * returns null if no meaningful number generator could be created.
	 */
	public NumberGenerator createGenerator() {
		if (type == null)
			return null;
		switch (type) {
		case LOG_NORMAL:
			return NumberGenerator.logNormal(parameter1, parameter2);
		case NORMAL:
			return NumberGenerator.normal(parameter1, parameter2);
		case TRIANGLE:
			return NumberGenerator.triangular(parameter1, parameter2, parameter3);
		case UNIFORM:
			return NumberGenerator.uniform(parameter1, parameter2);
		default:
			return null;
		}
	}
}
