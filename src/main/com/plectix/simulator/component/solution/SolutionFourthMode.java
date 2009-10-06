package com.plectix.simulator.component.solution;

import com.plectix.simulator.component.Agent;
import com.plectix.simulator.component.injections.Injection;
import com.plectix.simulator.interfaces.ConnectedComponentInterface;
import com.plectix.simulator.simulator.KappaSystem;

/*package*/ final class SolutionFourthMode extends AbstractSolutionForHigherModes {
	private final SuperStorage superStorage;
	
	SolutionFourthMode(KappaSystem system) {
		super(system);
		superStorage = getSuperStorage();
	}

	@Override
	protected final void addConnectedComponent(ConnectedComponentInterface component) {
		superStorage.addConnectedComponent(component);
	}

	@Override
	public final RuleApplicationPoolInterface prepareRuleApplicationPool() {
		return new StandardRuleApplicationPool();
	}
	
	@Override
	public final void addInjectionToPool(RuleApplicationPoolInterface pool, Injection injection) {
		StraightStorage storage = pool.getStorage();
		if (injection.isSuper()) {
			storage.addConnectedComponent(superStorage.extractComponent(injection));
		} else {
			if (injection.getImageAgent() != null) {
				ConnectedComponentInterface component = SolutionUtils.getConnectedComponent(injection.getImageAgent());
				for (Agent agent : component.getAgents()) {
					storage.addAgent(agent);
				}	
			}
		}
	}
}
