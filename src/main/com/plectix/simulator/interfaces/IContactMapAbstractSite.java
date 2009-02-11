package com.plectix.simulator.interfaces;

import java.util.Collection;

import com.plectix.simulator.components.contactMap.CContactMapLinkState;

public interface IContactMapAbstractSite extends IAbstractSite{
	
	public boolean includedInCollection(Collection<IContactMapAbstractSite> collection);
	
	public IContactMapAbstractAgent getAgentLink();

	public boolean equalz(IAbstractSite obj);

	public CContactMapLinkState getLinkState();

	public void setAgentLink(IContactMapAbstractAgent linkAgent);
	
	public boolean equalsNameId(IContactMapAbstractSite site);
	
	public boolean equalsLinkAgent(IContactMapAbstractSite site);
	
	public boolean equalsInternalState(IContactMapAbstractSite site);
	
	public boolean equalsLinkState(IContactMapAbstractSite site);
}
