package com.plectix.simulator.components.solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.plectix.simulator.components.CAgent;
import com.plectix.simulator.components.CConnectedComponent;
import com.plectix.simulator.components.CInternalState;
import com.plectix.simulator.components.CSite;
import com.plectix.simulator.interfaces.IAgent;
import com.plectix.simulator.interfaces.IConnectedComponent;
import com.plectix.simulator.interfaces.ILinkState;
import com.plectix.simulator.interfaces.ISite;
import com.plectix.simulator.interfaces.ISolution;
import com.plectix.simulator.simulator.KappaSystem;

/*package*/ abstract class SolutionAdapter implements ISolution {
	private List<SolutionLines> solutionLines = new ArrayList<SolutionLines>();
	private final KappaSystem mySystem;

	public SolutionAdapter(KappaSystem system) {
		mySystem = system;
	}
	
	public final void addConnectedComponents(List<IConnectedComponent> list) {
		if (list == null)
			return;
		for (IConnectedComponent component : list) {
			this.addConnectedComponent(component);
		}
	}
	
	//TODO REMOVE
	public final void checkSolutionLinesAndAdd(String line, long count) {
		line = line.replaceAll("[ 	]", "");
		while (line.indexOf("(") == 0) {
			line = line.substring(1);
			line = line.substring(0, line.length() - 1);
		}
		for (SolutionLines sl : solutionLines) {
			if (sl.getLine().equals(line)) {
				sl.setCount(sl.getCount() + count);
				return;
			}
		}
		solutionLines.add(new SolutionLines(line, count));
	}
	
	public final List<SolutionLines> getSolutionLines() {
		return Collections.unmodifiableList(solutionLines);
	}
	
	public final void clearSolutionLines() {
		solutionLines.clear();
	}
	
	//----------------------------CLONE METHODS--------------------------------------
	
	public IConnectedComponent cloneConnectedComponent(IConnectedComponent component) {
		return new CConnectedComponent(cloneAgentsList(component.getAgents()));
	}
	
	public List<IAgent> cloneAgentsList(List<IAgent> agentList) {
		List<IAgent> newAgentsList = new ArrayList<IAgent>();
		for (IAgent agent : agentList) {
			IAgent newAgent = new CAgent(agent.getNameId(), mySystem.generateNextAgentId());
			for (ISite site : agent.getSites()) {
				CSite newSite = new CSite(site.getNameId(), newAgent);
				newSite.setLinkIndex(site.getLinkIndex());
				newSite.setInternalState(new CInternalState(site
						.getInternalState().getNameId()));
				// newSite.getInternalState().setNameId(
				// site.getInternalState().getNameId());
				newAgent.addSite(newSite);
			}
			newAgentsList.add(newAgent);
		}
		for (int i = 0; i < newAgentsList.size(); i++) {
			for (ISite siteNew : newAgentsList.get(i).getSites()) {
				ILinkState lsNew = siteNew.getLinkState();
				ILinkState lsOld = agentList.get(i)
						.getSite(siteNew.getNameId()).getLinkState();
				lsNew.setStatusLink(lsOld.getStatusLink());
				if (lsOld.getSite() != null) {
					CSite siteOldLink = (CSite) lsOld.getSite();
					int j = 0;
					for (j = 0; j < agentList.size(); j++) {
						if (agentList.get(j) == siteOldLink.getAgentLink())
							break;
					}
					int index = j;
					lsNew.setSite(newAgentsList.get(index).getSite(
							siteOldLink.getNameId()));
				}
			}
		}
		return newAgentsList;
	}
	
	public KappaSystem getKappaSystem() {
		return mySystem;
	}

//	public void applyRule(RuleApplicationPool pool) {
////		for (IConnectedComponent component : pool.getComponents()) {
////			addConnectedComponent(component);
////		}
//	}
	
//	public void applyRule(RuleApplicationPool pool) {
//		for (InfoAddAction action : pool.getAddActions()) {
//			addAgent(action.getAgent());
//		}
//		for (InfoDeleteAction action : pool.getDeleteActions()) {
//			removeAgent(action.getAgent());
//		}
//		for (InfoModifyAction action : pool.getModifyActions()) {
//			action.getSite().getInternalState().setNameId(action.getNewInternalStateNameId());
//		}
//		for (InfoBoundAction action : pool.getBoundActions()) {
//			agent(action.getAgent());
//		}
//		for (InfoBreakAction action : pool.getBreakActions()) {
//			agent(action.getAgent());
//		}
//	}
}