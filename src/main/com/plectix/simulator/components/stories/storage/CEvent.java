package com.plectix.simulator.components.stories.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.plectix.simulator.components.CInternalState;
import com.plectix.simulator.components.CSite;
import com.plectix.simulator.components.stories.enums.EActionOfAEvent;
import com.plectix.simulator.components.stories.enums.EMarkOfEvent;
import com.plectix.simulator.components.stories.enums.EState;
import com.plectix.simulator.components.stories.enums.ETypeOfWire;

public class CEvent implements ICEvent {
	public final static boolean BEFORE_STATE = true;
	public final static boolean AFTER_STATE = false;
	
	private EMarkOfEvent mark = null;

	// filter with non bound/free wires
	private ArrayList<WireHashKey> filter = new ArrayList<WireHashKey>();

	// map with all wires which touched by this event
	private LinkedHashMap<WireHashKey, AtomicEvent<?>> eventsMap = new LinkedHashMap<WireHashKey, AtomicEvent<?>>();

	private final long stepId;

	// event symbolize applying this rule
	private final int ruleId;

	public CEvent(long stepId, int ruleId) {
		this.ruleId = ruleId;
		this.stepId = stepId;
	}

	public CEvent(long stepId, int ruleId, EMarkOfEvent initialMark) {
		this.ruleId = ruleId;
		this.stepId = stepId;
		this.mark = initialMark;
	}

	public String toString() {
		return new String("mark: " + mark + " stepId: " + stepId + " ruleId: "
				+ ruleId);
	}

