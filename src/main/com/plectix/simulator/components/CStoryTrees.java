package com.plectix.simulator.components;

import java.util.*;

import com.plectix.simulator.components.CNetworkNotation.*;
import com.plectix.simulator.components.CNetworkNotation.AgentSitesFromRules.SitesFromRules;
import com.plectix.simulator.interfaces.IRule;

public final class CStoryTrees {
	private HashMap<Integer, HashMap<RuleIDs, List<RuleIDs>>> contiguityList;
	private int ruleId;
	private Map<Long, HashMap<Integer, StorySites>> sSites;
	private NetworkNotationForCurrentStory nnCS;

	private HashMap<Integer, List<Integer>> ruleIDToTraceID;
	private HashMap<Integer, List<RuleIDs>> traceIDToRuleIDs;

	private int isomorphicCount = 1;

	// TODO
	private class StorySites {
		boolean isLeaf;
		int level = 0;
		int siteID;

		public StorySites(int level, int siteID, boolean isLeaf) {
			this.level = level;
			this.siteID = siteID;
			this.isLeaf = isLeaf;
		}

		public void checkLeaf(List<CNetworkNotation> commonList, int begin,
				long key) {
			for (int i = begin; i < commonList.size(); i++) {
				CNetworkNotation nn = commonList.get(i);
				AgentSites as = nn.changedAgentsFromSolution.get(key);
				if (as != null) {
					CStoriesSiteStates sss = (CStoriesSiteStates) as.sites
							.get(this.siteID);
					if (sss != null)
						return;
				}
			}
			this.isLeaf = true;
		}
	}

	// TODO separate
	private class RuleIDs {
		int ruleID;
		int indexInTrace;
		boolean isLeaf;
		int level;
		List<String> notation;

		// private NetworkNotationForCurrentStory nnCS;

		public RuleIDs(int ruleID, int indexInTrace, int level) {
			this.ruleID = ruleID;
			this.level = level;
			this.indexInTrace = indexInTrace;
			this.isLeaf = false;
			notation = new ArrayList<String>();
		}

		public final boolean equals(Object obj) {
			if (!(obj instanceof RuleIDs))
				return false;
			RuleIDs ruleID = (RuleIDs) obj;
			if (indexInTrace != ruleID.indexInTrace)
				return false;
			return true;
		}
	}

	public final int getIsomorphicCount() {
		return isomorphicCount;
	}

	public CStoryTrees(int ruleId, NetworkNotationForCurrentStory nnCS) {
		this.nnCS = nnCS;
		// contiguityList = new HashMap<Integer, List<Integer>>();
		this.ruleId = ruleId;
	}

	public final int getRuleID() {
		return this.ruleId;
	}

	public final HashMap<RuleIDs, List<RuleIDs>> getList(int i) {
		return contiguityList.get(i);
	}

	public final Map<Integer, HashMap<RuleIDs, List<RuleIDs>>> getMap() {
		return Collections.unmodifiableMap(contiguityList);
	}

	public final void getTreeFromList(List<CNetworkNotation> commonList) {
		int index = 0;

		traceIDToLevel = new HashMap<Integer, Integer>();
		this.ruleIDToTraceID = new HashMap<Integer, List<Integer>>();
		this.traceIDToRuleIDs = new HashMap<Integer, List<RuleIDs>>();

		contiguityList = new HashMap<Integer, HashMap<RuleIDs, List<RuleIDs>>>();
		CNetworkNotation newNN = commonList.get(index);
		traceIDToLevel.put(newNN.getStep(), index);
		List<Integer> list = new ArrayList<Integer>();
		list.add(newNN.getStep());
		this.ruleIDToTraceID.put(newNN.getRule().getRuleID(), list);

		/*
		 * if (commonList.size() == 1) {
		 * 
		 * }
		 */

		// sSites = new HashMap<Long, HashMap<Integer, StorySites>>();
		// addToStorySites(newNN, index, commonList, index + 1);
		// getTree(index, 1, newNN, commonList);
		isCausing(newNN, commonList, index + 1, 0);
		pushTree();
	}

