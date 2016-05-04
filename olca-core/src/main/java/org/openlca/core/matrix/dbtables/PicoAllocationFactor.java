package org.openlca.core.matrix.dbtables;

import org.openlca.core.model.AllocationMethod;

public class PicoAllocationFactor {

	public long processID;
	public long productID;
	public double value;

	public Long exchangeID;
	public AllocationMethod method;

}
