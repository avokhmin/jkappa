package com.plectix.simulator.staticanalysis.subviews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.xml.stream.XMLStreamException;

import com.plectix.simulator.interfaces.ConnectedComponentInterface;
import com.plectix.simulator.interfaces.SolutionInterface;
import com.plectix.simulator.simulationclasses.solution.SuperSubstance;
import com.plectix.simulator.simulator.XMLSimulatorWriter;
import com.plectix.simulator.staticanalysis.Agent;
import com.plectix.simulator.staticanalysis.Rule;
import com.plectix.simulator.staticanalysis.Site;
import com.plectix.simulator.staticanalysis.abstracting.AbstractAgent;
import com.plectix.simulator.staticanalysis.abstracting.AbstractSite;
import com.plectix.simulator.staticanalysis.influencemap.InfluenceMap;
import com.plectix.simulator.staticanalysis.influencemap.nofuture.InfluenceMapWithoutFuture;
import com.plectix.simulator.staticanalysis.subviews.base.AbstractClassSubViewBuilder;
import com.plectix.simulator.staticanalysis.subviews.base.AbstractionRule;
import com.plectix.simulator.staticanalysis.subviews.storage.SubViewsExeption;
import com.plectix.simulator.staticanalysis.subviews.storage.SubViewsInterface;
import com.plectix.simulator.util.NameDictionary;

