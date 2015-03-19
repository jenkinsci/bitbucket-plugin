package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.UnprotectedRootAction;
import hudson.model.AbstractProject;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BitbucketHookReceiver implements UnprotectedRootAction {

	private static final Logger LOGGER = Logger
			.getLogger(BitbucketHookReceiver.class.getName());

	/**
	 * Will contain a map of files manipulated by commits in the POST, stored by
	 * branch. ie map(branch name, file path).
	 */
	private Map<String, List<String>> manipulatedFiles;

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "bitbucket-hook";
	}

	/**
	 * Bitbucket send <a href=
	 * "https://confluence.atlassian.com/display/BITBUCKET/Write+brokers+(hooks)+for+Bitbucket"
	 * >payload</a> as form-urlencoded
	 * 
	 * <pre>
	 * payload = JSON
	 * </pre>
	 * 
	 * @throws IOException
	 */
	public void doIndex(StaplerRequest req) throws IOException {
		String body = IOUtils.toString(req.getInputStream());
		String contentType = req.getContentType();
		if (contentType != null
				&& contentType.startsWith("application/x-www-form-urlencoded")) {
			body = URLDecoder.decode(body, "utf-8");
		}
		if (body.startsWith("payload="))
			body = body.substring(8);

		LOGGER.fine("Received commit hook notification : " + body);
		JSONObject payload = JSONObject.fromObject(body);

		manipulatedFiles = Collections
				.unmodifiableMap(collectManipulatedFiles(payload));

		processPayload(payload);
	}

	/**
	 * @return unmodifiableList of files manipulated by commits in the POST
	 */
	public Map<String, List<String>> getManipulatedFiles() {
		return manipulatedFiles;
	}

	private void processPayload(JSONObject payload) {

		JSONObject repo = payload.getJSONObject("repository");

		String user = payload.getString("user");
		String url = payload.getString("canon_url")
				+ repo.getString("absolute_url");

		LOGGER.info("Received commit hook notification for " + repo);

		String scm = repo.getString("scm");
		if ("git".equals(scm)) {
			SecurityContext old = ACL.impersonate(ACL.SYSTEM);
			try {
				URIish remote = new URIish(url);

				for (AbstractProject<?, ?> job : Jenkins.getInstance()
						.getAllItems(AbstractProject.class)) {
					LOGGER.info("considering candidate job " + job.getName());
					BitBucketTrigger trigger = job
							.getTrigger(BitBucketTrigger.class);

					if (trigger != null) {

						if (match(job.getScm(), remote)
								&& isJobConcernedByPost(job)) {

							// tell job that this plugins has triggered it
							trigger.onPost(user);

						} else {
							LOGGER.info("job is not concerned by commits in the received POST");
						}
					} else {
						LOGGER.info("job hasn't BitBucketTrigger set");
					}
				}
			} catch (URISyntaxException e) {
				LOGGER.warning("invalid repository URL " + url);
			} finally {
				SecurityContextHolder.setContext(old);
			}

		} else {
			// TODO hg
			throw new UnsupportedOperationException("unsupported SCM type "
					+ scm);
		}
	}

	private boolean isJobConcernedByPost(AbstractProject<?, ?> job) {

		Set<String> jobBranchesConcernedByPost = getJobBranchesConcernedByPost(job);

		if (jobBranchesConcernedByPost.size() > 0) {

			List<FilePath> jobFolders = new ArrayList<FilePath>();
			FilePath workspaceRoot = job.getSomeWorkspace();
			collectFolders(workspaceRoot, jobFolders);

			List<String> manipulatedFilesForBranch;

			for (String branchName : jobBranchesConcernedByPost) {

				manipulatedFilesForBranch = getManipulatedFiles().get(
						branchName);

				String manipulatedFilePathName;
				String jobFolderPathName;

				for (FilePath jobFolder : jobFolders) {

					try {
						jobFolderPathName = jobFolder.toURI().getPath();
						for (String manipulatedFile : manipulatedFilesForBranch) {
							manipulatedFilePathName = new File(manipulatedFile)
									.toURI().getPath();
							if (manipulatedFilePathName
									.startsWith(jobFolderPathName)) {
								return true;
							}
						}
					} catch (IOException e) {
						LOGGER.severe(e.getMessage());
						e.printStackTrace();
					} catch (InterruptedException e) {
						LOGGER.severe(e.getMessage());
						e.printStackTrace();
					}

				}
			}
		}
		LOGGER.info("No commited elements are parts of workspace folders");
		return false;

	}

	/**
	 * 
	 * @param payload
	 * @return a map which contains all files manipulated in the commit, grouped
	 *         by branch
	 */
	@SuppressWarnings("unchecked")
	public Map<String, List<String>> collectManipulatedFiles(JSONObject payload) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		JSONArray commitList = payload.getJSONArray("commits");
		Iterator<JSONObject> commitIt = commitList.iterator();
		JSONObject commit;
		JSONObject jsonFile;
		String branch;
		String fileName;
		while (commitIt.hasNext()) {
			commit = (JSONObject) commitIt.next();
			branch = commit.getString("branch");
			if (!result.containsKey(branch)) {
				result.put(branch, new ArrayList<String>());
			}
			List<String> branchFileList = result.get(branch);
			JSONArray files = commit.getJSONArray("files");
			Iterator<JSONObject> fileIt = files.iterator();
			while (fileIt.hasNext()) {
				jsonFile = fileIt.next();
				fileName = jsonFile.getString("file");
				branchFileList.add(fileName);
			}

		}
		return result;
	}

	private void collectFolders(FilePath folder, List<FilePath> result) {
		try {
			for (FilePath aPath : folder.list()) {
				if (aPath.isDirectory()) {
					result.add(aPath);
					collectFolders(aPath, result);
				}
			}
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean match(SCM scm, URIish url) {
		if (scm instanceof GitSCM) {
			for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
				for (URIish urIish : remoteConfig.getURIs()) {
					if (GitStatus.looselyMatches(urIish, url))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Extract job branches and check if some manipulated file(s) is/are on one
	 * of these
	 * 
	 * @param job
	 * @return true if at least one manipulated file branch relate to one of job
	 *         branche(s), false otherwise
	 */
	private Set<String> getJobBranchesConcernedByPost(AbstractProject<?, ?> job) {
		List<String> jobBranches = new ArrayList<String>();
		for (BranchSpec aSpec : ((GitSCM) job.getScm()).getBranches()) {
			jobBranches.add(aSpec.getName());
		}
		Set<String> manipulatedFilesBranches = getManipulatedFiles().keySet();
		manipulatedFilesBranches.retainAll(jobBranches);
		return manipulatedFilesBranches;
	}

}
