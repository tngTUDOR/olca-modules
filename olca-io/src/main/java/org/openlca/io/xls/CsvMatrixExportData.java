package org.openlca.io.xls;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;

public class CsvMatrixExportData {

	public IDatabase db;
	public EntityCache cache;
	public ProductSystem productSystem;
	public File technologyFile;
	public File interventionFile;
	public String decimalSeparator;
	public String columnSeperator;

	boolean valid() {
		return db != null && cache != null && productSystem != null
				&& technologyFile != null && interventionFile != null
				&& decimalSeparator != null && columnSeperator != null;
	}

}
