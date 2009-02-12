package com.plectix.simulator.simulator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.plectix.simulator.components.CProbabilityCalculation;
import com.plectix.simulator.components.stories.CNetworkNotation;
import com.plectix.simulator.components.stories.CStories;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.controller.SimulatorInterface;
import com.plectix.simulator.controller.SimulatorResultsData;
import com.plectix.simulator.controller.SimulatorStatusInterface;
import com.plectix.simulator.interfaces.IInjection;
import com.plectix.simulator.interfaces.IRule;
import com.plectix.simulator.util.PlxTimer;
import com.plectix.simulator.util.RunningMetric;
import com.plectix.simulator.util.Info.InfoType;

public class Simulator implements SimulatorInterface {

	private static final String NAME = "Java Simulator JSIM";
	
	private static final Logger LOGGER = Logger.getLogger(Simulator.class);

	private double currentTime = 0.0;

	private boolean isIteration = false;

	private int timeStepCounter = 0;
	
	private SimulationData simulationData = new SimulationData();

	private SimulatorResultsData simulatorResultsData = new SimulatorResultsData();
	

	public Simulator() {
		super();
	}

	public SimulatorStatusInterface getStatus() {
		// TODO Need to implement this class for streaming data...
		return null;
	}
	
	private final void addIteration(int iteration_num) {
		// TODO: This method should be rewritten!!!
		List<List<RunningMetric>> runningMetrics = simulationData.getRunningMetrics();
		int number_of_observables = simulationData.getKappaSystem().getObservables().getComponentListForXMLOutput().size();

		if (iteration_num == 0) {
			simulationData.getTimeStamps().add(currentTime);

			for (int observable_num = 0; observable_num < number_of_observables; observable_num++) {
				runningMetrics.get(observable_num).add(new RunningMetric());
			}
		}

		for (int observable_num = 0; observable_num < number_of_observables; observable_num++) {
			// x is the value for the observable_num at the current time
			double x = simulationData.getKappaSystem().getObservables()
					.getComponentListForXMLOutput().get(observable_num)
					.getSize(simulationData.getKappaSystem().getObservables());
			if (timeStepCounter >= runningMetrics.get(observable_num).size()) {
				runningMetrics.get(observable_num).add(new RunningMetric());
			}
			runningMetrics.get(observable_num).get(timeStepCounter).add(x);
		}

		timeStepCounter++;
	}

	private final Source addCompleteSource() throws TransformerException, ParserConfigurationException {
		Source source = simulationData.createDOMModel();
		simulatorResultsData.addResultSource(source);
		return source;
	}
	
	private final void endOfMerge(PlxTimer timer){
		simulationData.stopTimer(InfoType.OUTPUT,timer, "-Merge stories:");
	}


	private final void endOfSimulation(InfoType outputType,boolean isEndRules, PlxTimer timer) {
		if(!simulationData.getSimulationArguments().isShortConsoleOutput())
			outputType = InfoType.OUTPUT;
		simulationData.stopTimer(outputType,timer, "-Simulation:");

		switch (outputType) {
		case OUTPUT:
			if (!isEndRules) {
				LOGGER.info("end of simulation: time");
			} else {
				LOGGER.info("end of simulation: there are no active rules");
			}
			break;
		}
	}

	public final void resetSimulation(InfoType outputType) {
		currentTime = 0.0;
		simulationData.resetSimulation(outputType);
	}

