package com.plectix.simulator.stories;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.plectix.simulator.components.stories.CStories;
import com.plectix.simulator.components.stories.compressions.CompressionPassport;
import com.plectix.simulator.components.stories.compressions.Compressor;
import com.plectix.simulator.components.stories.enums.EMarkOfEvent;
import com.plectix.simulator.components.stories.storage.AtomicEvent;
import com.plectix.simulator.components.stories.storage.IEventIterator;
import com.plectix.simulator.components.stories.storage.IWireStorage;
import com.plectix.simulator.components.stories.storage.StoryStorageException;
import com.plectix.simulator.components.stories.storage.WireHashKey;
import com.plectix.simulator.simulator.SimulationArguments.StoryCompressionMode;

public class TestPassportCorrectness extends InitStoriesTests {

	private static final String separator = File.separator;
	private static final String path = "test.data" + separator + "stories"
	// + separator + "simple"
			+ separator;
	Map<Integer, CompressionPassport> passports;

	@Parameters
	public static Collection<Object[]> regExValues() {
		return getAllTestFileNames(path);
	}

	private String fileName;

	public TestPassportCorrectness(String fileName) {

		super(path, fileName, false, false, false);

		this.fileName = fileName;

		passports = new TreeMap<Integer, CompressionPassport>();

	}

	@Test
	public void testAll() throws StoryStorageException {
		CStories stories = getStories();
		for (Entry<Integer, IWireStorage> entry : stories
				.getEventsMapForCurrentStory().entrySet()) {
			if (entry.getValue().isImportantStory()) {
				passports.put(entry.getKey(), entry.getValue()
						.extractPassport());
			}
		}

//		System.out
//				.println("\n\n**************************************fileName = "
//						+ fileName);

		for (CompressionPassport cp : passports.values()) {
			testPrepare(cp);
			testEventIterator(cp);
			testAgentTypeIterator(cp);
			testAgentWires(cp);
			testRemoveEventWithMarkDeleted(cp);
			testSwap(cp);
			testSwapAndUndo(cp);
		}

	}

	public void testPrepare(CompressionPassport passport)
			throws StoryStorageException {
		testStoragesCorrectness(passport.getStorage());
	}

	public void testEventIterator(CompressionPassport passport)
			throws StoryStorageException {
		boolean reverse = false;

		IEventIterator eventIterator = passport.eventIterator(reverse);
		LinkedHashSet<Long> eventsSteps = new LinkedHashSet<Long>();
		LinkedHashSet<Long> mySteps = new LinkedHashSet<Long>();
		for (Map<Long, AtomicEvent<?>> map : passport.getStorage()
				.getStorageWires().values()) {
			for (Long l : map.keySet())
				eventsSteps.add(l);
		}

		Long l = new Long(-2);
		Long m = l;
		while (eventIterator.hasNext()) {
			m = eventIterator.next();
			assertTrue(m > l);
			assertTrue(eventsSteps.contains(m));
			mySteps.add(m);
			l = m;
		}

		for (Long k : eventsSteps) {
			assertTrue(mySteps.contains(k));
		}

	}

	public void testAgentTypeIterator(CompressionPassport passport) {
		Iterator<Integer> agentTypeIterator = passport.agentTypeIterator();
		Integer type;
		long id;
		LinkedHashSet<Long> myIds = new LinkedHashSet<Long>();

		while (agentTypeIterator.hasNext()) {
			type = agentTypeIterator.next();
			Iterator<Long> agentIterator = passport.agentIterator(type);
			while (agentIterator.hasNext()) {
				id = agentIterator.next();
				myIds.add(id);
			}
		}

		LinkedHashSet<Long> allIds = new LinkedHashSet<Long>();

		for (WireHashKey wk : passport.getStorage().getStorageWires().keySet()) {
			allIds.add(wk.getAgentId());
			assertTrue(myIds.contains(wk.getAgentId()));
		}

		for (Long l : myIds) {
			assertTrue(allIds.contains(l));
		}

	}

	public void testAgentWires(CompressionPassport passport) {
		Iterator<Integer> agentTypeIterator = passport.agentTypeIterator();
		Integer type;
		long id;
		LinkedHashMap<Integer, LinkedHashSet<Integer>> wiresId = new LinkedHashMap<Integer, LinkedHashSet<Integer>>();
		LinkedHashSet<Integer> dangerous = new LinkedHashSet<Integer>();
		LinkedHashSet<Integer> dangerous2 = new LinkedHashSet<Integer>();
		while (agentTypeIterator.hasNext()) {
			type = agentTypeIterator.next();
			Iterator<Long> agentIterator = passport.agentIterator(type);
			assertTrue(wiresId.get(type) == null);
			wiresId.put(type, new LinkedHashSet<Integer>());
			boolean first = true;
			int idSite;
			while (agentIterator.hasNext()) {
				id = agentIterator.next();
				ArrayList<WireHashKey> getAgentWires = passport
						.getAgentWires(id);

				if (first) {
					for (WireHashKey wk : getAgentWires) {
						idSite = wk.getSiteId();

						assertTrue(!(wiresId.get(type).contains(idSite)
								&& dangerous.contains(idSite) && dangerous2
								.contains(idSite)));
						if (wiresId.get(type).contains(idSite)) {
							if (dangerous.contains(idSite)) {
								dangerous2.add(idSite);
							} else {
								dangerous.add(idSite);
							}

						}
						wiresId.get(type).add(idSite);
					}

				} else {
					LinkedHashSet<Integer> second = new LinkedHashSet<Integer>();

					for (WireHashKey wk : getAgentWires) {
						idSite = wk.getSiteId();
						assertTrue(!(second.contains(idSite)
								&& dangerous.contains(idSite) && dangerous2
								.contains(idSite)));
						if (second.contains(idSite)) {
							if (dangerous.contains(idSite)) {
								dangerous2.add(idSite);
							} else {
								dangerous.add(idSite);
							}

						}
						second.add(idSite);
					}
					// equalsSetInteger(second, wiresId.get(type));
				}

				dangerous.clear();
				dangerous2.clear();
				first = false;
			}
		}

		ArrayList<WireHashKey> getAgentWires = passport.getAgentWires(0);

	}

