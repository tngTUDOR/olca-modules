package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechGraph;

public interface IProductIndexBuilder {

	TechGraph build(LongPair refProduct);

	TechGraph build(LongPair refProduct, double demand);

}