	public final void run(int iteration_num) throws Exception {
		simulationData.addInfo(InfoType.OUTPUT,InfoType.INFO, "-Simulation...");
		
		PlxTimer timer = new PlxTimer();
		timer.startTimer();
		
		CProbabilityCalculation ruleProbabilityCalculation = new CProbabilityCalculation(InfoType.OUTPUT,simulationData);

		long clash = 0;
		long count = 0;
		long max_clash = 0;
		boolean isEndRules = false;
		
		while (!simulationData.isEndSimulation(currentTime, count)
				&& max_clash <= simulationData.getSimulationArguments().getMaxClashes()) {
			while (simulationData.checkSnapshots(currentTime)) {
				simulationData.createSnapshots(currentTime);				
			}
			
			simulationData.checkPerturbation(currentTime);
			IRule rule = ruleProbabilityCalculation.getRandomRule();

			if (rule == null) {
				isEndRules = true;
				simulationData.setTimeLength(currentTime);
				simulationData.println("#");
				break;
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Rule: " + rule.getName());
			}

			List<IInjection> injectionsList = ruleProbabilityCalculation.getSomeInjectionList(rule);
			if (!rule.isInfinityRate()) {
				currentTime += ruleProbabilityCalculation.getTimeValue();
			}

			if (!rule.isClash(injectionsList)) {
				// negative update
				max_clash = 0;
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("negative update");

				count++;
				rule.applyRule(injectionsList, simulationData);

				SimulationUtils.doNegativeUpdate(injectionsList);
				
				// positive update
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("positive update");
				}

				simulationData.getKappaSystem().doPositiveUpdate(rule, injectionsList);

				simulationData.getKappaSystem().getObservables().calculateObs(currentTime, count, simulationData.getSimulationArguments().isTime());
			} else {
				simulationData.addInfo(InfoType.NOT_OUTPUT,InfoType.INTERNAL, "Application of rule exp is clashing");
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Clash");
				}
				clash++;
				max_clash++;
			}

