package com.plectix.simulator.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.plectix.simulator.interfaces.ConnectedComponentInterface;
import com.plectix.simulator.interfaces.ObservableConnectedComponentInterface;
import com.plectix.simulator.interfaces.ObservableInterface;
import com.plectix.simulator.simulator.SimulationArguments;
import com.plectix.simulator.simulator.SimulationUtils;
import com.plectix.simulator.util.ObservableState;

/**
 * This class describes observables storage.
 * This is the set of objects, which we want to keep an eye on during the simulation.
 * 
 * In fact such objects are rules and substances.
 * 
 * @see ObservableConnectedComponent
 * @see ObservableRuleComponent
 * @author evlasov
 *
 */
@SuppressWarnings("serial")
public class Observables implements Serializable {
	private boolean ocamlStyleObsName = false;
	private boolean unifiedTimeSeriesOutput = false;
	private final List<ObservableState> countTimeList = new ArrayList<ObservableState>();;
	private List<ObservableConnectedComponentInterface> connectedComponentList 
				= new ArrayList<ObservableConnectedComponentInterface>();;
	private List<ObservableInterface> componentList = new ArrayList<ObservableInterface>();
	private double timeNext;
	private double timeSampleMin;
	private double initialTime = 0.0;
	private long events = -1;
	private int points = -1;
	private double lastTime;
	private boolean changeLastTime = false;
	private boolean changeTimeNext = false;
	private List<ObservableInterface> componentListForXMLOutput = null;
	private boolean flag = false;

	/**
	 * This method initializes CObservables within external parameters 
	 * @param fullTime total time of simulation 
	 * @param initialTime time starting point
	 * @param events number of events
	 * @param points precision (number of points) of observables state graphic
	 * @param isTime <tt>true</tt>, if we have to save an information about current time and 
	 * observables list, otherwise <tt>false</tt>
	 */
	public final void init(double fullTime, double initialTime, long events,
			int points, boolean isTime) {
		timeSampleMin = 0.;
		timeNext = 0.;
		this.initialTime = initialTime;
		this.events = events;
		this.points = points;

		if (isTime) {
			if (initialTime > 0.0) {
				timeNext = initialTime;
				fullTime = fullTime - timeNext;
			} else
				timeNext = timeSampleMin;
			this.initializeMinSampleTime(fullTime, points);
//			timeNext += timeSampleMin;
		} else {
			this.initializeMinSampleTime(events, points);
			if (initialTime <= 0.0)
				timeNext = timeSampleMin;
		}

	}
	
	/**
	 * This method resets all the information in CObservables. We use it when we've got
	 * more than on simulation in one time.
	 */
	public final void resetLists() {
		countTimeList.clear();
		componentListForXMLOutput = null;
		componentList.clear();
		connectedComponentList.clear();
	}
	
	/**
	 * This method returns minimal difference between two time points in graphic 
	 * for observables, i.e. precision.
	 * @return minimal difference between two time points in graphic for observables.
	 */
	public final double getTimeSampleMin() {
		return timeSampleMin;
	}

	/**
	 * This method returns list of time-points on "x" axis in graphic for observables.
	 * @return list of time-points on "x" axis in graphic for observables.
	 */
	public final List<ObservableState> getCountTimeList() {
		return countTimeList;
	}

	/**
	 * This method sets default value for the minimal difference of two time points on
	 * the "x" axis in graphic for observables.
	 * @param simulationParameter is double parameter vary on the simulation type 
	 * @param points is the quantity of points on the "x" axis in observables graphic
	 */
	private final void initializeMinSampleTime(double simulationParameter, int points) { 
		if (points == -1) {
			timeSampleMin = (simulationParameter / SimulationArguments.DEFAULT_NUMBER_OF_POINTS);
		} else {
			timeSampleMin = (simulationParameter / points);
		}
	}

