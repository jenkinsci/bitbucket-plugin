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

	private static final String NEW_FILE_POST = "newfile.json";
	private static final String DELETE_FILE_POST = "deletedfile.json";
	private static final String NEW_FOLDER_POST = "newfolder.json";
	private static final String DELETE_FOLDER_POST = "deletefolder.json";
	private static final String NEW_FILE_SECOND_COMMIT = "newfileSecondCommit.json";

	@Before
	public void setUp() {
		fixture = new BitbucketHookReceiver() {
			@Override
			public Map<String, List<String>> getManipulatedFiles() {
				JSONObject payload = getMockPost(NEW_FILE_POST);
				return getFixture().collectManipulatedFiles(payload);
			}
		};
	}

	private BitbucketHookReceiver getFixture() {
		return fixture;
	}

	@Test
	public void testCollectManipulatedFiles() {

		JSONObject payload = getMockPost(NEW_FILE_POST);

		assertNotNull("Payload must be decoded", payload);

		Map<String, List<String>> result = getFixture()
				.collectManipulatedFiles(payload);

		assertEquals("We have 2 commits", 2, payload.getJSONArray("commits")
				.size());

		assertTrue("Json result must contains the 'develop' branch",
				result.containsKey("develop"));
		assertTrue("Json result must contains the 'master' branch",
				result.containsKey("master"));

		assertEquals("develop commit has 1 files", 1, result.get("develop")
				.size());
		assertEquals("master commit has 1 files", 1, result.get("master")
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

		assertEquals("The mock workspace contains 9 folder", 9, result.size());
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

	@Test
	public void testIsJobConcernedByPostNewFile() {

		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();
		Set<String> jobBranchesConcernedByPost = new HashSet<String>();
		jobBranchesConcernedByPost.add("develop");

		fixture = new BitbucketHookReceiver() {
			@Override
			public Map<String, List<String>> getManipulatedFiles() {
				JSONObject payload = getMockPost(NEW_FILE_POST);
				return getFixture().collectManipulatedFiles(payload);
			}
		};

		boolean result = fixture.isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertTrue(result);

	}

	@Test
	public void testSimplifyRefSpecWithComplete() {

		String refspec = "refs/heads/develop";
		String expected = "develop";

		String result = getFixture().simplifyRefSpec(refspec);
		assertEquals(
				"A complete refspec must be simplified to cope with Bitbucket POST format",
				expected, result);

	}

	@Test
	public void testSimplifyRefSpecWithStarred() {

		String refspec = "*/develop";
		String expected = "develop";

		String result = getFixture().simplifyRefSpec(refspec);
		assertEquals("A Jenkins refspec including a star must be simplified",
				expected, result);

	}

	@Test
	public void testSimplifyRefSpecWithShort() {

		String refspec = "develop";
		String expected = "develop";

		String result = getFixture().simplifyRefSpec(refspec);
		assertEquals("A short refspec must not be truncated", expected, result);

	}

	@Test
	public void testIsJobConcernedByPostNewFolder() {

		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();
		Set<String> jobBranchesConcernedByPost = new HashSet<String>();
		jobBranchesConcernedByPost.add("develop");

		fixture = new BitbucketHookReceiver() {
			@Override
			public Map<String, List<String>> getManipulatedFiles() {
				JSONObject payload = getMockPost(NEW_FOLDER_POST);
				return getFixture().collectManipulatedFiles(payload);
			}
		};

		boolean result = fixture.isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertTrue(result);

	}

	@Test
	public void testIsJobConcernedByPostDeletedFolder() {

		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();
		Set<String> jobBranchesConcernedByPost = new HashSet<String>();
		jobBranchesConcernedByPost.add("develop");

		fixture = new BitbucketHookReceiver() {
			@Override
			public Map<String, List<String>> getManipulatedFiles() {
				JSONObject payload = getMockPost(DELETE_FOLDER_POST);
				return getFixture().collectManipulatedFiles(payload);
			}
		};

		boolean result = fixture.isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertTrue(result);

	}

	@Test
	public void testIsJobConcernedByPostDeletedFile() {

		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();
		Set<String> jobBranchesConcernedByPost = new HashSet<String>();
		jobBranchesConcernedByPost.add("develop");

		fixture = new BitbucketHookReceiver() {
			@Override
			public Map<String, List<String>> getManipulatedFiles() {
				JSONObject payload = getMockPost(DELETE_FILE_POST);
				return getFixture().collectManipulatedFiles(payload);
			}
		};

		boolean result = fixture.isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertTrue(result);

	}

	@Test
	public void testIsJobConcernedByPostNewFileOnSecondCommit() {

		FilePath mockWorkspaceRoot = getMockWorkspaceRoot();
		Set<String> jobBranchesConcernedByPost = new HashSet<String>();
		jobBranchesConcernedByPost.add("develop");

		fixture = new BitbucketHookReceiver() {
			@Override
			public Map<String, List<String>> getManipulatedFiles() {
				JSONObject payload = getMockPost(NEW_FILE_SECOND_COMMIT);
				return getFixture().collectManipulatedFiles(payload);
			}
		};

		boolean result = fixture.isJobConcernedByPost(mockWorkspaceRoot,
				jobBranchesConcernedByPost);

		assertTrue(result);

	}

	/* utility methods */

	private FilePath getMockWorkspaceRoot() {

		FilePath workspaceRoot = null;
		String mockWorkspacePath = BitbucketHookReceiverTest.class.getResource(
				"/" + NEW_FILE_POST).getPath();
		mockWorkspacePath = mockWorkspacePath.substring(0,
				mockWorkspacePath.lastIndexOf('/'))
				+ File.separator + "workspace" + File.separator;

		File tmp = new File(mockWorkspacePath);
		workspaceRoot = new FilePath(tmp);

		return workspaceRoot;
	}

	private JSONObject getMockPost(String mock) {
		JSONObject payload = null;
		String mockPostFilePath = BitbucketHookReceiverTest.class.getResource(
				"/" + mock).getPath();
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
