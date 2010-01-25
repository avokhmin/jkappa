package com.plectix.simulator.simulator.api.steps;

import com.plectix.simulator.simulator.KappaSystem;
import com.plectix.simulator.simulator.api.AbstractOperation;
import com.plectix.simulator.simulator.api.OperationType;
import com.plectix.simulator.simulator.initialization.InjectionsBuilder;

public class InjectionBuildingOperation extends AbstractOperation<KappaSystem> {
	private final KappaSystem kappaSystem;
	
	public InjectionBuildingOperation(KappaSystem kappaSystem) {
		super(kappaSystem.getSimulationData(), OperationType.INJECTIONS_BUILDING);
		this.kappaSystem = kappaSystem;
	}
	
	protected KappaSystem performDry() {
		(new InjectionsBuilder(kappaSystem)).build();
		return kappaSystem;
	}

}
