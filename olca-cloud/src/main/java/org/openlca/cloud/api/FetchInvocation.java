package org.openlca.cloud.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openlca.cloud.api.data.FetchReader;
import org.openlca.cloud.model.data.Dataset;
import org.openlca.cloud.util.Directories;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.jsonld.Dates;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Invokes a web service call to fetch the latest changes after the specified
 * commit id
 */
class FetchInvocation {

	private static final String PATH = "/fetch/";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	String baseUrl;
	String sessionId;
	String repositoryId;
	String lastCommitId;
	List<String> fetchData;
	Map<Dataset, JsonObject> mergedData;

	FetchInvocation(IDatabase database) {
		this.database = database;
	}

	/**
	 * Retrieves all changed data sets since the last fetch
	 * 
	 * @return The latest commit id
	 * @throws WebRequestException
	 *             If user is out of sync or has no access to the specified
	 *             repository
	 */
	String execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(repositoryId, "repository id");
		if (lastCommitId == null || lastCommitId.isEmpty())
			lastCommitId = "null";
		if (fetchData == null) // still call service to receive latest commit id
			fetchData = new ArrayList<>();
		String url = baseUrl + PATH + repositoryId + "/" + lastCommitId;
		ClientResponse response = WebRequests.call(Type.POST, url, sessionId,
				fetchData);
		if (response.getStatus() == Status.NO_CONTENT.getStatusCode())
			return null;
		return handleResponse(response.getEntityInputStream());
	}

	private String handleResponse(InputStream data) {
		Path dir = null;
		FetchReader reader = null;
		try {
			dir = Files.createTempDirectory("fetchReader");
			File zipFile = new File(dir.toFile(), "fetch.zip");
			Files.copy(data, zipFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			reader = new FetchReader(zipFile);
			return doImport(reader);
		} catch (IOException e) {
			log.error("Error reading fetch data", e);
			return null;
		} finally {
			if (dir != null && dir.toFile().exists())
				Directories.delete(dir.toFile());
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Error closing fetch reader", e);
				}
		}
	}

	private String doImport(FetchReader reader) {
		putMergedData(reader.getEntityStore());
		JsonImport jsonImport = new JsonImport(reader.getEntityStore(), database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		jsonImport.run();
		for (Dataset descriptor : reader.getDescriptors()) {
			if (reader.hasData(descriptor))
				continue;
			delete(Daos.createCategorizedDao(database, descriptor.type), descriptor.refId);
		}
		for (Entry<Dataset, JsonObject> entry : mergedData.entrySet())
			if (entry.getValue() == null)
				delete(Daos.createCategorizedDao(database, entry.getKey().type), entry.getKey().refId);
		return reader.getCommitId();
	}

	private void putMergedData(EntityStore store) {
		for (Entry<Dataset, JsonObject> entry : mergedData.entrySet()) {
			JsonObject json = entry.getValue();
			if (json == null)
				continue;
			Version version = Version.fromString(json.get("version")
					.getAsString());
			version.incUpdate();
			json.addProperty("version", Version.asString(version.getValue()));
			json.addProperty("lastChange",
					Dates.toString(Calendar.getInstance().getTime()));
			store.put(entry.getKey().type, json);
			if (isImpactMethod(json)) {
				putReferences(json, "impactCategories",
						ModelType.IMPACT_CATEGORY, store);
				putReferences(json, "nwSets", ModelType.NW_SET, store);
			}
		}
	}

	private void putReferences(JsonObject json, String field, ModelType type,
			EntityStore store) {
		if (!json.has(field))
			return;
		JsonArray array = json.getAsJsonArray(field);
		for (JsonElement element : array)
			if (element.isJsonObject())
				store.put(type, element.getAsJsonObject());
	}

	private boolean isImpactMethod(JsonObject json) {
		String type = json.get("@type").getAsString();
		return ImpactMethod.class.getSimpleName().equals(type);
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void delete(
			CategorizedEntityDao<T, V> dao, String refId) {
		dao.delete(dao.getForRefId(refId));
	}

}
