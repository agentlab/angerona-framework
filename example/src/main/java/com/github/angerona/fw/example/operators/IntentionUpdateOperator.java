package com.github.angerona.fw.example.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.Intention;
import com.github.angerona.fw.PlanElement;
import com.github.angerona.fw.Subgoal;
import com.github.angerona.fw.am.secrecy.operators.BaseIntentionUpdateOperator;
import com.github.angerona.fw.am.secrecy.operators.BaseViolatesOperator;
import com.github.angerona.fw.am.secrecy.operators.ViolatesResult;
import com.github.angerona.fw.am.secrecy.operators.parameter.PlanParameter;
import com.github.angerona.fw.operators.OperatorCallWrapper;
import com.github.angerona.fw.operators.parameter.EvaluateParameter;

/**
 * 	The default implementation of the IntentionUpdate operation searches the 
 * 	PlanComponent for the first action. The first stack in the collection
 * 	containing an action on top provides the action which is performed.
 * 	Therefore this implementation is order dependent.
 * 
 * 	The caller of the operator can use the setting 'allowUnsafe' to turn 
 * 	violation checking on/off.
 * 
 * 	@author Tim Janus
 */
public class IntentionUpdateOperator extends BaseIntentionUpdateOperator {

	/** reference to the logback instance used for logging */
	private static Logger LOG = LoggerFactory.getLogger(IntentionUpdateOperator.class);
	
	@Override
	protected PlanElement processImpl(PlanParameter param) {
		LOG.info("Run Default-Intention-Update");
		for(Subgoal plan : param.getActualPlan().getPlans()) {
			for(int i=0; i<plan.getNumberOfStacks(); ++i) {
				PlanElement pe = plan.peekStack(i);
				if(check(param, pe)) {
					return pe;
				}
			}
		}
		param.report("No atomic step candidate found.");
		return null;
	}

	protected boolean check(PlanParameter param, PlanElement pe) {
		Intention intention = pe.getIntention();
		Agent ag = param.getAgent();
		if(pe.getIntention().isAtomic()) {
			
			if(intention.isAtomic()) {
				boolean select = Boolean.parseBoolean(param.getSetting("allowUnsafe", String.valueOf(false)));
				
				if(!select) {
					OperatorCallWrapper op = ag.getOperators().getPreferedByType(BaseViolatesOperator.OPERATION_NAME);
					EvaluateParameter eparam = new EvaluateParameter(ag, ag.getBeliefs(), pe);
					select = ((ViolatesResult)op.process(eparam)).isAlright();
					if(select) {
						param.report("Mental action successful, using '" + intention.toString() + "' as next atomic action.");
					}
				}
				
				return select;
			}
		}
		return false;
	}
}
