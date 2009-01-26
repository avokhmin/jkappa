package com.plectix.simulator.parser.abstractmodel.reader;

import com.plectix.simulator.parser.KappaFileLine;
import com.plectix.simulator.parser.KappaFileParagraph;
import com.plectix.simulator.parser.ParseErrorException;
import com.plectix.simulator.parser.abstractmodel.*;
import com.plectix.simulator.parser.util.AgentFactory;
import com.plectix.simulator.simulator.SimulationArguments;
import com.plectix.simulator.simulator.SimulationUtils;

/*package*/class ObservablesParagraphReader extends
		KappaParagraphReader<AbstractObservables> {

	private final AgentFactory myAgentFactory;
	private final KappaModel myModel;

	public ObservablesParagraphReader(KappaModel model, SimulationArguments arguments) {
		super(model, arguments);
		myModel = model;
		myAgentFactory = new AgentFactory(getModel().getAgentIdGenerator());
	}

	public final AbstractObservables addComponent(KappaFileParagraph observablesParagraph)
			throws ParseErrorException {
		AbstractObservables observables = new AbstractObservables();
		int obsNameID = 0;

		for (KappaFileLine itemDS : observablesParagraph.getLines()) {
			String line = itemDS.getLine().trim();
			try {
				String name = null;
				if (line.indexOf("'") != -1) {
					line = line.substring(line.indexOf("'") + 1);
					int index = line.indexOf("'");
					if (index != -1) {
						name = line.substring(0, index).trim();
						line = line.substring(index + 1, line.length()).trim();
					}
				}

				if (line.length() == 0) {
					observables.addRulesName(name, obsNameID,
							myModel.getRules());
				} else
					observables.addConnectedComponents(
							SimulationUtils
									.buildConnectedComponents(myAgentFactory
											.parseAgent(line)), name, line,
							obsNameID);
				obsNameID++;
			} catch (ParseErrorException e) {
				e.setLineDescription(itemDS);
				throw e;
			}
		}
		return observables;
	}
}