public final class MainSubViews extends AbstractClassSubViewBuilder implements
		AllSubViewsOfAllAgentsInterface {
	private Map<String, AbstractAgent> agentNameToAgent;
	private List<AbstractionRule> abstractRules;
	private LinkedHashSet<Integer> deadRules;

	public MainSubViews() {
		super();
		agentNameToAgent = new LinkedHashMap<String, AbstractAgent>();
		abstractRules = new LinkedList<AbstractionRule>();
	}

	public final void build(SolutionInterface solution, List<Rule> rules) {
		Collection<Agent> agents = prepareSolutionAgents(solution);
		fillModelMapOfAgents(agents, rules);
		constructAbstractRules(rules);
		constructClasses(abstractRules, agentNameToAgent);

		InfluenceMap wI = new InfluenceMapWithoutFuture();
		// AInfluenceMap wI = new CInfluenceMapWithFuture();
		wI.initInfluenceMap(abstractRules, null, null, agentNameToAgent);

		fillingClasses(agents);
		initBoundRulesAndSubViews();
		try {
			// we use this original heuristic TODO
			constructAbstractContactMap(wI);
			constructAbstractContactMap(wI);
			constructAbstractContactMap(wI);
		} catch (SubViewsExeption e) {
			e.printStackTrace();
		}
	}

	private final void initBoundRulesAndSubViews() {
		for (AbstractionRule rule : abstractRules)
			rule.initActionsToSubViews(getSubViews());
	}

	private final void fillingClasses(Collection<Agent> agents) {
		for (List<SubViewsInterface> subViewsList : this.getSubViews().values()) {
			for (SubViewsInterface subViews : subViewsList)
				subViews.fillingInitialState(agentNameToAgent, agents);
		}

	}

	@Override
	public final List<SubViewsInterface> getAllSubViewsByType(String type) {
		return this.getSubViews().get(type);
	}

	public final Iterator<String> getAllTypesIdOfAgents() {
		return agentNameToAgent.keySet().iterator();
	}

	public final Map<String, AbstractAgent> getAgentNameToAgent() {
		return agentNameToAgent;
	}

	// ==========================================================================
	// =====================

	private static final Collection<Agent> prepareSolutionAgents(
			SolutionInterface solution) {
		Collection<Agent> agents = new ArrayList<Agent>();
		if (solution.getStraightStorage() != null) {
			agents.addAll(solution.getStraightStorage().getAgents());
		}
		if (solution.getSuperStorage() != null) {
			for (SuperSubstance substance : solution.getSuperStorage()
					.getComponents()) {
				agents.addAll(substance.getComponent().getAgents());
			}
		}
		return agents;

	}

	private final void fillModelMapOfAgents(Collection<Agent> agents, List<Rule> rules) {
		fillModelMapByAgentList(agents);

		for (Rule rule : rules) {
			for (ConnectedComponentInterface cc : rule.getLeftHandSide())
				fillModelMapByAgentList(cc.getAgents());
			if (rule.getRightHandSide() != null)
				for (ConnectedComponentInterface cc : rule.getRightHandSide())
					fillModelMapByAgentList(cc.getAgents());
		}
	}

	private final void fillModelMapByAgentList(Collection<Agent> agents) {
		for (Agent agent : agents) {
			AbstractAgent modelAgent = agentNameToAgent.get(agent.getName());
			if (modelAgent == null) {
				modelAgent = new AbstractAgent(agent.getName());
				agentNameToAgent.put(agent.getName(), modelAgent);
			}

			for (Site site : agent.getSites()) {
				AbstractSite abastractSite = new AbstractSite(site);
				abastractSite.setParentAgent(modelAgent);
				modelAgent.addModelSite(abastractSite);
			}
		}
	}

	/**
	 * This method initializes abstract rules.<br>
	 * For <code>AGENT_OR_RULE</code> mode, creates abstract contact map.
	 * 
	 * @param rules
	 *            given rules
	 */
	private final void constructAbstractRules(List<Rule> rules) {
		for (Rule rule : rules) {
			AbstractionRule abstractRule = new AbstractionRule(rule);
			abstractRules.add(abstractRule);
		}
	}

	private final void constructClasses(List<AbstractionRule> abstractRules,
			Map<String, AbstractAgent> agentNameToAgent) {
		List<AbstractionRule> list = new LinkedList<AbstractionRule>();
		for (AbstractionRule abstraction : abstractRules)
			list.add(abstraction);
		constructClassesSubViews(list, agentNameToAgent);

	}

	private final void constructAbstractContactMap(InfluenceMap influenceMap)
			throws SubViewsExeption {
		// RuleId
		Queue<Integer> activeRule = new LinkedList<Integer>();
		// RuleId -> isIncluded
		Map<Integer, Boolean> includedInQueue = new LinkedHashMap<Integer, Boolean>();
		// ruleId -> number in array
		Map<Integer, Integer> filter = new LinkedHashMap<Integer, Integer>();

		for (int i = 0; i < abstractRules.size(); i++) {
			AbstractionRule rule = abstractRules.get(i);
			activeRule.add(rule.getRuleId());
			includedInQueue.put(rule.getRuleId(), true);
			filter.put(rule.getRuleId(), i);
		}

		Integer ruleId;
		AbstractionRule rule;
		WrapperTwoSet activatedRule;
		LinkedHashSet<Integer> intersection = new LinkedHashSet<Integer>();
		while (!activeRule.isEmpty()) {
			ruleId = activeRule.poll();
			includedInQueue.put(ruleId, false);
			rule = abstractRules.get(filter.get(ruleId));
			activatedRule = rule.apply(agentNameToAgent, this.getSubViews());

			intersection = intersect(activatedRule, influenceMap
					.getActivationByRule(ruleId));
			if (intersection != null)
				for (int j : intersection) {
					if (includedInQueue.get(j))
						continue;
					includedInQueue.put(j, true);
					activeRule.add(j);
				}
		}
	}

	private final LinkedHashSet<Integer> intersect(WrapperTwoSet activatedRule,
			List<Integer> activationByRule) {
		if (activatedRule == null || activationByRule == null) {
			return null;
		}
		
		LinkedHashSet<Integer> answer = activatedRule.getSecond();
		for (int i : activatedRule.getFirst()) {
			if (activationByRule.contains(i)) {
				answer.add(i);
			}
		}
		return answer;
	}

	public final Map<String, AbstractAgent> getFullMapOfAgents() {
		return agentNameToAgent;
	}

	public final void initDeadRules() {
		deadRules = new LinkedHashSet<Integer>();
		for (AbstractionRule rule : abstractRules)
			if (!rule.isApply())
				deadRules.add(rule.getRuleId());
	}

	public final LinkedHashSet<Integer> getDeadRules() {
		return deadRules;
	}

	public final List<AbstractionRule> getRules() {
		return abstractRules;
	}

	@Override
	public final void createXML(XMLSimulatorWriter writer) throws XMLStreamException {
		// TODO Auto-generated method stub
		writer.writeStartElement("Reachables");
		writer.writeAttribute("Name", "Subviews");
		for (String agentType : agentNameToAgent.keySet()) {
			if (NameDictionary.isDefaultAgentName(agentType))
				continue;
			for (SubViewsInterface subViews : this.getSubViews().get(agentType)) {
				// Element set = doc.createElement("Set");
				writer.writeStartElement("Set");
				writer.writeAttribute("Agent", agentType);
				// Element tag = doc.createElement("Tag");
				writer.writeStartElement("Tag");
				String data = "Agent: " + agentType + " ; Sites: ";
				String sites = new String("");
				for (String siteName : subViews.getSubViewClass().getSitesNames()) {
					if (sites.length() != 0)
						sites += ",";
					sites += siteName;
				}
				data += sites + " ";
				writer.writeAttribute("Data", data);
				// set.appendChild(tag);
				writer.writeEndElement();

				for (AbstractAgent agent : subViews.getAllSubViews()) {
					writer.writeStartElement("Entry");
					writer.writeAttribute("Data", agent.toStringForXML());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
	}
}
