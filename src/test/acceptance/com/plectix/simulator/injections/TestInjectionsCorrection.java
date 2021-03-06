package com.plectix.simulator.injections;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import com.plectix.simulator.interfaces.ObservableConnectedComponentInterface;
import com.plectix.simulator.parser.SimulationDataFormatException;
import com.plectix.simulator.simulationclasses.injections.Injection;
import com.plectix.simulator.staticanalysis.Agent;
import com.plectix.simulator.util.CorrectionsDataParser;
import com.plectix.simulator.util.Failer;
import com.plectix.simulator.util.MessageConstructor;

public class TestInjectionsCorrection extends TestInjections {
	private static int shift = 609;
	private static final String separator = File.separator;
	private final Map<String, SortedSet<Long>> compareData;
	private final Failer failer = new Failer();
	private boolean antiFlag = false;

	public TestInjectionsCorrection() throws FileNotFoundException, SimulationDataFormatException, IOException {
		compareData = (new CorrectionsDataParser("test.data" + separator
				+ "InjectionsCorrectionData" + DEFAULT_EXTENSION_FILE))
				.parse();
	}

	@Test
	public void test0() {
		MessageConstructor mc = new MessageConstructor();
		for (ObservableConnectedComponentInterface c : getInitializator()
				.getObservables()) {
			if (!c.getName().startsWith("q")) {
				if (!testCC(c)) {
					mc.addValue(c.getName());
				}
			}
		}
		if (!mc.isEmpty()) {
			failer.failOnMC(mc);
		}
	}

	public boolean testCC(ObservableConnectedComponentInterface c) {
		if (c.getName().startsWith("scary")) {
			return true;
		}
		SortedSet<Long> solutionLinkingForCurrentObs = new TreeSet<Long>();

		Collection<Injection> injectionsList = c.getInjectionsList();
		for (Injection injection : injectionsList) {
			for (Agent agent : injection.getCorrespondence().values()) {
				solutionLinkingForCurrentObs.add(agent.getId()-shift);
			}
		}

		boolean fail = solutionLinkingForCurrentObs.equals(compareData.get(c
				.getName()));

		// print first failed test info in console
		if (!fail & !antiFlag) {
			antiFlag = true;
		}
		return (fail);
	}
}