	public void addAtomicEvent(WireHashKey key, CSite site,
			EActionOfAEvent type, boolean isBefore) {
		switch (key.getTypeOfWire()) {
		case AGENT:
			addEventAgent(key, type, isBefore);
			break;
		case BOUND_FREE:
			addEventBoundFree(key, site, type, isBefore);
			break;
		case INTERNAL_STATE:
			addEventInternalState(key, site, type, isBefore);
			break;
		case LINK_STATE:
			addEventLinkState(key, site, type, isBefore);
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void addEventLinkState(WireHashKey key, CSite site,
			EActionOfAEvent type, boolean isBefore) {
		AtomicEvent<CStateOfLink> event = (AtomicEvent<CStateOfLink>) eventsMap
				.get(key);
		if (event == null) {
			addToFilter(key);
			event = new AtomicEvent<CStateOfLink>(this, type);
			eventsMap.put(key, event);
		} else
			event.correctingType(type);

		CSite connectedSite = site.getLinkState().getConnectedSite();
		if (isBefore)
			if (site.getLinkState().getConnectedSite() == null)
				event.getState().setBeforeState(new CStateOfLink());
			else
				event.getState().setBeforeState(
						new CStateOfLink(connectedSite.getParentAgent().getId(),
								connectedSite.getNameId()));
		else if (site.getLinkState().getConnectedSite() == null)
			event.getState().setAfterState(new CStateOfLink());
		else
			event.getState().setAfterState(
					new CStateOfLink(connectedSite.getParentAgent().getId(),
							connectedSite.getNameId()));
	}

	@SuppressWarnings("unchecked")
	private void addEventInternalState(WireHashKey key, CSite site,
			EActionOfAEvent type, boolean isBefore) {
		if (site.getInternalState().getNameId() == CInternalState.EMPTY_STATE
				.getNameId())
			return;

		AtomicEvent<Integer> event = (AtomicEvent<Integer>) eventsMap.get(key);
		if (event == null) {
			addToFilter(key);
			event = new AtomicEvent<Integer>(this, type);
			eventsMap.put(key, event);
		} else
			event.correctingType(type);

		if (isBefore)
			event.getState()
					.setBeforeState(site.getInternalState().getNameId());
		else
			event.getState().setAfterState(site.getInternalState().getNameId());
	}

	@SuppressWarnings("unchecked")
	private void addEventBoundFree(WireHashKey key, CSite site,
			EActionOfAEvent type, boolean isBefore) {
		AtomicEvent<EState> event = (AtomicEvent<EState>) eventsMap.get(key);
		if (event == null) {
			addToFilter(key);
			event = new AtomicEvent<EState>(this, type);
			eventsMap.put(key, event);
		} else
			event.correctingType(type);

		if (isBefore)
			if (site.getLinkState().getConnectedSite() == null)
				event.getState().setBeforeState(EState.FREE_LINK_STATE);
			else
				event.getState().setBeforeState(EState.BOUND_LINK_STATE);
		else if (site.getLinkState().getConnectedSite() == null)
			event.getState().setAfterState(EState.FREE_LINK_STATE);
		else
			event.getState().setAfterState(EState.BOUND_LINK_STATE);

	}

	@SuppressWarnings("unchecked")
	private void addEventAgent(WireHashKey key, EActionOfAEvent type,
			boolean isBefore) {
		AtomicEvent<EState> event = (AtomicEvent<EState>) eventsMap.get(key);
		if (event == null) {
			addToFilter(key);
			event = new AtomicEvent<EState>(this, type);
			eventsMap.put(key, event);
		} else
			event.correctingType(type);

		if (isBefore)
			event.getState().setBeforeState(EState.CHECK_AGENT);
		else {
			if (event.getType() != EActionOfAEvent.TEST_AND_MODIFICATION)
				event.getState().setAfterState(EState.CHECK_AGENT);
		}

	}

	public long getStepId() {
		return stepId;
	}

	public int getRuleId() {
		return ruleId;
	}

	public LinkedHashMap<WireHashKey, AtomicEvent<?>> getAtomicEvents() {
		return eventsMap;
	}


	public void setMark(EMarkOfEvent newMark, IWireStorage storage)
			throws StoryStorageException {
		if (newMark == mark)
			throw new StoryStorageException("same mark");

		// there is newMark != unresolved
		if (mark == EMarkOfEvent.UNRESOLVED) {
			shiftNumberOfUnresolvedEventsOnWires(false, storage);
		} else {
			if (newMark == EMarkOfEvent.UNRESOLVED) {
				shiftNumberOfUnresolvedEventsOnWires(true, storage);
			}
		}
		mark = newMark;
	}

	private void shiftNumberOfUnresolvedEventsOnWires(boolean up,
			IWireStorage storage) throws StoryStorageException {
		for (WireHashKey w : eventsMap.keySet()) {
			if (eventsMap.get(w).getType() != EActionOfAEvent.TEST) {
				upNumberOfUnresolvedModifyEvent(w, up, storage);
			}
		}
	}

	public EMarkOfEvent getMark() {
		return mark;
	}

	public AtomicEvent<?> getAtomicEvent(int index)
			throws StoryStorageException {
		WireHashKey wk = filter.get(index);
		if (wk == null)
			throw new StoryStorageException("get atomic event =null", index);
		return eventsMap.get(wk);
	}

	public int getAtomicEventCount() throws StoryStorageException {
		// TODO: refactor
		filter.clear();
		for(WireHashKey wk : eventsMap.keySet()){
			addToFilter(wk);
		}
		return filter.size();
	}

	public ETypeOfWire getAtomicEventType(int index)
			throws StoryStorageException {
		WireHashKey wk = filter.get(index);
		if (wk == null)
			throw new StoryStorageException("get atomic event type", index);

		// TODO comment after good testing
		// if (eventsMap.get(wk).getType() ==
		// EActionOfAEvent.TEST_AND_MODIFICATION
		// && eventsMap.get(wk).getState().getAfterState() == eventsMap
		// .get(wk).getState().getBeforeState()) {
		// throw new StoryStorageException(
		// "states after and before equals on testAndModify event");
		// }
		// if (eventsMap.get(wk).getType() == EActionOfAEvent.TEST
		// && (eventsMap.get(wk).getState().getAfterState() != null || eventsMap
		// .get(wk).getState().getBeforeState() == null))
		// throw new StoryStorageException(
		// "states after!=null or before=null on test event");
		//
		// if (eventsMap.get(wk).getType() == EActionOfAEvent.MODIFICATION
		// && (eventsMap.get(wk).getState().getBeforeState() != null ||
		// eventsMap
		// .get(wk).getState().getAfterState() == null))
		// throw new StoryStorageException(
		// "states after=null or before!=null on onlyModify event  "
		// + this.stepId);

		return wk.getTypeOfWire();
	}

	public WireHashKey getWireKey(int index) throws StoryStorageException {
		if (filter.isEmpty())
			throw new StoryStorageException("filter in CEvent is empty");
		WireHashKey wk = filter.get(index);
		if (wk == null)
			throw new StoryStorageException("get atomic event", index);
		return wk;
	}

	// optimize : LinkedList -> ArrayList
	public IAtomicEventIterator wireEventIterator() {
		List<AtomicEvent<?>> list = new LinkedList<AtomicEvent<?>>();

		for (int i = 0; i < filter.size(); i++) {
			list.add(eventsMap.get(filter.get(i)));
		}
		return new CIteratorAEventWithinEvent(eventsMap);
	}

	public boolean containsWire (WireHashKey wireKey)
	{
		return eventsMap.containsKey(wireKey);
	}
	
	public AtomicEvent<?> getAtomicEvent(WireHashKey wireKey)
			throws StoryStorageException {
		AtomicEvent<?> event = eventsMap.get(wireKey);
		if (event == null) {
			throw new StoryStorageException("getAtomicEvent", wireKey
					.hashCode());
		}
		return event;
	}

	public void removeWire (WireHashKey wKey)
	{
		eventsMap.remove(wKey);
		filter.remove(wKey);
	}
	
	/**
	 * return null if all wire in this event doesn't contain unresolved modify
	 * event
	 * 
	 * @throws StoryStorageException
	 * 
	 */
	public WireHashKey getWireWithMinimumUresolvedEvent(IWireStorage storage)
			throws StoryStorageException {
//		if (filter.isEmpty()) {
//			throw new StoryStorageException("filter is empty"
//					+ this.getStepId());
//		}
		WireHashKey wKey = filter.get(0);
		int n = filter.size();
		int m = getNumberOfUnresolvedModifyEventOnWire(wKey, storage);
		int temp;
		for (int i = 1; i < n; i++) {
			temp = getNumberOfUnresolvedModifyEventOnWire(filter.get(i),
					storage);
			if ((temp != 0 && temp < m) || (m == 0 && temp > 0)) {
				wKey = filter.get(i);
				m = temp;
			}
		}

		if (m > 0) {
			return wKey;
		} else {
			return null;
		}
	}

	public void setMarkUnresolved(IWireStorage storage)
			throws StoryStorageException {
		mark = EMarkOfEvent.UNRESOLVED;
		shiftNumberOfUnresolvedEventsOnWires(true, storage);
	}

	public void addToFilter(WireHashKey key) {
		if (key.getTypeOfWire() == ETypeOfWire.AGENT)
			filter.add(0, key);
		else
			filter.add(key);
	}

	public void setNumberOfModifyEventOnWire(WireHashKey wKey,
			int numberOfUnresolvedEventOnWireN, IWireStorage storage) {
		storage.putUnresolvedModifyEvent(wKey, Integer
				.valueOf(numberOfUnresolvedEventOnWireN));
	}

	public int getNumberOfUnresolvedModifyEventOnWire(WireHashKey wKey,
			IWireStorage storage) throws StoryStorageException {
		return storage.getUnresolvedModifyCount(wKey);
	}

	public void upNumberOfUnresolvedModifyEvent(WireHashKey wKey, boolean up,
			IWireStorage storage) throws StoryStorageException {
		// if(storage.getUnresolved(this)){
		// throw new StoryStorageException("number unresolved in wire");
		// System.out.println();
		// }
		int x = storage.getUnresolvedModifyCount(wKey);

		if (up) {
			x++;
		} else {
			x--;
		}
		if (x < 0)
			throw new StoryStorageException(
					"negative number of unresolved events on wire");

		storage.putUnresolvedModifyEvent(wKey, Integer.valueOf(x));

	}

	public void onlySetMark(EMarkOfEvent newMark) {
		mark = newMark;

	}

	// rebuild event and numberOfUnresolvedevents on wires
	public List<PointRound> exchangeWires(Map<WireHashKey, WireHashKey> map, IWireStorage storage) throws StoryStorageException {

		List<PointRound> list = new LinkedList<PointRound>();

		for (WireHashKey wk : map.keySet()) {
			WireHashKey wkTwo = map.get(wk);
			
			AtomicEvent<?> ae1 = eventsMap.remove(wk);
			AtomicEvent<?> ae2 = eventsMap.remove(wkTwo);
			
			if (ae1 == null && ae2 == null) {
				continue;
			}
			if (ae1 == null) {
				eventsMap.put(wk, ae2);

			} else {
				if (ae2 == null) {
					eventsMap.put(wkTwo, ae1);

				}
				else{
					eventsMap.put(wkTwo, ae1);
					eventsMap.put(wk, ae2);
				}
			}

			PointRound pr = new PointRound();
			pr.number = stepId;
			pr.wk1 = wk;
			pr.wk2 = wkTwo;
			list.add(pr);

			int dif = 0;
			if(mark==EMarkOfEvent.UNRESOLVED){
				if (eventsMap.get(wk)!=null&&eventsMap.get(wk).getType()!=EActionOfAEvent.TEST){
					dif++;
				}
				if (eventsMap.get(wkTwo)!=null&&eventsMap.get(wkTwo).getType()!=EActionOfAEvent.TEST){
					dif--;
				}
				if(dif>0){
					upNumberOfUnresolvedModifyEvent(wk, true,storage);
					upNumberOfUnresolvedModifyEvent(wkTwo, false,storage);
				}

				if(dif<0){
					upNumberOfUnresolvedModifyEvent(wk, false,storage);
					upNumberOfUnresolvedModifyEvent(wkTwo, true,storage);
				}
		
			}			
		}
		filter.clear();
		for(WireHashKey wk : eventsMap.keySet()){
			addToFilter(wk);
		}
		return list;

	}
	
	private void updateCounts(boolean b, boolean c, WireHashKey wk1,
			WireHashKey wk2, IWireStorage storage) throws StoryStorageException {
		
		if(mark!=EMarkOfEvent.UNRESOLVED){
			return;
		}
		if(b&&!c){
			if(eventsMap.get(wk1).getType()!=EActionOfAEvent.TEST){
				upNumberOfUnresolvedModifyEvent(wk1, true,storage);
				upNumberOfUnresolvedModifyEvent(wk2, false, storage);
				
			}
			
		}
		if(!b&&c){
			if(eventsMap.get(wk2).getType()!=EActionOfAEvent.TEST){
				upNumberOfUnresolvedModifyEvent(wk2, true, storage);
				upNumberOfUnresolvedModifyEvent(wk1, false, storage);		
			}

			
			
		}
		if(!b&&!c){
			if(eventsMap.get(wk2).getType()!=EActionOfAEvent.TEST){
				upNumberOfUnresolvedModifyEvent(wk2, true, storage);
				upNumberOfUnresolvedModifyEvent(wk1, false, storage);		
			}
			if(eventsMap.get(wk1).getType()!=EActionOfAEvent.TEST){
				upNumberOfUnresolvedModifyEvent(wk1, true,storage);
				upNumberOfUnresolvedModifyEvent(wk2, false, storage);
				
			}		
		}
			
	}

	
}
