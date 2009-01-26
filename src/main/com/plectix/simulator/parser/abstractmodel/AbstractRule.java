package com.plectix.simulator.parser.abstractmodel;

import java.util.*;

import com.plectix.simulator.components.CConnectedComponent;
import com.plectix.simulator.interfaces.IConnectedComponent;

public class AbstractRule implements IAbstractComponent {
	private final String myName;
	private final List<IConnectedComponent> myLHS;
	private final List<IConnectedComponent> myRHS;
	private final double myRate;
	private final int myId;
	public static final IConnectedComponent EMPTY_LHS_CC = new CConnectedComponent(
			CConnectedComponent.EMPTY);
	
	public AbstractRule(List<IConnectedComponent> left,
			List<IConnectedComponent> right, String name,
			double ruleRate, int ruleID, boolean isStorify) {
		myRate = ruleRate;
		if (left == null) {
			myLHS = new ArrayList<IConnectedComponent>();
			myLHS.add(EMPTY_LHS_CC);
		} else {
			myLHS = left;
		}
		
		if (right == null) {
			myRHS = new ArrayList<IConnectedComponent>();
			myRHS.add(EMPTY_LHS_CC);
		} else {
			myRHS = right;
		}
		myName = name;
		myId = ruleID;
	}
	
	public String getName() {
		return myName;
	}
	
	public double getRate() {
		return myRate;
	}
	
	// uwaga! we won't wrap collections here, 'cos these are "temporary" classes  
	public List<IConnectedComponent> getRHS() {
		return myRHS;
	}
	
	public List<IConnectedComponent> getLHS() {
		return myLHS;
	}

	public Integer getID() {
		return myId;
	}
}