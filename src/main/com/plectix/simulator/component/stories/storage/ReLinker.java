package com.plectix.simulator.component.stories.storage;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.plectix.simulator.component.stories.TypeOfWire;

public class ReLinker {

	public static void bottom(
			Long firstEventId,
			StateOfLink oldState,
			StateOfLink newState,
			WireHashKey wk,
			Map<WireHashKey, TreeMap<Long, AtomicEvent<?>>> storageWires)
			throws StoryStorageException {
		TreeMap<Long, AtomicEvent<?>> map = storageWires.get(wk);
		Entry<Long, AtomicEvent<?>> entry = map.ceilingEntry(firstEventId);
		HashSet<WireHashKey> set = new HashSet<WireHashKey>();
		while (entry != null) {
			AtomicEvent<?> ae = entry.getValue();
			Object afterState = ae.getState().getAfterState();
			if (afterState != null) {
				StateOfLink sl = (StateOfLink) afterState;
				if (sl.getAgentId() != -1)
					addWireForRelinking(set, sl);
			}
			Object beforeState = ae.getState().getBeforeState();
			if (beforeState != null) {
				StateOfLink sl = (StateOfLink) beforeState;
				if (sl.getAgentId() != -1)
					addWireForRelinking(set, sl);
			}

			entry = map.higherEntry(entry.getKey());
		}

		relinkBottom(set, newState, oldState, firstEventId,storageWires);
	}

	public static void top(
			Long firstEventId,
			StateOfLink oldState,
			StateOfLink newState,
			WireHashKey wk,
			Map<WireHashKey, TreeMap<Long, AtomicEvent<?>>> storageWires)
			throws StoryStorageException {
		TreeMap<Long, AtomicEvent<?>> map = storageWires.get(wk);
		Entry<Long, AtomicEvent<?>> entry = map.floorEntry(firstEventId);
		HashSet<WireHashKey> set = new HashSet<WireHashKey>();

		while (entry != null) {
			AtomicEvent<?> ae = entry.getValue();
			if (ae.getState().getAfterState() != null) {
				StateOfLink sl = (StateOfLink) ae.getState().getAfterState();
				if (sl.getAgentId() != -1)
					addWireForRelinking(set, sl);
			}
			if (ae.getState().getBeforeState() != null) {
				StateOfLink sl = (StateOfLink) ae.getState().getBeforeState();
				if (sl.getAgentId() != -1)
					addWireForRelinking(set, sl);
			}

			entry = map.lowerEntry(entry.getKey());
		}

		relinkTop(set, newState, oldState, firstEventId, storageWires);
	}
	
	public final static void relinkBottom(HashSet<WireHashKey> wireKeys,
			StateOfLink newState, StateOfLink oldState, long first,Map<WireHashKey, TreeMap<Long, AtomicEvent<?>>> storageWires)
			throws StoryStorageException {
		for (WireHashKey wk : wireKeys) {
			reLinkBottom(wk, newState, oldState, first,storageWires.get(wk));
		}

	}

	public final static void relinkTop(HashSet<WireHashKey> wireKeys,
			StateOfLink newState, StateOfLink oldState, long first,Map<WireHashKey, TreeMap<Long, AtomicEvent<?>>> storageWires)
			throws StoryStorageException {
		for (WireHashKey wk : wireKeys) {
			reLinkTop(storageWires.get(wk), newState, oldState, first);
		}
	}

	
	protected final static void reLinkTop(TreeMap<Long, AtomicEvent<?>> wire, StateOfLink newState,
			StateOfLink oldState, long first) throws StoryStorageException {
		if (newState == null || oldState == null) {
			throw new StoryStorageException("relink null!");
		}

		if (wire == null) {
			throw new StoryStorageException(
					"internal error : storage doesn't contain wire");
		}
		Entry<Long, AtomicEvent<?>> lowerEntry = wire.floorEntry(first);

		while (lowerEntry != null) {
			AtomicEvent<?> ae = lowerEntry.getValue();
			first = lowerEntry.getKey();
			correctLinkOnWire(oldState, newState, ae);

			lowerEntry = wire.lowerEntry(first);
		}
	}


	protected final static void reLinkBottom(WireHashKey wireKey,
			StateOfLink newState, StateOfLink oldState, long first,TreeMap<Long, AtomicEvent<?>> wire)
			throws StoryStorageException {
		if (newState == null || oldState == null) {
			throw new StoryStorageException("relink null!");
		}
		if (wire == null) {
			throw new StoryStorageException(
					"internal error : storage doesn't contain wire");
		}
		Entry<Long, AtomicEvent<?>> ceilingEntry = wire.ceilingEntry(first);
		while (ceilingEntry != null) {
			AtomicEvent<?> ae = ceilingEntry.getValue();
			first = ceilingEntry.getKey();
			correctLinkOnWire(oldState, newState, ae);
			ceilingEntry = wire.higherEntry(first);
		}
	}
	


	private static void addWireForRelinking(HashSet<WireHashKey> set,
			StateOfLink sl) {
		WireHashKey e = new WireHashKey(sl.getAgentId(), sl.getSiteName(),
				TypeOfWire.LINK_STATE);
		set.add(e);
	}
	
	public static final void reLinkTop(StateOfLink newState,
			StateOfLink oldState, long first,TreeMap<Long, AtomicEvent<?>> wire) throws StoryStorageException {
		if (newState == null || oldState == null) {
			throw new StoryStorageException("relink null!");
		}

		if (wire == null) {
			throw new StoryStorageException(
					"internal error : storage doesn't contain wire");
		}
		Entry<Long, AtomicEvent<?>> lowerEntry = wire.floorEntry(first);

		while (lowerEntry != null) {
			AtomicEvent<?> ae = lowerEntry.getValue();
			first = lowerEntry.getKey();
			correctLinkOnWire(oldState, newState, ae);

			lowerEntry = wire.lowerEntry(first);
		}
	}

	public static final void correctLinkOnWire(StateOfLink oldState,
			StateOfLink newState, AtomicEvent<?> ae) {
		Object beforeState = ae.getState().getBeforeState();
		if (beforeState != null) {
			if (((StateOfLink) beforeState).equals(oldState))
				((StateOfLink) beforeState).setState(newState);

		}

		Object afterState = ae.getState().getAfterState();
		if (afterState != null) {
			if (((StateOfLink) afterState).equals(oldState)) {
				((StateOfLink) afterState).setState(newState);
			}

		}
	}

	public static final void reLinkBottom(
			StateOfLink newState, StateOfLink oldState, long first,TreeMap<Long, AtomicEvent<?>> wire)
			throws StoryStorageException {
		if (newState == null || oldState == null) {
			throw new StoryStorageException("relink null!");
		}
		if (wire == null) {
			throw new StoryStorageException(
					"internal error : storage doesn't contain wire");
		}
		Entry<Long, AtomicEvent<?>> ceilingEntry = wire.ceilingEntry(first);
		while (ceilingEntry != null) {
			AtomicEvent<?> ae = ceilingEntry.getValue();
			first = ceilingEntry.getKey();
			correctLinkOnWire(oldState, newState, ae);
			ceilingEntry = wire.higherEntry(first);
		}
	}

}
