package com.plectix.simulator.interfaces;

import com.plectix.simulator.components.CStoriesSiteStates.StateType;

public interface IStoriesSiteStates {

	public void addInformation(StateType index, IStoriesSiteStates siteStates);

	public IStates getLastState();

	public IStates getCurrentState();

}
