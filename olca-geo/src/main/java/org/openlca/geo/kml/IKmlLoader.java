package org.openlca.geo.kml;

import java.util.List;

import org.openlca.core.matrix.TechGraph;

public interface IKmlLoader {
	
	List<LocationKml> load(TechGraph index);

}