	// private void getTree(int index, int begin, CNetworkNotation newNN,
	// List<CNetworkNotation> commonList) {
	// List<Integer> list = new ArrayList<Integer>();
	// int begInd = begin;
	// index++;
	// boolean isTrue = false;
	// if (!contiguityList.keySet().contains(newNN.getRule().getRuleID())) {
	// for (int i = begin; i < commonList.size(); i++) {
	// CNetworkNotation nn = commonList.get(i);
	// addToStorySites(nn, index, commonList, i + 1);
	// //if (!list.contains(nn.getRule().getRuleID()))
	// //if (nn.getRule().getRuleID() != newNN.getRule().getRuleID())
	// list.add(nn.getRule().getRuleID());
	// if (fullCover(index)) {
	// begInd = i + 1;
	// isTrue = true;
	// break;
	// }
	// }
	// addToConList(newNN, list);
	// if (isTrue) {
	// for (int i = begin; i < commonList.size(); i++) {
	// CNetworkNotation nn = commonList.get(i);
	// getTree(index, begInd, nn, commonList);
	// }
	// }
	// }
	// }

	private void isCausing(CNetworkNotation newNN,
			List<CNetworkNotation> commonList, int begin, int level) {
		Iterator<Long> agentIterator = newNN.usedAgentsFromRules.keySet()
				.iterator();
		List<Integer> list = new ArrayList<Integer>();

		if (begin == commonList.size()) {
			Integer key = newNN.getRule().getRuleID();
			HashMap<Integer, List<RuleIDs>> cList = new HashMap<Integer, List<RuleIDs>>();
			// this.traceIDToLevel
			// contiguityList.put(key, cList);

			addToMapRuleIDToTraceID(newNN, level);
			return;
		}

		while (agentIterator.hasNext()) {
			Long agentKey = agentIterator.next();
			AgentSitesFromRules aSFR = newNN.usedAgentsFromRules.get(agentKey);
			Iterator<Integer> siteIterator = aSFR.sites.keySet().iterator();
			int leafIndex = 0;
			while (siteIterator.hasNext()) {
				Integer siteKey = siteIterator.next();
				SitesFromRules sFR = aSFR.sites.get(siteKey);
				boolean isLink = true;
				if (isCausing(newNN, commonList, begin, isLink, agentKey,
						siteKey, sFR, level) == IS_NOT_CAUSE) {
					leafIndex++;
				}
				isLink = false;
				if (isCausing(newNN, commonList, begin, isLink, agentKey,
						siteKey, sFR, level) == IS_NOT_CAUSE) {
					leafIndex++;
				}
			}
			if (aSFR.sites.size() * 2 == leafIndex) {
				addToMapRuleIDToTraceID(newNN, level);
			}
		}
		return;
	}

	public static final byte IS_CAUSE = 2;
	public static final byte IS_NONE = 1;
	public static final byte IS_NOT_CAUSE = 0;

	private byte isCausing(CNetworkNotation newNN,
			List<CNetworkNotation> commonList, int begin, boolean isLink,
			Long agentKey, int siteKey, SitesFromRules sFR, int level) {
		for (int i = begin; i < commonList.size(); i++) {
			CNetworkNotation comparableNN = commonList.get(i);
			AgentSitesFromRules aSFRComparable = comparableNN.usedAgentsFromRules
					.get(agentKey);
			if (aSFRComparable != null) {
				SitesFromRules sFRComparable = aSFRComparable.sites
						.get(siteKey);
				if (sFRComparable != null) {
					if (sFRComparable.isCausing(sFR, isLink)) {
						level++;
						addToConList(newNN, comparableNN, i, level);
						isCausing(comparableNN, commonList, i + 1, level);
						return IS_CAUSE;
					}
					if (!isLink
							&& sFRComparable.getInternalStateMode() != CNetworkNotation.MODE_NONE) {
						return IS_NONE;
					}
					if (isLink
							&& sFRComparable.getLinkStateMode() != CNetworkNotation.MODE_NONE) {
						return IS_NONE;
					}
				}
			}
		}
		return IS_NOT_CAUSE;
	}

