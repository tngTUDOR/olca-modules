package org.openlca.cloud.api;

import org.openlca.cloud.util.Strings;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;

/**
 * Invokes a web service call to delete a repository
 */
class DeleteRepositoryInvocation {

	private static final String PATH = "/repository/delete";

	private String baseUrl;
	private String sessionId;
	private String name;

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Deletes the repository with the specified name
	 * 
	 * @throws WebRequestException
	 *             if a repository with the specified name did not exists
	 */
	public void execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(sessionId, "session id");
		Valid.checkNotEmpty(name, "repository name");
		String url = Strings.concat(baseUrl, PATH, "/", name);
		WebRequests.call(Type.DELETE, url, sessionId);
	}

}