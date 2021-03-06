package com.plectix.simulator.staticanalysis.subviews;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.plectix.simulator.interfaces.SolutionInterface;
import com.plectix.simulator.staticanalysis.Rule;
import com.plectix.simulator.staticanalysis.StaticAnalysisException;
import com.plectix.simulator.staticanalysis.abstracting.AbstractAgent;
import com.plectix.simulator.staticanalysis.subviews.base.AbstractionRule;
import com.plectix.simulator.staticanalysis.subviews.storage.SubViewsInterface;

// TODO please rename
public interface AllSubViewsOfAllAgentsInterface {

	public void build(SolutionInterface solution, List<Rule> rules) throws StaticAnalysisException;

	public Iterator<String> getAllTypesIdOfAgents();

	public List<SubViewsInterface> getAllSubViewsByType(String type);

	public Map<String, AbstractAgent> getFullMapOfAgents();

	public void initDeadRules();

	public List<AbstractionRule> getAbstractRules();

	public Map<String, AbstractAgent> getAgentNameToAgent();

	public boolean isEmpty();

	public Map<String, List<SubViewsInterface>> getSubViews();
	
	public Set<Integer> getDeadRules();

}