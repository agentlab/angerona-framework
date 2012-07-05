package angerona.fw.operators.def;

import java.util.Map;
import java.util.Set;

import net.sf.tweety.logics.firstorderlogic.syntax.FolFormula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import angerona.fw.BaseBeliefbase;
import angerona.fw.comm.Answer;
import angerona.fw.logic.ConfidentialKnowledge;
import angerona.fw.logic.Secret;
import angerona.fw.operators.BaseViolatesOperator;
import angerona.fw.operators.parameter.ViolatesParameter;

/**
 * This class is capable of proofing if the applying of an answer
 * action violates confidentially.
 *
 * For every other action type the default violates operator returns
 * false.
 * @author Tim Janus
 */

public class ViolatesOperator extends BaseViolatesOperator {
	
	/** reference to the logback instance used for logging */
	private static Logger LOG = LoggerFactory.getLogger(ViolatesOperator.class);
	
	@Override
	protected Boolean processInt(ViolatesParameter param) {
		LOG.info("Run Default-ViolatesOperator");
		
		// only apply violates if confidential knowledge is saved in agent.
		ConfidentialKnowledge conf = param.getAgent().getComponent(ConfidentialKnowledge.class);
		if(conf == null)
			return new Boolean(false);
		
		//JOptionPane.showMessageDialog(null, param.getAction());
		if(param.getAction() instanceof Answer) {
			Answer a = (Answer) param.getAction();
			Map<String, BaseBeliefbase> views = param.getBeliefs().getViewKnowledge();
			BaseBeliefbase origView = (BaseBeliefbase) views.get(a.getReceiverId());
			BaseBeliefbase view = (BaseBeliefbase) origView.clone(); 
			view.addKnowledge(a);
			
			if(views.containsKey(a.getReceiverId())) {
				for(String changeOp : conf.getTargetsByChangeOperator().keySet()) {
					
					//Does it even make sense to go through all confidential targets,
					//given how it's making false positives right now?
					Set<FolFormula> origInfere = origView.infere();
					Set<FolFormula> cloneInfere = view.infere();
					for(Secret secret : conf.getTargetsByChangeOperator().get(changeOp)) {
						if(secret.getSubjectName().equals(a.getReceiverId())) {
							//LOG.info(id + " Found CF=" + ct + " and answer=" + aa);
							boolean inOrig = origInfere.contains(secret.getInformation());
							if(inOrig) {
								LOG.warn("The secret '{}' is already in the original view.", secret.toString());
								continue;
							}
							
							boolean inClone = cloneInfere.contains(secret.getInformation());
							if(	inClone )  {
								report("Confidential-Target: '" + secret + "' of '" + param.getAgent().getName() + "' injured by: '" + param.getAction() + "'", view);
								return new Boolean(true);
							}
						}
					}
				}
			}
		}
		report("No violation applying the action: '" + param.getAction() + "'", param.getAgent());
		return new Boolean(false);
	}
}