	/**
	 * This method handles observables in current time/event. 
	 * If there's need to save information about observables state (quantity, activity, etc), 
	 * it saves it.
	 * @param time current time
	 * @param count current event number
	 * @param isTime <tt>true</tt>, if we have to save an information about current time and 
	 * observables list, otherwise <tt>false</tt>
	 */
	public final void calculateObs(double time, long count, boolean isTime) {
		int size = countTimeList.size();

		if ((size > 0)
				&& (Math.abs(countTimeList.get(size - 1).getTime() - time) < 1e-16)) {
			if (isTime) {
				calculateAll(true);
			} else {
				updateLastValueAll(time);
				calculateAll(false);
				addToCountTimeList(time, count);
				changeLastTime = false;
			}
			return;
		}

		if (isCalculateNow(time, count, isTime)) {
			if (flag){
				addToCountTimeList(initialTime, count);
				calculateAll(false);
				lastTime = initialTime;
				changeLastTime = false;
				flag = false;
			}
			// if (time >= timeNext) {
			timeNext += timeSampleMin;
			if (!changeLastTime) {
				updateLastValueAll(time);
			}
			addToCountTimeList(lastTime, count);
			calculateAll(false);
			lastTime = time;
			changeLastTime = false;
			return;
		}
		updateLastValueAll(time);
	}

	/**
	 * This method indicates if we have to save an information about current time and 
	 * observables list
	 * @param time current time
	 * @param count is the number of current simulation event
	 * @param isTime true if we run "time" simulation
	 * @return <tt>true</tt>, if we have to save an information about current time and 
	 * observables list, otherwise <tt>false</tt>
	 */
	private final boolean isCalculateNow(double time, long count, boolean isTime) {
		if (isTime) {
			if (initialTime > 0)
				if ((!changeTimeNext) && (initialTime < time)) {
					changeTimeNext = true;
					flag = true;
					updateLastValueAll(time);
				} 
			if (time >= timeNext)
				return true;
		} else {
			if (initialTime > 0)
				if ((!changeTimeNext) && (initialTime < time)) {
					long fullTime = events - count;
					this.initializeMinSampleTime(fullTime, points);
					timeNext = count + timeSampleMin;
					updateLastValueAll(time);
					addToCountTimeList(time, count);
					calculateAll(false);
					changeTimeNext = true;
				}
			if (time < initialTime)
				return false;
			if (count >= timeNext)
				return true;
		}

		return false;
	}

	/**
	 * This method saves current observables state (quantity, activity, etc).  
	 * @param replaceLast <tt>true</tt> if we need to overwrite the latest information, or <tt>false</tt>
	 * if we don't 
	 */
	private final void calculateAll(boolean replaceLast) {
		for (ObservableInterface cc : componentList) {
			cc.fixState(replaceLast);
		}
	}

	/**
	 * This method handles information on observables in the latest moment of simulation 
	 * @param time current time
	 */
	private final void updateLastValueAll(double time) {
		for (ObservableInterface cc : componentList) {
			cc.updateLastValue();
		}
		lastTime = time;
		changeLastTime = true;
	}

	/**
	 * This method saves observables state (quantity, activity, etc) in the latest moment of simulation.  
	 * @param time current time
	 */
	public final void calculateObsLast(double time, long event) {
		int size = countTimeList.size();
		if (size == 0)
			return;
		if (Math.abs(countTimeList.get(size - 1).getTime() - time) < 1e-16)
			return;
		updateLastValueAll(time);
		addToCountTimeList(time, event);
		calculateAll(false);
	}
	
	private void addToCountTimeList(double time, long event){
		if(unifiedTimeSeriesOutput)
			countTimeList.add(new ObservableState(time, event));
		else
			countTimeList.add(new ObservableState(time));
	}

	//--------------------------ADDERS------------------------------------------
	
