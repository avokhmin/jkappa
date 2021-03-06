package com.plectix.simulator.updates;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Before;

import com.plectix.simulator.DirectoryTestsRunner;
import com.plectix.simulator.Initializator;
import com.plectix.simulator.interfaces.ConnectedComponentInterface;
import com.plectix.simulator.parser.SimulationDataFormatException;
import com.plectix.simulator.simulationclasses.injections.Injection;
import com.plectix.simulator.simulator.KappaSystem;
import com.plectix.simulator.simulator.Simulator;
import com.plectix.simulator.simulator.ThreadLocalData;
import com.plectix.simulator.simulator.UpdatesPerformer;
import com.plectix.simulator.staticanalysis.Rule;
import com.plectix.simulator.staticanalysis.stories.storage.StoryStorageException;
import com.plectix.simulator.util.io.PlxLogger;

public abstract class TestUpdate extends DirectoryTestsRunner {

	private Simulator mySimulator;
	private static final PlxLogger LOGGER = ThreadLocalData
			.getLogger(Simulator.class);
	private double currentTime = 0.;
	private Rule myActiveRule;

	private String myTestFileName = "";

	private List<Injection> myCurrentInjectionsList;
	private final Integer operationMode;

	protected TestUpdate(String fileName, Integer opMode) {
		super();
		myTestFileName = fileName;
		operationMode = opMode;
	}

	public abstract void init() throws FileNotFoundException, SimulationDataFormatException, IOException;

	@Override
	public abstract String getPrefixFileName();

	public abstract boolean isDoingPositive();

	@Before
	public void setup() throws Exception {
		String fullTestFilePath = getPrefixFileName() + myTestFileName;
		Initializator initializator = getInitializator();

		initializator.init(fullTestFilePath, operationMode);
		mySimulator = initializator.getSimulator();
		run();
		init();
	}

	public Rule getActiveRule() {
		return myActiveRule;
	}

	public Collection<Rule> getRules() {
		return mySimulator.getSimulationData().getKappaSystem().getRules();
	}

	public List<Injection> getCurrentInjectionsList() {
		return myCurrentInjectionsList;
	}

	private void run() throws StoryStorageException {
		KappaSystem kappaSystem = mySimulator.getSimulationData()
				.getKappaSystem();

		mySimulator.getSimulationData().getKappaSystem().getObservables()
				.calculateObs(
						currentTime,
						1,
						mySimulator.getSimulationData()
								.getSimulationArguments().isTime());
		mySimulator.getSimulationData().getKappaSystem().updateRuleActivities();
		myActiveRule = mySimulator.getSimulationData().getKappaSystem()
		.getRandomRule();

		if (myActiveRule == null) {
//			mySimulator.getSimulationData().getClock().setTimeLimit(currentTime);
			fail(myTestFileName + " : there's no active rules");
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Rule: " + myActiveRule.getName());

		// myCurrentInjectionsList = ruleProbabilityCalculation
		// .getSomeInjectionList(myActiveRule);
		myCurrentInjectionsList = kappaSystem
				.chooseInjectionsForRuleApplication(myActiveRule);
		currentTime += mySimulator.getSimulationData().getKappaSystem()
				.getTimeValue();

		if (myCurrentInjectionsList != null) {
			// negative update
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("negative update");

			myActiveRule.applyRule(myCurrentInjectionsList, mySimulator
					.getSimulationData());
			UpdatesPerformer.doNegativeUpdate(myCurrentInjectionsList);
			if (isDoingPositive()) {
				mySimulator
						.getSimulationData()
						.getKappaSystem()
						.doPositiveUpdate(myActiveRule, myCurrentInjectionsList);
			}

		} else {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Clash");
		}
	}

	public static boolean lhsIsEmpty(List<ConnectedComponentInterface> lh) {
		return (lh.size() == 1)
				&& (lh.contains(ThreadLocalData.getEmptyConnectedComponent()));
	}
}
