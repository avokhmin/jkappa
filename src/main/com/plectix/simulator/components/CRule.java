package com.plectix.simulator.components;

import java.util.List;


public class CRule {

	private List<CConnectedComponent> left;
	private List<CConnectedComponent> right;
	private Double activity;

	public Double getActivity() {
		return activity;
	}

	public void setActivity(Double activity) {
		this.activity = activity;
	}

	public CRule(List<CConnectedComponent> left, List<CConnectedComponent> right, Double activity) {
		this.left = left;
		this.right = right;
		this.activity = activity;
	}

	public List<CConnectedComponent> getLeft() {
		return left;
	}

	public List<CConnectedComponent> getRight() {
		return right;
	}
}