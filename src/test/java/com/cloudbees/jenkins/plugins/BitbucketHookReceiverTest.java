package com.cloudbees.jenkins.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import hudson.FilePath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Before;
import org.junit.Test;

public class BitbucketHookReceiverTest {

	BitbucketHookReceiver fixture;

	@Before
	public void setUp() {
		fixture = new BitbucketHookReceiver();
	}

	private BitbucketHookReceiver getFixture() {
		return fixture;
	}

	@Test
	public void testCollectManipulatedFiles() {

		JSONObject payload = getMockPost();

		assertNotNull("Payload must be decoded", payload);

		Map<String, List<String>> result = getFixture()
				.collectManipulatedFiles(payload);

		assertEquals("We have 2 commits", 2, payload.getJSONArray("commits")
				.size());

		assertTrue("Json result must contains the 'develop' branch",
				result.containsKey("develop"));
		assertTrue("Json result must contains the 'master' branch",
				result.containsKey("master"));

		assertEquals("develop commit has 4 files", 4, result.get("develop")
				.size());
		assertEquals("master commit has 3 files", 3, result.get("master")
				.size());

	}

	@Test
	public void testCollectFolders() {

		List<FilePath> result = new ArrayList<FilePath>();
		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();

		try {
			assertTrue("The mock workspace must be a folder",
					mockWorkspaceRoot.isDirectory());
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

		assertNotNull("The mock workspace must be resolved", mockWorkspaceRoot);
		getFixture().collectFolders(mockWorkspaceRoot, result);

		assertEquals("The mock workspace contains lib/ and middleware/", 2,
				result.size());
	}

	@Test
	public void testIsJobConcernedByPost() {

		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();
		Set<String> jobBranchesConcernedByPost = new HashSet<String>();
		jobBranchesConcernedByPost.add("develop");

		boolean result = getFixture().isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertTrue(result);

		jobBranchesConcernedByPost.clear();
		jobBranchesConcernedByPost.add("pouet");
		result = getFixture().isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertFalse(result);

	}

	/* utility methods */

	private FilePath getMockWorkspaceRoot() {

		FilePath workspaceRoot = null;
		String mockWorkspacePath = BitbucketHookReceiverTest.class.getResource(
				"/mockPost.json").getPath();
		mockWorkspacePath = mockWorkspacePath.substring(0,
				mockWorkspacePath.lastIndexOf('/'))
				+ File.separator + "workspace" + File.separator;

		File tmp = new File(mockWorkspacePath);
		workspaceRoot = new FilePath(tmp);

		return workspaceRoot;
	}

	private JSONObject getMockPost() {
		JSONObject payload = null;
		String mockPostFilePath = BitbucketHookReceiverTest.class.getResource(
				"/mockPost.json").getPath();
		try {
			String jsonContent = readFileAsString(mockPostFilePath);
			payload = (JSONObject) JSONSerializer.toJSON(jsonContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return payload;
	}

	private String readFileAsString(String filePath) throws IOException {
		StringBuffer fileData = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}
}