	private void equalsSetLong(LinkedHashSet<Long> first, LinkedHashSet<Long> second) {
		for (Long l : first) {
			assertTrue(second.contains(l));
		}

		for (Long l : second) {
			assertTrue(first.contains(l));
		}
	}

	private void equalsSetInteger(LinkedHashSet<Integer> first,
			LinkedHashSet<Integer> second) {
		for (Integer l : first) {
			assertTrue(second.contains(l));
			if (!second.contains(l)) {
				System.out.println((l) + "first");
			}
		}

		for (Integer li : second) {
			assertTrue(first.contains(li));
			if (!first.contains(li)) {
				System.out.println((li) + "second");
			}
		}
	}

	public void testSwapAgent(CompressionPassport passport) {

	}

	public void testUndoSwap(CompressionPassport passport) {
	}

	private void testRemoveEventWithMarkDeleted(CompressionPassport passport)
			throws StoryStorageException {
		Compressor weak = new Compressor(passport.getStorage());
		passport.getStorage().markAllNull();
		passport.getStorage().initialEvent().onlySetMark(EMarkOfEvent.KEPT);

		weak.execute(StoryCompressionMode.WEAK);

		passport.getStorage().markAllUnresolvedAsDeleted();
		passport.removeEventWithMarkDelete();
		// passport.getStorage().markAllUnresolved();
		testStoragesCorrectness(passport.getStorage());
	}

	private void testSwap(CompressionPassport passport) {
		Iterator<Integer> agentTypeIterator = passport.agentTypeIterator();
		Integer type;
		long id;
		LinkedHashSet<Long> myIds = new LinkedHashSet<Long>();

		while (agentTypeIterator.hasNext()) {
			type = agentTypeIterator.next();
			Iterator<Long> agentIterator = passport.agentIterator(type);
			while (agentIterator.hasNext()) {
				id = agentIterator.next();
				myIds.add(id);
			}
		}

		for (Long i : myIds) {
			for (Long j : myIds) {
				if (i == j)
					continue;
				passport.isAbleToSwap(i, j);
			}
		}
	}

	private void testSwapAndUndo(CompressionPassport passport)
			throws StoryStorageException {
		Iterator<Integer> agentTypeIterator = passport.agentTypeIterator();
		Integer type;
		long id;
		LinkedHashSet<Long> myIds = new LinkedHashSet<Long>();

		while (agentTypeIterator.hasNext()) {
			type = agentTypeIterator.next();
			Iterator<Long> agentIterator = passport.agentIterator(type);
			while (agentIterator.hasNext()) {
				id = agentIterator.next();
				myIds.add(id);
			}
		}

		int k = 0;
		for (Long i : myIds) {
			for (Long j : myIds) {
				if (i == j)
					continue;
				if (passport.isAbleToSwap(i, j)) {
					LinkedList<Long> l1 = new LinkedList<Long>();
					LinkedList<Long> l2 = new LinkedList<Long>();
					l1.add(i);
					l2.add(j);
					for (Long l = Long.valueOf(2); l <= passport.getStorage()
							.observableEvent().getStepId()-1; l++) {
						if (k < 1000) {
							passport.swapAgents(l1, l2, Long.valueOf(l), true);
							passport.undoSwap();

							
							StoryCorrectness.testCountUnresolvedOnWire(passport
									.getStorage());
							
							passport.swapAgents(l1, l2, Long.valueOf(l), false);
							passport.undoSwap();

							
							StoryCorrectness.testCountUnresolvedOnWire(passport
									.getStorage());
							
							k++;
						}
			

					}
				}
			}
		}

	}

	private void testStoragesCorrectness(IWireStorage storage)
			throws StoryStorageException {

		StoryCorrectness.testStorage(storage);

		StoryCorrectness.testWires(storage);
		StoryCorrectness.testOfStates(storage);
		StoryCorrectness.testOfParallelCorrectness(storage);
		StoryCorrectness.testInternalStatesIterator(storage);
		StoryCorrectness.testWireWithMinUnresolved(storage);
		StoryCorrectness.testCompareStorageMaps(storage);
		StoryCorrectness.testCountUnresolvedOnWire(storage);

	}

}
