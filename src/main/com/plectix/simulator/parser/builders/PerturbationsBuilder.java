package com.plectix.simulator.parser.builders;

import java.util.ArrayList;
import java.util.List;

import com.plectix.simulator.components.CAgent;
import com.plectix.simulator.components.CRule;
import com.plectix.simulator.components.perturbations.CPerturbation;
import com.plectix.simulator.components.perturbations.CRulePerturbation;
import com.plectix.simulator.components.perturbations.RateExpression;
import com.plectix.simulator.interfaces.IConnectedComponent;
import com.plectix.simulator.interfaces.IObservablesComponent;
import com.plectix.simulator.interfaces.IPerturbationExpression;
import com.plectix.simulator.parser.abstractmodel.AbstractPerturbation;
import com.plectix.simulator.parser.abstractmodel.perturbations.LinearExpressionMonome;
import com.plectix.simulator.parser.abstractmodel.perturbations.conditions.AbstractSpeciesCondition;
import com.plectix.simulator.parser.abstractmodel.perturbations.conditions.AbstractTimeCondition;
import com.plectix.simulator.parser.abstractmodel.perturbations.modifications.AbstractOnceModification;
import com.plectix.simulator.parser.abstractmodel.perturbations.modifications.AbstractRateModification;
import com.plectix.simulator.parser.abstractmodel.perturbations.modifications.ModificationType;
import com.plectix.simulator.parser.exceptions.DocumentFormatException;
import com.plectix.simulator.parser.exceptions.ParseErrorException;
import com.plectix.simulator.simulator.KappaSystem;
import com.plectix.simulator.simulator.SimulationArguments;
import com.plectix.simulator.simulator.SimulationData;
import com.plectix.simulator.simulator.SimulationUtils;

public class PerturbationsBuilder {
	private final SubstanceBuilder mySubstanceBuilder;
	private final SimulationArguments myArguments;
	private final KappaSystem myData;

	public PerturbationsBuilder(SimulationData data) {
		myData = data.getKappaSystem();
		myArguments = data.getSimulationArguments();
		mySubstanceBuilder = new SubstanceBuilder(myData);
	}

	public List<CPerturbation> build(List<AbstractPerturbation> arg)
			throws ParseErrorException, DocumentFormatException {
		List<CPerturbation> result = new ArrayList<CPerturbation>();
		for (AbstractPerturbation perturbation : arg) {
			List<CPerturbation> res = convert(perturbation);
			if (res != null) {
				result.addAll(res);
			}
		}
		return result;
	}

	// TODO throw document format exception
	private CRule findRule(String name) {
		if (name == null) {
			return null;
		}

		for (CRule rule : myData.getRules()) {
			if (name.equals(rule.getName())) {
				return rule;
			}
		}
		return null;
	}

	private List<IPerturbationExpression> createRateExpression(
			AbstractRateModification modification) {
		List<IPerturbationExpression> result = new ArrayList<IPerturbationExpression>();
		for (LinearExpressionMonome monome : modification.getExpression()
				.getPolynome()) {
			CRule foundedRule = findRule(monome.getObsName());
			double multiplier = monome.getMultiplier();
			result.add(new RateExpression(foundedRule, multiplier));
		}
		return result;
	}

	private List<CPerturbation> convert(AbstractPerturbation arg)
			throws ParseErrorException, DocumentFormatException {
		ModificationType modificationType = arg.getModification().getType();
		List<CPerturbation> result = new ArrayList<CPerturbation>();
		boolean rateModification = (modificationType == ModificationType.RATE);
		boolean addOnceModification = (modificationType == ModificationType.ADDONCE);

		// TODO worry about type conversion?
		switch (arg.getCondition().getType()) {
		case TIME: {
			AbstractTimeCondition condition = (AbstractTimeCondition) arg
					.getCondition();
			double timeBound = condition.getBounds();

			if (rateModification) {
				AbstractRateModification modification = (AbstractRateModification) arg
						.getModification();
				result.add(new CPerturbation(timeBound,
						findRule(modification
								.getArgument()),
						createRateExpression(modification)));
				return result;
			} else {
				AbstractOnceModification modification = (AbstractOnceModification) arg
						.getModification();
				List<CAgent> agentList = mySubstanceBuilder
						.buildAgents(modification.getSubstance());
				List<IConnectedComponent> ccList = SimulationUtils
						.buildConnectedComponents(agentList);
				
				for (IConnectedComponent cc : ccList) {
					List<IConnectedComponent> ccL = new ArrayList<IConnectedComponent>();
					ccL.add(cc);
					CRulePerturbation rule;
					if (addOnceModification) {
						rule = new CRulePerturbation(null, ccL, "", 0,
								(int) myData.generateNextRuleId(), myArguments.isStorify());
					} else {
						rule = new CRulePerturbation(ccL, null, "", 0,
								(int) myData.generateNextRuleId(), myArguments.isStorify());
					}
					rule.setCount(modification.getQuantity());
					myData.addRule(rule);
					result.add(new CPerturbation(timeBound,
							rule));
				}
				return result;
			}
		}
		case SPECIES: {
			AbstractSpeciesCondition condition = (AbstractSpeciesCondition) arg
					.getCondition();
			if (rateModification) {
				AbstractRateModification modification = (AbstractRateModification) arg
						.getModification();

				// old code "conversion"
				List<IObservablesComponent> obsID = new ArrayList<IObservablesComponent>();
				List<Double> parameters = new ArrayList<Double>();
				double freeTerm = 0;
				boolean freeTermNotZero = false;
				for (LinearExpressionMonome monome : condition.getExpression()
						.getPolynome()) {
					if (monome.getObsName() != null) {
						obsID.add(checkInObservables(monome.getObsName()));
						parameters.add(monome.getMultiplier());
					} else {
						freeTerm += monome.getMultiplier();
						freeTermNotZero = true;
					}
				}

				if (freeTermNotZero) {
					parameters.add(freeTerm);
				}
				CRule rule = findRule(modification.getArgument());
				IObservablesComponent component = checkInObservables(condition
						.getArgument());
				result.add(new CPerturbation(obsID, parameters, component
						.getId(), 
						rule, condition.isGreater(), createRateExpression(modification), 
						myData.getObservables()));
				return result;
			} else {
//				myData.addInfo(InfoType.OUTPUT, InfoType.WARNING, 
//						"WARNING - We cannot use species condition with 'ONCE' modification");
//				throw new ParseErrorException("We cannot use species condition with 'ONCE' modification");
				// TODO we've not implemented this feature :-(
			}
		}
		}
		return null;
	}

	private final IObservablesComponent checkInObservables(String obsName)
			throws DocumentFormatException {
		for (IObservablesComponent cc : myData.getObservables()
				.getComponentList()) {
			if ((cc.getName() != null) && (cc.getName().equals(obsName))) {
				return cc;
			}
		}
		throw new DocumentFormatException("'" + obsName
				+ "' must be in observables!");
	}
}
