package com.plectix.simulator.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.plectix.simulator.components.injections.CInjection;
import com.plectix.simulator.components.injections.CLiftElement;
import com.plectix.simulator.components.solution.SuperSubstance;
import com.plectix.simulator.components.CAgent;
import com.plectix.simulator.components.CAgentLink;
import com.plectix.simulator.interfaces.IConnectedComponent;

import com.plectix.simulator.interfaces.IRandom;

import com.plectix.simulator.components.CSite;

/**
 * This class implements "connected component" entity.<br><br>
 * It unites a pack of connected agents. We state that all the agents included in 
 * connected component somehow connected with each other (there is a path consisted by
 * several connections, connecting each pair of agents in fixed connected component).<br><br>
 * 
 * @author avokhmin
 *
 */
public class CConnectedComponent implements IConnectedComponent, Serializable {
	private static final long serialVersionUID = -2233812055480299501L;
	
	/**
	 * "EMPTY" ConnectedComponent, contains no agents.
	 */
	public static CConnectedComponent EMPTY = new CConnectedComponent();

	private final List<CAgent> agentList;
	private Map<Integer, List<CSpanningTree>> spanningTreeMap;
	private List<CAgent> agentFromSolutionForRHS;
	private List<CAgentLink> agentLinkList;
	private final Map<Integer, CInjection> injectionsList;
	private List<CSite> injectedSites;
	private int maxId = -1;
	private CRule rule;
	private SuperSubstance mySubstance = null;

	/**
	 * private empty connected component constructor
	 */
	private CConnectedComponent() {
		agentList = new ArrayList<CAgent>();
		agentList.add(new CAgent());
		injectionsList = new TreeMap<Integer, CInjection>();
		addInjection(CInjection.EMPTY_INJECTION, 0);
		agentFromSolutionForRHS = new ArrayList<CAgent>();
	}

	/**
	 * Main constructor. Creates connected component with given list of connected agents.
	 * @param connectedAgents list of agents, which can be source for the new connected component 
	 */
	public CConnectedComponent(List<CAgent> connectedAgents) {
		agentList = connectedAgents;
		injectionsList = new TreeMap<Integer, CInjection>();
		agentFromSolutionForRHS = new ArrayList<CAgent>();
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}
	
	public void setSuperSubstance(SuperSubstance substance) {
		mySubstance = substance;
	}

	public SuperSubstance getSubstance() {
		return mySubstance;
	}

	/**
	 * This method registers some agents from solution when apply rule to the util collection needed
	 * on positive update.
	 * @param agentFromSolutionForRHS agent to be registered
	 */
	//TODO move?
	public final void addAgentFromSolutionForRHS(CAgent agentFromSolutionForRHS) {
		this.agentFromSolutionForRHS.add(agentFromSolutionForRHS);
	}

	/**
	 * This method clears util collection of solution agents needed
	 * on positive update.
	 */
	//TODO move?
	public final void clearAgentsFromSolutionForRHS() {
		agentFromSolutionForRHS.clear();
	}

	/**
	 * This method returns util collection of agents needed on positive update.
	 * @return util list of agents, needed on positive update
	 */
	//TODO move?
	public final List<CAgent> getAgentFromSolutionForRHS() {
		return Collections.unmodifiableList(agentFromSolutionForRHS);
	}

	/**
	 * This method registers injection to current connected component with given id.
	 * @param inj injection to register
	 * @param id id of injection
	 */
	private final void addInjection(CInjection inj, int id) {
		if (inj != null) {
			maxId = Math.max(maxId, id);
			inj.setId(id);
			injectionsList.put(id, inj);
		}
	}

	/**
	 * This method unregisters given injection from current connected component
	 * @param injection injection to remove
	 */
	public final void removeInjection(CInjection injection) {
		if (injection == null) {
			return;
		}

		int id = injection.getId();

		if (injectionsList.get(id) != null) {
			if (injection != injectionsList.get(id)) {
				return;
			}
			CInjection inj = injectionsList.remove(maxId);
			if (id != maxId) {
				addInjection(inj, id);
			}
			maxId--;
		}
	}

	/**
	 * This method initializes "spanning tree map" structure, which is used for comparing two components
	 * and so on
	 */
	public final void initSpanningTreeMap() {
		CSpanningTree spTree;
		spanningTreeMap = new HashMap<Integer, List<CSpanningTree>>();
		if (agentList.isEmpty())
			return;

		for (CAgent agentAdd : agentList) {
			spTree = new CSpanningTree(agentList.size(), agentAdd);
			List<CSpanningTree> list = spanningTreeMap
					.get(agentAdd.getNameId());
			if (list == null) {
				list = new ArrayList<CSpanningTree>();
				spanningTreeMap.put(agentAdd.getNameId(), list);
			}
			list.add(spTree);
		}
	}

