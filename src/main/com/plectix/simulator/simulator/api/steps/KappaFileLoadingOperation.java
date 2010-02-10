package com.plectix.simulator.simulator.api.steps;

import java.io.IOException;
import java.util.List;

import com.plectix.simulator.parser.KappaFile;
import com.plectix.simulator.parser.KappaFileReader;
import com.plectix.simulator.parser.SimulationDataFormatException;
import com.plectix.simulator.parser.abstractmodel.reader.RulesParagraphReader;
import com.plectix.simulator.parser.builders.RuleBuilder;
import com.plectix.simulator.parser.util.AgentFactory;
import com.plectix.simulator.simulator.KappaSystem;
import com.plectix.simulator.simulator.SimulationArguments;
import com.plectix.simulator.simulator.SimulationData;
import com.plectix.simulator.simulator.api.OperationType;
import com.plectix.simulator.staticanalysis.Rule;
import com.plectix.simulator.staticanalysis.contactmap.ContactMapMode;

public class KappaFileLoadingOperation extends AbstractOperation<KappaFile> {
	private final SimulationData simulationData;
	private String kappaFileId;
	private char[] charKappaInput;

	public KappaFileLoadingOperation(SimulationData simulationData, String kappaFileId) {
		super(simulationData, OperationType.KAPPA_FILE_LOADING);
		this.simulationData = simulationData;
		this.kappaFileId = kappaFileId;
		simulationData.getSimulationArguments().setInputFilename(kappaFileId);
	}

	public KappaFileLoadingOperation(SimulationData simulationData, char[] inputCharArray) {
		super(simulationData, OperationType.KAPPA_FILE_LOADING);
		this.simulationData = simulationData;
		simulationData.getSimulationArguments().setInputCharArray(inputCharArray);
		charKappaInput = inputCharArray;
	}
	
	protected KappaFile performDry() throws RuntimeException,
			SimulationDataFormatException, IOException {
		SimulationArguments simulationArguments = simulationData.getSimulationArguments();

		if (!simulationData.argumentsInitialized()) {
			throw new RuntimeException(
					"Simulator Arguments must be set before reading the simulation file!");
		}
		KappaSystem kappaSystem = simulationData.getKappaSystem();
		

		// simulationData.getClock().resetBar();
		kappaSystem.getObservables().setOcamlStyleObsName(
				simulationArguments.isOcamlStyleNameingInUse());
		kappaSystem.getObservables().setUnifiedTimeSeriesOutput(
				simulationArguments.isUnifiedTimeSeriesOutput());
		if (simulationArguments.getSnapshotsTimeString() != null) {
			simulationData.setSnapshotTime(simulationArguments
					.getSnapshotsTimeString());
		}

		KappaFileReader kappaFileReader;
		String inputFileName = kappaFileId;
		if (inputFileName != null) {
			kappaFileReader = new KappaFileReader(inputFileName, true);
		} else {
			kappaFileReader = new KappaFileReader(charKappaInput);
		}

		if (simulationArguments.getFocusFilename() != null) {
			this.setFocusOn(simulationArguments.getFocusFilename());
		} else {
			kappaSystem.getContactMap().setMode(ContactMapMode.SEMANTIC);
		}
		KappaFile kappaFile = kappaFileReader.parse();
	
		simulationData.setKappaFile(kappaFile);
			
		return kappaFile;
	}

	private final void setFocusOn(String fileNameFocusOn) throws IOException,
			SimulationDataFormatException {
		KappaSystem kappaSystem = simulationData.getKappaSystem();
		SimulationArguments simulationArguments = simulationData
				.getSimulationArguments();

		KappaFileReader kappaFileReader = new KappaFileReader(fileNameFocusOn,
				true);
		KappaFile kappaFile = kappaFileReader.parse();
		List<Rule> ruleList = (new RuleBuilder(new KappaSystem(simulationData)))
				.build(new RulesParagraphReader(simulationArguments,
						new AgentFactory(false)).readComponent(kappaFile
						.getRules()), null);

		kappaSystem.getContactMap().setSimulationData(kappaSystem);
		if (ruleList != null && !ruleList.isEmpty()) {
			kappaSystem.getContactMap().setFocusRule(ruleList.get(0));
			kappaSystem.getContactMap().setMode(ContactMapMode.FOCUS_ON_AGENT_OR_RULE);
		} else {
			kappaSystem.getContactMap().setFocusRule(null);
		}
	}

	@Override
	protected boolean noNeedToPerform() {
		return simulationData.getKappaInput() != null;
	}

	@Override
	protected KappaFile retrievePreparedResult() {
		return simulationData.getKappaInput();
	}
}
