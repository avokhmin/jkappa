package com.plectix.simulator.perturbations.triggering;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.plectix.simulator.FileNameCollectionGenerator;
import com.plectix.simulator.Initializator;
import com.plectix.simulator.OperationModeCollectionGenerator;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.io.SimulationDataReader;
import com.plectix.simulator.simulationclasses.perturbations.ComplexPerturbation;
import com.plectix.simulator.simulationclasses.perturbations.ConditionInterface;
import com.plectix.simulator.simulationclasses.perturbations.TimeCondition;
import com.plectix.simulator.simulator.KappaSystem;
import com.plectix.simulator.simulator.SimulationArguments;
import com.plectix.simulator.simulator.SimulationData;
import com.plectix.simulator.simulator.Simulator;
import com.plectix.simulator.util.Info.InfoType;

@RunWith(value = Parameterized.class)
public class TestPerturbationsTriggering {

	private static final String separator = File.separator;
	private static final String testDirectory = "test.data" + separator
			+ "perturbations" + separator + "triggering" + separator;
	private final String prefixFileName;

	private Simulator mySimulator;
	private final Integer[] times = { 200 };
	private final Integer operationMode;

	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> coll = FileNameCollectionGenerator.getAllFileNames(testDirectory);
		return OperationModeCollectionGenerator.generate(coll,true);
	}

	public TestPerturbationsTriggering(String filename, Integer opMode) {
		prefixFileName = filename;
		operationMode = opMode;
	}

	@Test
	public void test() throws Exception {
		for (int i = 0; i < times.length; i++) {
			setup(times[i]);
			KappaSystem kappaSystem = mySimulator.getSimulationData()
					.getKappaSystem();
			List<ComplexPerturbation<?, ?>> perturbations = kappaSystem.getPerturbations();

			// check the perturbations have been triggered
			for (ComplexPerturbation<?, ?> perturbation : perturbations) {
				ConditionInterface condition = perturbation.getCondition();
				
				if (condition instanceof TimeCondition) {
					double timeLimit = ((TimeCondition)condition).getTimeLimit();
					boolean wasPerformed = perturbation.getModification().wasPerformed() ;
					
					if (!wasPerformed && timeLimit < times[i]) {
						fail("perturbation: $T > " + timeLimit + " has not been triggered");
					} else if (wasPerformed	&& timeLimit > times[i])
						fail("perturbation: $T > " + timeLimit	+ " has been triggered, but it must not to");
				}
			}

		}
	}

	public void setup(Integer time) throws Exception {
		init(testDirectory + prefixFileName, time);
		try {
			mySimulator.run(new SimulatorInputData(mySimulator
					.getSimulationData().getSimulationArguments()));
		} catch (Exception e) {
			e.printStackTrace();
			junit.framework.Assert.fail(e.getMessage());
		}
	}

	public void init(String filePath, Integer time) throws Exception {
		mySimulator = null;
		mySimulator = new Simulator();
		SimulationData simulationData = mySimulator.getSimulationData();
		SimulationArguments args = null;
		try {
			args = Initializator.prepareTimeArguments(filePath, time, operationMode);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}

		simulationData.setSimulationArguments(InfoType.OUTPUT, args);
		(new SimulationDataReader(simulationData)).readAndCompile();
		simulationData.getKappaSystem().initialize();
	}

}
