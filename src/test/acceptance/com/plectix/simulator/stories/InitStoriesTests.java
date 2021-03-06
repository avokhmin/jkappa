package com.plectix.simulator.stories;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.junit.Before;

import com.plectix.simulator.DirectoryTestsRunner;
import com.plectix.simulator.Initializator;
import com.plectix.simulator.io.SimulationDataReader;
import com.plectix.simulator.simulator.KappaSystem;
import com.plectix.simulator.simulator.SimulationArguments;
import com.plectix.simulator.simulator.SimulationData;
import com.plectix.simulator.simulator.Simulator;
import com.plectix.simulator.staticanalysis.Rule;
import com.plectix.simulator.staticanalysis.stories.Stories;
import com.plectix.simulator.util.Info.InfoType;

public class InitStoriesTests extends DirectoryTestsRunner {
	private static String testDirectory = "";

	private String FileName = "";
	private Simulator mySimulator;
	// private CStoryTrees storyTrees;

	private double time = 10;
	private boolean isSlow;
	private boolean isWeak;
	private boolean isStrong;
	private boolean mode;

	private Integer operationMode;

	@Override
	public String getPrefixFileName() {
		return testDirectory;
	}

//	@Parameters
//	public static Collection<Object[]> regExValues() {
//		return OperationModeCollectionGenerator.generate(getAllTestFileNames(getDirectory()));
//	}

	public InitStoriesTests(String path, String fileName, boolean isSlow,
			boolean isWeak, boolean isStrong, boolean mode, Integer opMode) {
		testDirectory = path;
		FileName = fileName;
		this.isSlow = isSlow;
		this.isWeak = isWeak;
		this.mode = mode;
		this.isStrong = isStrong;
		operationMode = opMode;
	}

	@Before
	public void setup() throws Exception {
		init(testDirectory + FileName, operationMode);
//		if (mode) {
//			mySimulator.getSimulationData().getClock().setTimeLimit(time);
//		}
		try {
			mySimulator.runStories();
		} catch (Exception e) {
			e.printStackTrace();
			junit.framework.Assert.fail(e.getMessage());
		}
	}

	@Override
	public void init(String filePath, Integer opMode) throws Exception {
		mySimulator = new Simulator();

		SimulationData simulationData = mySimulator.getSimulationData();

		SimulationArguments args = null;
		try {
			// seed=2: 13
			// seed=9: 13
			// seed=13: 11(storage)
			if (mode) {
				args = Initializator.prepareStorifyArguments(filePath, isSlow,
						isWeak, isStrong, true, Long.valueOf(1000), Integer
								.valueOf(13), opMode);
			} else {
				args = Initializator.prepareStorifyArguments(filePath, isSlow,
						isWeak, isStrong, false, Long.valueOf(500), Integer
								.valueOf(5), opMode);

			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}

		simulationData.setSimulationArguments(InfoType.OUTPUT, args);
		(new SimulationDataReader(simulationData)).readAndCompile();
		simulationData.getKappaSystem().initialize();
	}

	public static String getDirectory() {
		return testDirectory;
	}

	public Stories getStories() {
		return mySimulator.getSimulationData().getKappaSystem().getStories();
	}

	public List<Rule> getRules() {
		return mySimulator.getSimulationData().getKappaSystem().getRules();
	}

	public KappaSystem getKappaSystem() {
		return mySimulator.getSimulationData().getKappaSystem();
	}

}
