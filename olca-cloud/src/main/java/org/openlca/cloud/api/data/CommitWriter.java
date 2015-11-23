package org.openlca.cloud.api.data;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.openlca.cloud.model.data.DatasetDescriptor;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.EntityStore;

public class CommitWriter extends DataWriter {

	private String commitMessage;

	public CommitWriter(IDatabase database) {
		super(database);
	}

	public void putForRemoval(DatasetDescriptor descriptor) {
		putDescriptor(descriptor);
	}

	public void setCommitMessage(String message) {
		this.commitMessage = message;
	}

	@Override
	protected void writeMetaData(FileSystem zip) throws IOException {
		if (commitMessage == null)
			commitMessage = "";
		Files.write(zip.getPath("message.txt"), commitMessage.getBytes(),
				StandardOpenOption.CREATE);
	}

	public void put(CategorizedEntity entity) {
		export.write(entity);
		DatasetDescriptor descriptor = toDescriptor(entity);
		putDescriptor(descriptor);
		if (entity instanceof ImpactMethod)
			putRelated((ImpactMethod) entity);
	}

	private void putRelated(ImpactMethod method) {
		for (ImpactCategory category : method.getImpactCategories()) {
			DatasetDescriptor descriptor = toDescriptor(category);
			putDescriptor(descriptor);
		}
		for (NwSet set : method.getNwSets()) {
			DatasetDescriptor descriptor = toDescriptor(set);
			putDescriptor(descriptor);
		}
	}

	private DatasetDescriptor toDescriptor(CategorizedEntity entity) {
		DatasetDescriptor descriptor = toDescriptor((RootEntity) entity);
		if (entity.getCategory() != null)
			descriptor.setCategoryRefId(entity.getCategory().getRefId());
		if (entity instanceof Category)
			descriptor.setCategoryType(((Category) entity).getModelType());
		else
			descriptor.setCategoryType(ModelType.forModelClass(entity
					.getClass()));
		descriptor.setFullPath(getFullPath(entity));
		return descriptor;
	}

	private DatasetDescriptor toDescriptor(RootEntity entity) {
		DatasetDescriptor descriptor = new DatasetDescriptor();
		descriptor.setLastChange(entity.getLastChange());
		descriptor.setRefId(entity.getRefId());
		descriptor.setName(entity.getName());
		descriptor.setType(ModelType.forModelClass(entity.getClass()));
		descriptor.setVersion(new Version(entity.getVersion()).toString());
		return descriptor;
	}

	private String getFullPath(CategorizedEntity entity) {
		String path = entity.getName();
		Category category = entity.getCategory();
		while (category != null) {
			path = category.getName() + "/" + path;
			category = category.getCategory();
		}
		return path;
	}

	public EntityStore getEntityStore() {
		return entityStore;
	}

}