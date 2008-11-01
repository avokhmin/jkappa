package com.plectix.simulator;

import java.util.*;

import org.junit.runners.Parameterized.Parameters;

import org.junit.*;

import com.plectix.simulator.components.*;
import com.plectix.simulator.components.CObservables.ObservablesConnectedComponent;
import com.plectix.simulator.util.*;

public class TestPositiveUpdate extends UpdateDirectoryTestsRunner {
	private String myTestFileName = "";
	private Map<String, Integer> myObsInjectionsQuantity;
	private Map<String, Integer> myLHSInjectionsQuantity;
	private static final String myPrefixFileName = "test.data/positiveUpdate/";
	private Failer myFailer = new Failer(); 
		
	@Parameters
	public static Collection<Object[]> regExValues() {
		return UpdateDirectoryTestsRunner.getAllTestFileNames(myPrefixFileName);
	}

	public TestPositiveUpdate(String filePath) {
		super(filePath);
		myTestFileName = filePath;
		myFailer.loadTestFile(myTestFileName);
	}

	@Test
	// we can only check for quantity of injections, meaning correct injections setting
	public void testObs() {
		SortedSet<Long> solutionLinkingForCurrentObs = new TreeSet<Long>();

		for (ObservablesConnectedComponent cc : getObservables()) {
			for (CInjection injection : cc.getInjectionsList()) {
				for (CAgentLink agentLink : injection.getAgentLinkList()) {
					solutionLinkingForCurrentObs.add(agentLink.getAgentTo()
							.getId());
				}
			}
		}
		
		myFailer.assertSizeEquality("Observatory injections", solutionLinkingForCurrentObs,
				myObsInjectionsQuantity.get(myTestFileName));
	}

	@Test
	// the same way, we're checking common injections quantity for all the cc from lhs
	public void testLHS() {
		List<CConnectedComponent> leftHand = getActiveRule().getLeftHandSide();
		for (CConnectedComponent cc : leftHand) {
			List<CInjection> componentInjections = cc.getInjectionsList();
			if (!RunUpdateTests.lhsIsEmpty(leftHand)) {
				myFailer.assertSizeEquality("LHS injections", componentInjections,
						myLHSInjectionsQuantity.get(myTestFileName));
			} else {
				myFailer.assertTrue("LHS injections", (componentInjections.size() == 1)
						&& (componentInjections.contains(CConnectedComponent.EMPTY_INJECTION)));
			}
		}
	}
	
	public boolean isDoingPositive() {
		return true;
	}
	
	public String getPrefixFileName() {
		return myPrefixFileName;
	}
	
	public void init() {
		myObsInjectionsQuantity = (new InjectionsQuantityDataParser (myPrefixFileName 
				+ "ObsInjectionsData")).parse(); 
		myLHSInjectionsQuantity = (new InjectionsQuantityDataParser (myPrefixFileName 
				+ "LHSInjectionsData")).parse();
	}
}
