package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Stack;

import org.openlca.cloud.model.data.DatasetDescriptor;
import org.openlca.core.database.IDatabase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FetchWriter extends DataWriter {

	private String commitId;

	public FetchWriter(IDatabase database) {
		super(database);
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public void put(DatasetDescriptor descriptor, String data, File binDir)
			throws IOException {
		if (data != null && !data.isEmpty()) {
			JsonElement element = new Gson().fromJson(data, JsonElement.class);
			JsonObject object = element.isJsonObject() ? element
					.getAsJsonObject() : null;
			entityStore.put(descriptor.getType(), object);
			if (binDir != null && binDir.exists())
				putBin(descriptor, binDir);
		}
		putDescriptor(descriptor);
	}

	private void putBin(DatasetDescriptor descriptor, File binDir)
			throws IOException {
		Stack<File> dirs = new Stack<>();
		dirs.push(binDir);
		Path dirPath = binDir.toPath();
		while (!dirs.isEmpty()) {
			File dir = dirs.pop();
			for (File file : dir.listFiles())
				if (file.isDirectory())
					dirs.push(file);
				else
					putBin(descriptor, dirPath, file.toPath());
		}
	}

	private void putBin(DatasetDescriptor descriptor, Path binDir, Path file)
			throws IOException {
		String filename = relativize(binDir, file);
		byte[] data = Files.readAllBytes(file);
		entityStore.putBin(descriptor.getType(), descriptor.getRefId(),
				filename, data);
	}

	private String relativize(Path dir, Path file) {
		return dir.relativize(file).toString().replace('\\', '/');
	}

	@Override
	protected void writeMetaData(FileSystem zip) throws IOException {
		Files.write(zip.getPath("id.txt"), commitId.getBytes(),
				StandardOpenOption.CREATE);
	}

	@Override
	public File getFile() {
		return super.getFile();
	}

}
