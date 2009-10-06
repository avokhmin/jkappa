package com.plectix.simulator.component.solution;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.plectix.simulator.component.Agent;
import com.plectix.simulator.component.injections.Injection;
import com.plectix.simulator.interfaces.ConnectedComponentInterface;
import com.plectix.simulator.simulator.KappaSystem;

@SuppressWarnings("serial")
/*package*/ final class SolutionFirstMode extends AbstractComplexSolution implements Serializable {
	private final StraightStorage straightStorage;
	
	// we instantiate this type through UniversalSolution only
	SolutionFirstMode(KappaSystem system) {
		super(system);
		straightStorage = this.getStraightStorage();
	}

	//-----------------GETTERS----------------------------------
	
	@Override
	public final Collection<ConnectedComponentInterface> split() {
		return straightStorage.split();
	}

	//----------------RULE APPLICATION---------------------------

	@Override
	public RuleApplicationPoolInterface prepareRuleApplicationPool() {
		return new TransparentRuleApplicationPool(this);
	}

	/**
	 * This one does nothing, because we use transparent pool to apply any rule here,
	 * so all the changes are applied already
	 */
	@Override
	public final void flushPoolContent(RuleApplicationPoolInterface pool) {
	}

	@Override
	public final void addInitialConnectedComponents(long quantity, List<Agent> components) {
		for (Agent agent : components) {
			straightStorage.addAgent(agent);
		}
		for (int i = 1; i < quantity; i++) {
			for (Agent agent : this.cloneAgentsList(components)) {
				straightStorage.addAgent(agent);
			}
		}
	}
	
	/**
	 * This one does nothing, because we use transparent pool to apply any rule here,
	 * so all the changes are applied already
	 */
	@Override
	public final void addInjectionToPool(RuleApplicationPoolInterface prepareRuleApplicationPool, Injection injection) {
		// it should be empty
	}
}