	/**
	 * This method creates observable component using list of connected 
	 * components from solution
	 * @param list list of connected componetns from solution
	 * @param name name of the observable
	 * @param line kappa file line, which describes this observable component
	 * @param observableId observable id
	 */
	public final void addConnectedComponents(List<ConnectedComponentInterface> list,
			String name, String line, int observableId) {
		boolean unique;
		if (list.size() > 1)
			unique = false;
		else
			unique = true;
		if (ocamlStyleObsName) {
			line = SimulationUtils.printPartRule(list, ocamlStyleObsName);
		}

		for (ConnectedComponentInterface component : list) {
			ObservableConnectedComponentInterface oCC = new ObservableConnectedComponent(
					component.getAgents(), name, line, observableId, unique);
			oCC.initSpanningTreeMap();
			connectedComponentList.add(oCC);
			componentList.add(oCC);
		}
	}

	/**
	 * This method returns adds Observable-rule from given collection of rules by it's name
	 * @param name name of the rule
	 * @param observableRuleID new observable rule id
	 * @param rules list of rules
	 * @return <tt>true</tt> if we've founded and added such rule to observables, otherwise <tt>false</tt>
	 */
	public final boolean addRulesName(String name, int observableRuleID,
			List<Rule> rules) {
		for (Rule rule : rules) {
			if ((rule.getName() != null) && (rule.getName().equals(name))) {
				ObservableRuleComponent obsRC = new ObservableRuleComponent(
						rule, observableRuleID);
				componentList.add(obsRC);
				return true;
			}
		}

		return false;
	}

	/**
	 * For each observable component this method calculates whether it's unique  
	 * canonical representative or not and saves this information to the component
	 */
	public final void checkAutomorphisms() {
		for (ObservableConnectedComponentInterface oCC : connectedComponentList) {
			if (oCC.getMainAutomorphismNumber() == ObservableConnectedComponent.NO_INDEX) {
				for (ObservableConnectedComponentInterface oCCIn : connectedComponentList) {
					if (!(oCC == oCCIn)
							&& oCCIn.getMainAutomorphismNumber() == ObservableConnectedComponent.NO_INDEX) {
						if (oCC.getAgents().size() == oCCIn.getAgents().size())
							if (oCC.isAutomorphicTo(oCCIn.getAgents().get(0))) {
								int index = connectedComponentList.indexOf(oCC);
								oCC.addAutomorphicObservables(index);
								oCCIn.setMainAutomorphismNumber(index);
							}
					}
				}
			}
		}
	}
	
	//------------------------GETTERS AND SETTERS------------------------------
	
	public final List<ObservableInterface> getComponentList() {
		return componentList;
	}
	
	public final List<ObservableInterface> getUniqueComponentList() {
		if (componentListForXMLOutput == null) {
			Set<Integer> set = new LinkedHashSet<Integer>();
			List<ObservableInterface> list = new ArrayList<ObservableInterface>();
			for (ObservableInterface cc : componentList) {
				if (!set.contains(cc.getId())) {
					set.add(cc.getId());
					list.add(cc);
				}
			}
			componentListForXMLOutput = list;
		}
		return componentListForXMLOutput;
	}

	public final List<ObservableConnectedComponentInterface> getConnectedComponentList() {
		return connectedComponentList;
	}

	public final List<ObservableConnectedComponentInterface> getConnectedComponentListForXMLOutput() {
		Set<Integer> map = new LinkedHashSet<Integer>();
		List<ObservableConnectedComponentInterface> list = new LinkedList<ObservableConnectedComponentInterface>();
		for (ObservableConnectedComponentInterface cc : connectedComponentList) {
			if (!map.contains(cc.getId())) {
				map.add(cc.getId());
				list.add(cc);
			}
		}
		return list;
	}
	
	public final void setOcamlStyleObsName(boolean ocamlStyleObsName) {
		this.ocamlStyleObsName = ocamlStyleObsName;
	}

	public final void setUnifiedTimeSeriesOutput(boolean unifiedTimeSeriesOutput) {
		this.unifiedTimeSeriesOutput = unifiedTimeSeriesOutput;
	}
	
}
