package com.plectix.simulator.simulationclasses.action;

import java.util.ArrayList;
import java.util.List;

import com.plectix.simulator.interfaces.ConnectedComponentInterface;
import com.plectix.simulator.simulationclasses.injections.Injection;
import com.plectix.simulator.simulationclasses.solution.RuleApplicationPoolInterface;
import com.plectix.simulator.simulator.SimulationData;
import com.plectix.simulator.staticanalysis.Agent;
import com.plectix.simulator.staticanalysis.Rule;
import com.plectix.simulator.staticanalysis.Site;
import com.plectix.simulator.staticanalysis.stories.storage.Event;

/**
 * Class implements "BOUND" action type.
 * 
 * @author avokhmin
 * @see ActionType
 */
public class BoundAction extends Action {
	/**
	 * Constructor of CBoundAction.<br>
	 * <br>
	 * Example:<br>
	 * <code>A(x)->A(x!1),B(y!1)</code>, creates 2 <code>BOUND</code> actions
	 * and <code>ADD</code> action.<br>
	 * <li>relative to site "x" from agent "A":<br>
	 * <code>siteFrom</code> - site "x" from agent "A" from right handSide.<br>
	 * <code>siteTo</code> - site "y" from agent "B" from right handSide.<br>
	 * <code>ccL</code> - connected component "A(x)" from left handSide.<br>
	 * <code>ccR</code> - connected component "A(x!1),B(y!1)" from right
	 * handSide.<br>
	 * <code>rule</code> - rule "A(x)->A(x!1),B(y!1)".<br>
	 * </li> <br>
	 * <li>relative to site "y" from agent "B":<br>
	 * <code>siteFrom</code> - site "y" from agent "B" from right handSide.<br>
	 * <code>siteTo</code> - site "x" from agent "A" from right handSide.<br>
	 * <code>ccL</code> - connected component "NULL" from left handSide.<br>
	 * <code>ccR</code> - connected component "A(x!1),B(y!1)" from right
	 * handSide.<br>
	 * <code>rule</code> - this rule "A(x)->A(x!1),B(y!1)".<br>
	 * </li>
	 * 
	 * @param rule
	 *            given rule
	 * @param sourceSite
	 *            given site from right handSide
	 * @param targetSite
	 *            given site from right handSide
	 * @param leftHandSideComponent
	 *            given connected component from left handSide (may be null)
	 * @param rightHandSideComponent
	 *            given connected component from right handSide
	 */
	public BoundAction(Rule rule, Site sourceSite, Site targetSite,
			ConnectedComponentInterface leftHandSideComponent,
			ConnectedComponentInterface rightHandSideComponent) {
		super(rule, null, null, leftHandSideComponent, rightHandSideComponent);
		setActionApplicationSites(sourceSite, targetSite);
		setType(ActionType.BOUND);
	}

	@Override
	public final void doAction(RuleApplicationPoolInterface pool,
			Injection injection, ActionObserverInteface event,
			SimulationData simulationData) {
		// TODO copypaste detected =(
		Agent agentFromInSolution;
		Site boundingTargetSite = this.getTargetSite();
		Site boundingSourceSite = this.getSourceSite();
		Rule parentRule = this.getRule();
		
		if (boundingSourceSite.getParentAgent().getIdInRuleHandside() > getAgentsFromConnectedComponent(
				parentRule.getLeftHandSide()).size()) {
			agentFromInSolution = parentRule.getAgentAdd(boundingSourceSite
					.getParentAgent());
		} else {
			int agentIdInCC = getAgentIdInCCBySideId(boundingSourceSite
					.getParentAgent());

			agentFromInSolution = injection.getAgentFromImageById(agentIdInCC);

			Site injectedSite = agentFromInSolution
					.getSiteByName(boundingSourceSite.getName());
			injection.addToChangedSites(injectedSite);

		}

		Agent agentToInSolution;
		if (boundingTargetSite.getParentAgent().getIdInRuleHandside() > getAgentsFromConnectedComponent(
				parentRule.getLeftHandSide()).size()) {
			agentToInSolution = parentRule.getAgentAdd(boundingTargetSite
					.getParentAgent());
		} else {
			int agentIdInCC = getAgentIdInCCBySideId(boundingTargetSite
					.getParentAgent());
			Injection inj = parentRule
					.getInjectionBySiteToFromLHS(boundingTargetSite);
			agentToInSolution = inj.getAgentFromImageById(agentIdInCC);
		}

		
		event.registerAgent(agentFromInSolution);
		event.registerAgent(agentToInSolution);
		
		
		agentFromInSolution.getSiteByName(boundingSourceSite.getName())
				.getLinkState().connectSite(
						agentToInSolution.getSiteByName(boundingTargetSite
								.getName()));

		agentToInSolution.getSiteByName(boundingTargetSite.getName())
				.getLinkState().connectSite(
						agentFromInSolution.getSiteByName(boundingSourceSite
								.getName()));
		event.boundAddToEventContainer(agentFromInSolution
				.getSiteByName(boundingSourceSite.getName()),
				Event.AFTER_STATE);
		event.boundAddToEventContainer(agentToInSolution
				.getSiteByName(boundingTargetSite.getName()),
				Event.AFTER_STATE);

		agentFromInSolution.getSiteByName(boundingSourceSite.getName())
				.setLinkIndex(boundingSourceSite.getLinkIndex());
		agentToInSolution.getSiteByName(boundingTargetSite.getName())
				.setLinkIndex(boundingTargetSite.getLinkIndex());

	}

	/**
	 * This methods extracts all agents from a given connected components
	 * 
	 * @param components
	 *            list of connected components
	 * @return list of agents, included in ccList
	 */
	private static final List<Agent> getAgentsFromConnectedComponent(
			List<ConnectedComponentInterface> components) {
		List<Agent> agentList = new ArrayList<Agent>();
		if (components.get(0).getAgents().get(0).getIdInRuleHandside() == Agent.UNMARKED)
			return agentList;
		for (ConnectedComponentInterface cc : components) {
			for (Agent agent : cc.getAgents())
				agentList.add(agent);
		}

		return agentList;
	}


}
