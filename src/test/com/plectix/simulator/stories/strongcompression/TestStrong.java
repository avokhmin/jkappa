package com.plectix.simulator.stories.strongcompression;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.plectix.simulator.component.stories.Stories;
import com.plectix.simulator.component.stories.compressions.CompressionPassport;
import com.plectix.simulator.component.stories.storage.StoryStorageException;
import com.plectix.simulator.component.stories.storage.WireStorageInterface;
import com.plectix.simulator.stories.InitStoriesTests;

public class TestStrong extends InitStoriesTests {

	private static final String separator = File.separator;
	private static final String path = "test.data" + separator + "stories"
			+ separator + "strong" + separator;
	private Map<Integer, CompressionPassport> passports;

	@Parameters
	public static Collection<Object[]> regExValues() {
		return getAllTestFileNames(path);
	}

	public TestStrong(String fileName) {
		super(path, fileName, false, false, true, false);
		passports = new TreeMap<Integer, CompressionPassport>();
	}

	@Test
	public void testAll() throws StoryStorageException {
		Stories stories = getStories();
		for (Entry<Integer, WireStorageInterface> entry : stories
				.getEventsMapForCurrentStory().entrySet()) {
			if (entry.getValue().isImportantStory()) {
				passports.put(entry.getKey(), entry.getValue()
						.extractPassport());
			}
		}
	}

}