	/**
	 * Sets new injection for this connected component.
	 * @param injection injection to be set
	 */
	public final void setInjection(CInjection injection) {
		addInjection(injection, maxId + 1);
		for (CSite changedSite : injectedSites) {
			changedSite.addToLift(new CLiftElement(this, injection));
		}
	}

	/**
	 * Returns new injection for current ConnectedComponent by input agent.<br>
	 * If injection can't be create, returns "null".
	 * @param agent given agent
	 */
	public final CInjection createInjection(CAgent agent) {
		if (unify(agent)) {
			CInjection injection = new CInjection(this, injectedSites,
					agentLinkList);
			return injection;
		}
		return null;
	}

	/**
	 * This method does positive update for current ConnectedComponent using given list of 
	 * connected components.
	 * @param connectedComponentList list of ConnectedComponents to be used
	 */
	public final void doPositiveUpdate(List<IConnectedComponent> connectedComponentList) {
		if (connectedComponentList == null)
			return;
		for (IConnectedComponent cc : connectedComponentList) {
			for (CAgent agent : cc.getAgentFromSolutionForRHS()) {
				CInjection inj = this.createInjection(agent);
				if (inj != null) {
					if (!agent.hasSimilarInjection(inj))
						setInjection(inj);
				}
			}
		}
	}

	/**
	 * This method returns all agents from current ConnectedComponent
	 * @return list of this component's agents
	 */
	public final List<CAgent> getAgents() {
		return Collections.unmodifiableList(agentList);
	}

	public final boolean unify(CAgent agent) {
		injectedSites = new ArrayList<CSite>();
		agentLinkList = new ArrayList<CAgentLink>();

		if (spanningTreeMap == null)
			return false;

		List<CSpanningTree> spList = spanningTreeMap.get(agent.getNameId());

		if (spList == null) {
			return false;
		}

		for (CSpanningTree tree : spList) {
			injectedSites.clear();
			agentLinkList.clear();
			if (tree != null) {
				tree.resetNewVertex();
				if (agentList.get(tree.getRootIndex()).getSites().isEmpty()) {
					injectedSites.add(agent.getDefaultSite());
					agentLinkList.add(new CAgentLink(0, agent));
					return true;
				} else {
					if (compareAgents(agentList.get(tree.getRootIndex()), agent))
						if (viewSpanningTree(agent, tree,
								tree.getRootIndex(), false))
							if (this.getAgents().size() == agentLinkList.size())
								return true;
							else {
								injectedSites.clear();
								agentLinkList.clear();
							}
				}

			}
		}
		return false;
	}

	public final boolean isAutomorphicTo(CAgent agent) {
		if (spanningTreeMap == null)
			return false;

		List<CSpanningTree> spList = spanningTreeMap.get(agent.getNameId());

		if (spList == null)
			return false;

		for (CSpanningTree tree : spList) {
			if (tree != null) {
				tree.resetNewVertex();
				if (agentList.get(tree.getRootIndex()).getSites().isEmpty()
						&& agent.getSites().isEmpty()) {
					return true;
				} else {
					if (fullEqualityOfAgents(
							agentList.get(tree.getRootIndex()), agent))
						if (viewSpanningTree(agent, tree,
								tree.getRootIndex(), true))
							return true;
				}

			}
		}
		return false;
	}

	/**
	 * This method compares two agents for being completely equal.
	 * @param agentOne first agent
	 * @param agentTwo second agent
	 * @return <tt>true</tt> if these agents are completely equal, otherwise <tt>false</tt>
	 */
	//TODO MOVE??
	private final boolean fullEqualityOfAgents(CAgent agentOne, CAgent agentTwo) {
		if (agentOne == null || agentTwo == null)
			return false;
		if (agentOne.getSites().size() != agentTwo.getSites().size())
			return false;

		for (CSite cc1Site : agentOne.getSites()) {
			CSite cc2Site = agentTwo.getSiteById(cc1Site.getNameId());
			if (cc2Site == null)
				return false;
			if (!cc1Site.expandedEqualz(cc2Site, true))
				return false;
		}
		return true;
	}