	private HashMap<Integer, Integer> traceIDToLevel;

	private void pushTree() {
		Iterator<Integer> ruleIterator = ruleIDToTraceID.keySet().iterator();
		// contiguityList.keySet().iterator();

		while (ruleIterator.hasNext()) {
			int key = ruleIterator.next();
			// HashMap<Integer, List<RuleIDs>> map = contiguityList.get(key);
			List<Integer> traceIDList = ruleIDToTraceID.get(key);

			for (Integer traceID : traceIDList) {
				List<RuleIDs> curList = new ArrayList<RuleIDs>();
				List<RuleIDs> ruleIDsList = traceIDToRuleIDs.get(traceID);
				for (RuleIDs ruleIDs : ruleIDsList) {
					Integer level = traceIDToLevel.get(ruleIDs.indexInTrace);
					if ((level != null) && level == ruleIDs.level)
						curList.add(ruleIDs);
				}
				traceIDToRuleIDs.put(traceID, curList);
			}
		}
	}

	public String getText(int index) {
		return nnCS.getNetworkNotation(index).getRule().getName();
	}

	public void getLevelToTraceID(
			HashMap<Integer, List<Integer>> levelToTraceID,
			HashMap<Integer, List<String>> traceIDToIntroString,
			HashMap<Integer, String> traceIDToData,
			HashMap<Integer, String> traceIDToText) {
		Iterator<Integer> iterator = traceIDToLevel.keySet().iterator();
		while (iterator.hasNext()) {
			int traceID = iterator.next();
			int level = traceIDToLevel.get(traceID);
			List<Integer> list = levelToTraceID.get(level);
			if (list == null) {
				list = new ArrayList<Integer>();
				levelToTraceID.put(level, list);
			}
			list.add(traceID);
			CNetworkNotation nn = this.nnCS.getNetworkNotation(traceID);
			if (nn.getAgentsNotation().size() > 0)
				traceIDToIntroString.put(traceID, nn.getAgentsNotation());
			IRule rule = nn.getRule();
			traceIDToData.put(traceID, rule.getData());
			traceIDToText.put(traceID, rule.getName());
		}
	}

	private void addToMapRuleIDToTraceID(CNetworkNotation nn, int level) {
		int ruleID = nn.getRule().getRuleID();
		List<Integer> list = ruleIDToTraceID.get(ruleID);
		if (list == null) {
			list = new ArrayList<Integer>();
			ruleIDToTraceID.put(ruleID, list);
		}
		int indexInTrace = nn.getStep();
		if (!list.contains(indexInTrace)) {
			list.add(indexInTrace);
			// traceIDToRuleIDs.put(indexInTrace, new ArrayList<RuleIDs>());
		}

		List<RuleIDs> ruleIDsList = traceIDToRuleIDs.get(nn.getStep());
		if (ruleIDsList == null) {
			ruleIDsList = new ArrayList<RuleIDs>();
			traceIDToRuleIDs.put(nn.getStep(), ruleIDsList);
		}

		// RuleIDs cRuleID = new RuleIDs(nn.getRule().getRuleID(), nn.getStep(),
		// level);

		// if (!ruleIDsList.contains(ruleID))
		// ruleIDsList.add(cRuleID);

		Integer levelIn = traceIDToLevel.get(indexInTrace);
		if ((levelIn == null) || (levelIn != null && levelIn < level))
			traceIDToLevel.put(nn.getStep(), level);
	}

