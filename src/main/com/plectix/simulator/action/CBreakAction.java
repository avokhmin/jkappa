package com.plectix.simulator.action;

import com.plectix.simulator.components.CLinkStatus;
import com.plectix.simulator.components.CRule;
import com.plectix.simulator.components.injections.CInjection;
import com.plectix.simulator.components.solution.RuleApplicationPool;
import com.plectix.simulator.components.stories.CNetworkNotation.NetworkNotationMode;
import com.plectix.simulator.components.stories.CStoriesSiteStates.StateType;
import com.plectix.simulator.components.CAgent;
import com.plectix.simulator.interfaces.IConnectedComponent;

import com.plectix.simulator.interfaces.INetworkNotation;
import com.plectix.simulator.components.CSite;
import com.plectix.simulator.simulator.SimulationData;

/**
 * Class implements "BREAK" action type.
 * @author avokhmin
 * @see CActionType
 */
@SuppressWarnings("serial")
public class CBreakAction extends CAction {
	private final CSite mySiteFrom;
	private final CSite mySiteTo;
	private final CRule myRule;

	/**
	 * Constructor of CBreakAction.<br>
	 * <br>
	 * Example:<br>
	 * <code>A(x!1),B(y!1)->A(x),B(y)</code>, creates 2 <code>BREAK</code> actions.<br> 
	 * <li>relative to site "x" from agent "A":<br>
	 * <code>siteFrom</code> - site "x" from agent "A" from left handSide.<br>
	 * <code>siteTo</code> - site "x" from agent "A" from right handSide.<br>
	 * <code>ccL</code> - connected component "A(x!1),B(y!1)" from left handSide.<br>
	 * <code>ccR</code> - connected component "A(x)" from right handSide.<br>
	 * <code>rule</code> - rule "A(x!1),B(y!1)->A(x),B(y)".<br>
	 * </li>
	 * 
	 * <li>relative to site "y" from agent "B":<br>
	 * similarly.
	 * </li>
	 * 
	 * @param rule  given rule
	 * @param siteFrom given site from left handSide
	 * @param siteTo given site from right handSide
	 * @param ccL given connected component from left handSide
	 * @param ccR given connected component from right handSide
	 */
	public CBreakAction(CRule rule, CSite siteFrom, CSite siteTo,
			IConnectedComponent ccL, IConnectedComponent ccR) {
		super(rule, null, null, ccL, ccR);
		myRule = rule;
		mySiteFrom = siteFrom;
		mySiteTo = siteTo;
		setActionApplicationSites(mySiteFrom, mySiteTo);
		setType(CActionType.BREAK);
	}

	public final void doAction(RuleApplicationPool pool, CInjection injection, 
			INetworkNotation netNotation, SimulationData simulationData) {
		CAgent agentFromInSolution;
		int agentIdInCC = getAgentIdInCCBySideId(mySiteFrom.getAgentLink());
		agentFromInSolution = injection.getAgentFromImageById(agentIdInCC);

		CSite injectedSite = agentFromInSolution.getSiteById(mySiteFrom.getNameId());

		addToNetworkNotation(StateType.BEFORE, netNotation,
				injectedSite);
		addRuleSitesToNetworkNotation(true, netNotation, injectedSite);

		CSite linkSite = (CSite) injectedSite.getLinkState().getConnectedSite();
		if ((mySiteFrom.getLinkState().getConnectedSite() == null) && (linkSite != null)) {
			addToNetworkNotation(StateType.BEFORE, netNotation,
					linkSite);

			linkSite.getLinkState().connectSite(null);
			linkSite.getLinkState().setStatusLink(CLinkStatus.FREE);
			if (mySiteTo != null) {
//				linkSite.setLinkIndex(mySiteTo.getLinkIndex());
				linkSite.setLinkIndex(-1);
			}
			injection.addToChangedSites(linkSite);
			getRightCComponent().addAgentFromSolutionForRHS(linkSite
					.getAgentLink());
			addToNetworkNotation(StateType.AFTER, netNotation,
					linkSite);

		}

		agentFromInSolution.getSiteById(mySiteFrom.getNameId()).getLinkState()
				.connectSite(null);
		agentFromInSolution.getSiteById(mySiteFrom.getNameId()).getLinkState()
				.setStatusLink(CLinkStatus.FREE);
		// /////////////////////////////////////////////

		injection.addToChangedSites(injectedSite);

		addToNetworkNotation(StateType.AFTER, netNotation,
				injectedSite);
		/**
		 * Break a bond for this rules: A(x!_)->A(x)
		 */
		if (mySiteFrom.getLinkState().getConnectedSite() == null && linkSite != null) {
			addSiteToConnectedWithBroken(linkSite);
			addRuleSitesToNetworkNotation(false, netNotation, linkSite);
		}
		// /////////////////////////////////////////////
		agentFromInSolution.getSiteById(mySiteFrom.getNameId()).
			setLinkIndex(-1);
	}

	/**
	 * Util method. Uses for fill {@link CRule#addSiteConnectedWithBroken(CSite)}.
	 * @param checkedSite given site.
	 */
	private final void addSiteToConnectedWithBroken(CSite checkedSite) {
		for (CSite site : myRule.getSitesConnectedWithBroken()) {
			if (site == checkedSite) {
				return;
			}
		}
		myRule.addSiteConnectedWithBroken(checkedSite);
	}

	protected final void addRuleSitesToNetworkNotation(boolean existInRule,
			INetworkNotation netNotation, CSite site) {
		if (netNotation != null) {
			NetworkNotationMode agentMode = NetworkNotationMode.NONE;
			NetworkNotationMode linkStateMode = NetworkNotationMode.NONE;
			NetworkNotationMode internalStateMode = NetworkNotationMode.NONE;

			if (existInRule) {
				agentMode = NetworkNotationMode.TEST;
				linkStateMode = NetworkNotationMode.TEST_OR_MODIFY;
			} else {
				linkStateMode = NetworkNotationMode.MODIFY;
			}
			netNotation.addToAgentsFromRules(site, agentMode,
					internalStateMode, linkStateMode);
		}
	}

	protected final void addToNetworkNotation(StateType index,
			INetworkNotation netNotation, CSite site) {
		if (netNotation != null) {
			netNotation.checkLinkForNetworkNotation(index, site);
			netNotation.checkLinkToUsedSites(index, site);
		}
	}
}