	/**
	 * Util method for partial agents comparison
	 * @param currentAgent first agent
	 * @param solutionAgent second agent
	 * @return <tt>true</tt> if these agents are partially equal, otherwise <tt>false</tt>
	 */
	private final boolean compareAgents(CAgent currentAgent, CAgent solutionAgent) {
		if (currentAgent == null || solutionAgent == null)
			return false;
		for (CSite site : currentAgent.getSites()) {
			CSite solutionSite = solutionAgent.getSiteById(site.getNameId());
			if (solutionSite == null)
				return false;
			if (!site.expandedEqualz(solutionSite, false))
				return false;
			injectedSites.add(solutionSite);
		}
		agentLinkList.add(new CAgentLink(currentAgent
				.getIdInConnectedComponent(), solutionAgent));
		return true;
	}

	/**
	 * Util method, uses in {@link #viewSpanningTree(CAgent, CSpanningTree, int, boolean)}.
	 * @param agentFrom given agent
	 * @param agentTo given agent
	 * @return Returns list of connect sites between given agents
	 */
	private final List<CSite> getConnectedSite(CAgent agentFrom, CAgent agentTo) {
		List<CSite> siteList = new ArrayList<CSite>();

		for (CSite sF : agentFrom.getSites()) {
			for (CSite sT : agentTo.getSites()) {
				if (sF == sT.getLinkState().getConnectedSite()) {
					siteList.add(sF);
				}
			}
		}
		return siteList;
	}

	/**
	 * Util method.
	 */
	private final boolean viewSpanningTree(CAgent agent,
			CSpanningTree spTree, int rootVertex, boolean fullEquality) {
		spTree.setTrue(rootVertex);
		for (Integer v : spTree.getVertexes()[rootVertex]) {
			CAgent cAgent = agentList.get(v);// get next agent from spanning
			if (!(spTree
					.getNewVertexElement(cAgent.getIdInConnectedComponent()))) {
				List<CSite> sitesFrom = getConnectedSite(agentList
						.get(rootVertex), agentList.get(v));
				CAgent sAgent = agent.findLinkAgent(cAgent, sitesFrom);
				if (fullEquality && !(fullEqualityOfAgents(cAgent, sAgent)))
					return false;
				if (!fullEquality && !compareAgents(cAgent, sAgent))
					return false;
				viewSpanningTree(sAgent, spTree, v, fullEquality);
			}
		}
		return true;
	}

	/**
	 * This method returns rule containing this connected component (if there is one)
	 * @return rule containing this connected component, or <tt>null</tt> if there isn't such
	 */
	public final CRule getRule() {
		return rule;
	}

	/**
	 * This method sets rule containing this connected component.
	 * @param rule new value
	 */
	public final void setRule(CRule rule) {
		this.rule = rule;
	}

	/**
	 * This method returns all injections from current connected components.
	 * @return list of injections from this connected component
	 */
	public final Collection<CInjection> getInjectionsList() {
		return Collections.unmodifiableCollection(injectionsList.values());
	}

	/**
	 * This method returns random injection from current connected component
	 * @param random random number generator
	 * @return random injection from current connected component
	 */
	public final CInjection getRandomInjection(IRandom random) {
		int index;
		CInjection inj = null;
		index = random.getInteger(maxId + 1);
		inj = injectionsList.get(index);
		return inj;
	}

	/**
	 * This method returns first injection from current ConnectedComponent.
	 * Used only for checking clashes in case of infinite rated rules
	 * @return first injection from list of injections
	 */
	public final CInjection getFirstInjection() {
		return injectionsList.get(0);
	}

	/**
	 * This method sorts agents from current connected component by id in rule handside.
	 * @return sorted collection of component's agents 
	 */
	public final List<CAgent> getAgentsSortedByIdInRule() {
		List<CAgent> temp = new ArrayList<CAgent>();
		temp.addAll(agentList);
		Collections.sort(temp);
		return temp;
	}

	// -----------------------hash, toString, equals-----------------------------

//	/**
//	 * This methods takes agentNames in alphabetical order as a String, then
//	 * allsiteNames in this order as String too and then concatenates these
//	 * Strings.
//	 */
//	//TODO IMPLEMENT
//	public String getHash() {
//		StringBuffer sb = new StringBuffer();
////		List<String> siteNames = new ArrayList<String>();
////		// TreeMap means this collection sorted by key anytime
////		Map<String, String> agentStrings = new TreeMap<String, String>();
////		for (CAgent agent : agentList) {
////			StringBuffer sb = new StringBuffer();
////			for (CSite site : agent.getSites()) {
////				siteNames.add(site.getName());
////			}
////			Collections.sort(siteNames);
////			for (CSite site : agent.getSites()) {
////				sb.append(site.getName());
////			}
////			agentStrings.put(agent.getName(), sb.toString());
////		}
////		for (String key : agentStrings.keySet()) {
////			sb.append(key + agentStrings.get(key));
////		}
//		return sb.toString();
//	}
}