	private void addToConList(CNetworkNotation nn, CNetworkNotation nnToAdd,
			int indexInTrace, int level) {

		Integer key = nnToAdd.getRule().getRuleID();
		// HashMap<RuleIDs,List<RuleIDs>> curMap = contiguityList.get(key);

		List<Integer> intTraceList = ruleIDToTraceID.get(key);

		if (intTraceList == null) {
			intTraceList = new ArrayList<Integer>();
			ruleIDToTraceID.put(key, intTraceList);
		}

		if (!intTraceList.contains(nnToAdd.getStep())) {
			intTraceList.add(nnToAdd.getStep());
		}

		List<RuleIDs> ruleIDsList = traceIDToRuleIDs.get(nn.getStep());
		if (ruleIDsList == null) {
			ruleIDsList = new ArrayList<RuleIDs>();
			traceIDToRuleIDs.put(nn.getStep(), ruleIDsList);
		}

		RuleIDs ruleID = new RuleIDs(nnToAdd.getRule().getRuleID(), nnToAdd
				.getStep(), level);

		if (!ruleIDsList.contains(ruleID))
			ruleIDsList.add(ruleID);

		Integer levelIn = traceIDToLevel.get(indexInTrace);
		if ((levelIn == null) || (levelIn != null && levelIn < level))
			traceIDToLevel.put(nnToAdd.getStep(), level);
	}

	public HashMap<Integer, List<Integer>> getRuleIDToTraceID() {
		return ruleIDToTraceID;
	}

	public HashMap<Integer, List<RuleIDs>> getTraceIDToRuleIDs() {
		return traceIDToRuleIDs;
	}

	public HashMap<Integer, Integer> getTraceIDToLevel() {
		return traceIDToLevel;
	}

	public final boolean isIsomorphic(CStoryTrees treeIn) {
		if (this.getRuleIDToTraceID().size() != treeIn.getRuleIDToTraceID()
				.size())
			return false;

		List<Integer> treeNumbers = getTreeNumbers(this);

		List<Integer> treeNumbersIn = getTreeNumbers(treeIn);

		for (Integer i : treeNumbers) {
			if (treeNumbersIn.contains(i))
				treeNumbersIn.remove(i);
			else
				return false;
		}

		if (treeNumbersIn.size() != 0)
			return false;

		isomorphicCount++;
		return true;
	}

	private final List<Integer> getTreeNumbers(CStoryTrees treeIn) {
		Iterator<Integer> ruleIterator = treeIn.getRuleIDToTraceID().keySet()
				.iterator();
		List<Integer> treeNumbers = new ArrayList<Integer>();

		while (ruleIterator.hasNext()) {
			int key = ruleIterator.next();
			List<Integer> list = treeIn.getRuleIDToTraceID().get(key);
			for (Integer number : list)
				treeNumbers.add(key);
		}
		return treeNumbers;
	}

	private void addToStorySites(CNetworkNotation nn, int index,
			List<CNetworkNotation> commonList, int begin) {
		Map<Long, AgentSites> chAFS = nn.changedAgentsFromSolution;
		Iterator<Long> iterator = chAFS.keySet().iterator();

		while (iterator.hasNext()) {
			Long key = iterator.next();
			HashMap<Integer, StorySites> ss = sSites.get(key);

			AgentSites as = chAFS.get(key);

			if (ss == null) {
				ss = new HashMap<Integer, StorySites>();
				sSites.put(key, ss);
			}

			Iterator<Integer> siteIterator = as.sites.keySet().iterator();
			while (siteIterator.hasNext()) {
				Integer keySite = siteIterator.next();
				StorySites ssSite = ss.get(keySite);

				if (ssSite == null) {
					ssSite = new StorySites(index, keySite, false);
					ssSite.checkLeaf(commonList, begin, key);
					ss.put(keySite, ssSite);
				} else {
					ssSite.level = index;
				}
			}
		}
	}

}