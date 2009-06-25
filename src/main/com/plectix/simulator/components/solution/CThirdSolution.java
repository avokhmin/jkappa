package com.plectix.simulator.components.solution;

import java.util.*;

import com.plectix.simulator.components.CAgent;
import com.plectix.simulator.components.injections.CInjection;
import com.plectix.simulator.interfaces.IConnectedComponent;
import com.plectix.simulator.simulator.KappaSystem;

public class CThirdSolution extends CAbstractSuperSolution {
	private final SuperStorage mySuperStorage;
	private final StraightStorage myStraightStorage;
	
	public CThirdSolution(KappaSystem system) {
		super(system);
		mySuperStorage = getSuperStorage();
		myStraightStorage = getStraightStorage();
	}

	private final void addConnectedComponent(IConnectedComponent component) {
		if (!mySuperStorage.tryIncrement(component)) { 
			myStraightStorage.addConnectedComponent(component);
		}
	}

	@Override
	public final void applyChanges(RuleApplicationPool pool) {
		// TODO Auto-generated method stub
		Collection<CAgent> agents = pool.getStorage().getAgents();
		if (!agents.isEmpty()) {
			Set<CAgent> connectedComponents = new HashSet<CAgent>();
			List<IConnectedComponent> list = new ArrayList<IConnectedComponent>();
			connectedComponents.addAll(agents);
			for (CAgent agent : agents) {
				IConnectedComponent component = SolutionUtils.getConnectedComponent(agent);
				list.add(component);
				for (CAgent agentFromComponent : component.getAgents()) {
					connectedComponents.remove(agentFromComponent);
				}
			}
			for (IConnectedComponent cc : list) {
				this.addConnectedComponent(cc);
			}
		}
		
		pool.clear();
	}
	
	@Override
	public final RuleApplicationPool prepareRuleApplicationPool() {
		return new StandardRuleApplicationPool(new StraightStorage());
	}
	
	@Override
	public final void addInjectionToPool(RuleApplicationPool pool, CInjection injection) {
		StraightStorage storage = pool.getStorage();
		if (injection.isSuper()) {
			storage.addConnectedComponent(mySuperStorage.extractComponent(injection));
		} else {
			if (injection.getImageAgent() != null) {
				IConnectedComponent component = SolutionUtils.getConnectedComponent(injection.getImageAgent());
				for (CAgent agent : component.getAgents()) {
					storage.addAgent(agent);
					// if 'agent' is from straight storage, then it should be removed from there
					myStraightStorage.removeAgent(agent);
				}	
			}
		}
	}
}