			if (isIteration) {
				addIteration(iteration_num);
			}
		}
		
		simulationData.checkOutputFinalState(currentTime);
		simulationData.getKappaSystem().getObservables().calculateObsLast(currentTime);
		simulationData.setTimeLength(currentTime);
		simulationData.setEvent(count);
		
		endOfSimulation(InfoType.OUTPUT,isEndRules, timer);
		Source source = addCompleteSource();
		
		if (!isIteration) {
			simulationData.outputData(source, count);
		}
	}

	public final void run(SimulatorInputData simulatorInputData) throws Exception {
		PlxTimer timer = new PlxTimer();
		timer.startTimer();

		simulationData.setPrintStream(simulatorInputData.getPrintStream());
		simulationData.setSimulationArguments(InfoType.OUTPUT,simulatorInputData.getSimulationArguments());
		simulationData.readSimulatonFile(InfoType.OUTPUT);
		simulationData.getKappaSystem().initialize(InfoType.OUTPUT);
		
		simulationData.stopTimer(InfoType.OUTPUT,timer, "-Initialization:");
		simulationData.setClockStamp(System.currentTimeMillis());
		
		if (simulationData.getSimulationArguments().isCompile()) {
			simulationData.outputData();
			return;
		}
		
		if (!simulationData.getSimulationArguments().isDebugInit()) {
			if (simulationData.getSimulationArguments().isGenereteMap() || simulationData.getSimulationArguments().isContactMap() ) {
				Source source = addCompleteSource();
				simulationData.outputData(source, 0);
			} else if (simulationData.getSimulationArguments().isNumberOfRuns()) {
				runIterations();
			} else if (simulationData.getSimulationArguments().isStorify()) {
				runStories();
			} else {
				run(0);
			}
		}
		
		simulationData.println("-------" + simulatorResultsData.getResultSource());
	}

	public final void runIterations() throws Exception {
		isIteration = true;
		int seed = simulationData.getSimulationArguments().getSeed();
		List<Double> timeStamps = new ArrayList<Double>();
		List<List<RunningMetric>> runningMetrics = new ArrayList<List<RunningMetric>>();
		simulationData.initIterations(timeStamps, runningMetrics);
		
		for (int iteration_num = 0; iteration_num < simulationData.getSimulationArguments().getIterations(); iteration_num++) {
			// Initialize the Random Number Generator with seed = initialSeed +
			// i
			// We also need a new command line argument to feed the initialSeed.
			// See --seed argument of simplx.
			simulationData.getSimulationArguments().setSeed(seed + iteration_num);

			// run the simulator
			timeStepCounter = 0;
			// while (true) {
			// run the simulation until the next time to dump the results
			run(iteration_num);
			// }

			// if the simulator's initial state is cached, reload it for next
			// run
			if (iteration_num < simulationData.getSimulationArguments().getIterations() - 1) {
				resetSimulation(InfoType.OUTPUT);
			}

		}

		// we are done. report the results
		simulationData.createTMPReport();
		// Source source = addCompleteSource();
		// outputData(source, 0);
	}

	public final void runStories() throws Exception {
		CStories stories = simulationData.getKappaSystem().getStories();
		int count = 0;
		if(simulationData.getSimulationArguments().isShortConsoleOutput())
			simulationData.addInfo(InfoType.OUTPUT,InfoType.INFO, "-Simulation...");
		simulationData.resetBar();
		PlxTimer timerAllStories = new PlxTimer();
		timerAllStories.startTimer();
		for (int i = 0; i < simulationData.getSimulationArguments().getIterations(); i++) {
			PlxTimer timer=null;
			if(!simulationData.getSimulationArguments().isShortConsoleOutput()){
				simulationData.addInfo(InfoType.OUTPUT,InfoType.INFO, "-Simulation...");
				timer = new PlxTimer();
				timer.startTimer();
			}
			
			boolean isEndRules = false;
			long clash = 0;
			long max_clash = 0;
			CProbabilityCalculation ruleProbabilityCalculation = new CProbabilityCalculation(InfoType.NOT_OUTPUT,simulationData);
		    
		    while (!simulationData.isEndSimulation(currentTime, count)
					&& max_clash <= simulationData.getSimulationArguments().getMaxClashes()) {
				
				simulationData.checkPerturbation(currentTime);
				IRule rule = ruleProbabilityCalculation.getRandomRule();

				if (rule == null) {
					simulationData.setTimeLength(currentTime);
					simulationData.printlnBar();
					break;
				}

				List<IInjection> injectionsList = ruleProbabilityCalculation.getSomeInjectionList(rule);
				
				if (!rule.isInfinityRate()) {
					currentTime += ruleProbabilityCalculation.getTimeValue();
				}
				if (!rule.isClash(injectionsList)) {
					CNetworkNotation netNotation = new CNetworkNotation(this,count, rule, injectionsList, simulationData);
					max_clash = 0;
					if (stories.checkRule(rule.getRuleID(), i)) {
						rule.applyLastRuleForStories(injectionsList,netNotation);
						rule.applyRuleForStories(injectionsList, netNotation, simulationData, true);
						stories.addToNetworkNotationStoryStorifyRule(i, netNotation, currentTime);
						count++;
						isEndRules = true;
						simulationData.printlnBar();
						break;
					}
					
					rule.applyRuleForStories(injectionsList, netNotation, simulationData, false);
					netNotation.fillAddedAgentsID(simulationData);
					if (!rule.isRHSEqualsLHS()) {
						stories.addToNetworkNotationStory(i, netNotation);
					}
					count++;

					SimulationUtils.doNegativeUpdate(injectionsList);
					simulationData.getKappaSystem().doPositiveUpdate(rule, injectionsList);
				} else {
					clash++;
					max_clash++;
				}
			}
			simulationData.checkStoriesBar(i);
			count = 0;
			stories.handling(i);
			if(!simulationData.getSimulationArguments().isShortConsoleOutput())
				endOfSimulation(InfoType.OUTPUT,isEndRules, timer);
			
				
			if (i < simulationData.getSimulationArguments().getIterations() - 1) {
				resetSimulation(InfoType.NOT_OUTPUT);
			}
		}
		if(simulationData.getSimulationArguments().isShortConsoleOutput()){
			simulationData.println("#");
			endOfSimulation(InfoType.OUTPUT, false, timerAllStories);
		}
		PlxTimer mergeTimer= new PlxTimer();
		mergeTimer.startTimer();
		stories.merge();
		endOfMerge(mergeTimer);
		Source source = addCompleteSource();
		simulationData.outputData(source, count);
	}
	
	//////////////////////////////////////////////////////////////////////////
	//
	//                    GETTERS AND SETTERS
	//
	//

	public final double getCurrentTime() {
		return currentTime;
	}

	public final String getName() {
		return NAME;
	}

	public final SimulationData getSimulationData() {
		return simulationData;
	}

	public final SimulatorResultsData getSimulatorResultsData() {
		return simulatorResultsData;
	}


}
