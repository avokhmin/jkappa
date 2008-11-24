package com.plectix.simulator.perturbations;


import org.junit.Before;

import com.plectix.simulator.*;
import com.plectix.simulator.components.*;
import com.plectix.simulator.simulator.*;

public abstract class TestPerturbation extends DirectoryTestsRunner implements Test {
	private String myTestFileName = "";
	private Simulator mySimulator;
	private SimulatorManager myManager;
	
	protected TestPerturbation (String fileName) {
		super();
		myTestFileName = fileName;
	}
	
	@Before
	public void setup() {
		String fullTestFilePath = getPrefixFileName() + myTestFileName;
		Initializator initializator = getInitializator();
		
		initializator.init(fullTestFilePath);
		mySimulator = initializator.getSimulator();
		myManager = initializator.getManager();
		mySimulator.run(null);
		init();
	}
	
	public abstract String getPrefixFileName();
	public abstract void init();
	
	public CRule getRuleByName(String name) {
		for (CRule rule : myManager.getRules()) {
			if (name.equals(rule.getName())) {
				return rule;
			}
		}
		return null;
	}
}