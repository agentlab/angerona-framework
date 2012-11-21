package angerona.fw.gui.asp;

import java.util.List;

import javax.swing.DefaultListModel;

import net.sf.tweety.logicprogramming.asplibrary.syntax.Literal;
import net.sf.tweety.logicprogramming.asplibrary.util.AnswerSet;
import angerona.fw.gui.view.BeliefbaseView;
import angerona.fw.logic.asp.AspBeliefbase;
import angerona.fw.logic.asp.AspReasoner;

/**
 * This class extends the default Beliefbase View to an ASP specific belief base
 * view. It outputs the answer sets for a specific reasoner using the ASPReasoner
 * interface specific processAnswerSets method.
 * It outputs the answer sets in the middle between the ELP (belief base) and
 * the set of inferred formulas.
 * @author Tim Janus
 */
public class AspBeliefbaseView extends BeliefbaseView {

	/** kill warning */
	private static final long serialVersionUID = -9197437532805094834L;

	@Override
	public Class<?> getObservationObjectType() {
		return AspBeliefbase.class;
	}
	
	@Override
	protected void update(DefaultListModel<ListElement> model) {
		if(ref == null)	return;
		
		// First output the belief base content (using super class behavior)
		updateBeliefbaseOutput(model);
		
		// Second: Process Answer sets 
		AspBeliefbase bAct = (AspBeliefbase)actual;
		if(! (bAct.getReasoningOperator() instanceof AspReasoner)) {
			return;
		}
		AspReasoner reasoner = (AspReasoner)bAct.getReasoningOperator();

		model.addElement(new ListElement(" ", ListElement.ST_NOTCHANGED));
		model.addElement(new ListElement("--- Answerset Result using: " + 
		reasoner.getNameAndParameters(), ListElement.ST_RESERVED));
		
		List<AnswerSet> answerSets = reasoner.processAnswerSets(bAct);
		
		// Output the answer sets to the JList.
		int counter = 1;
		for(AnswerSet as : answerSets) {
			model.addElement(new ListElement("Answer Set " + counter + "/" + answerSets.size(), 
				ListElement.ST_NOTCHANGED));
			
			String output = "";
			for(Literal l : as.getLiterals()) {
				output += ", " + l;
				if(output.length() > 100) {
					output = output.substring(2);
					model.addElement(new ListElement(output, ListElement.ST_NOTCHANGED));
				}
			}
			if(output.length() != 0) {
				output = output.substring(2);
				model.addElement(new ListElement(output, ListElement.ST_NOTCHANGED));
			}
			
			counter += 1;
			if(counter <= answerSets.size()) {
				model.addElement(new ListElement(" ", ListElement.ST_NOTCHANGED));
			}
		}
		
		// Third: Output the inferred knowledge (using the super class method).
		updateInferenceOutput(model);
	}
}
