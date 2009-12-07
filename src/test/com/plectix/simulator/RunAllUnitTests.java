package com.plectix.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.plectix.simulator.injections.RunInjectionsTests;
import com.plectix.simulator.parser.incomplete.TestIncompleteSubstance;
import com.plectix.simulator.simulationclasses.probability.TestWeightedItemSelector;
import com.plectix.simulator.staticanalysis.graphs.GraphsTest;
import com.plectix.simulator.staticanalysis.rulecompression.TestsRuleCompressions;

@RunWith(value = Suite.class)
@SuiteClasses(value = { 
		TestsRuleCompressions.class,
		GraphsTest.class,
		RunInjectionsTests.class,
		TestWeightedItemSelector.class,
		TestIncompleteSubstance.class
		})


public class RunAllUnitTests {

}