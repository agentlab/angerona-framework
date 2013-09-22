package com.github.angerona.knowhow.graph;

import java.io.Serializable;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.github.angerona.knowhow.graph.complexity.ComplexityCalculator;
import com.github.angerona.knowhow.graph.parameter.Parameter;
import com.github.angerona.knowhow.graph.parameter.ParameterCheckVisitor;


public interface GraphNode extends Serializable{
	public static enum NodeType {
		NT_PROCESSOR,
		NT_SELECTOR
	}
	
	/**
	 * @return A list of GraphNodes that represent the children of this node
	 */
	List<? extends GraphNode> getChildren();
	
	/**
	 * Calculates the complexity of this node and each of its children. The visitor
	 * pattern is used to move the logic of the complexity calculation into the
	 * {@link ComplexityCalculator} interface.
	 * 
	 * @param calculator	The calculator responsible for the calculator
	 * @return	An integer representing the complexity of this node
	 */
	int visitComplexityCalculation(ComplexityCalculator calculator);
	
	boolean visitParameterCheck(ParameterCheckVisitor parameterChecker);
	
	int getComplexity();
	
	String getName();
	
	List<Parameter> getParameters();
	
	NodeType getType();
	
	String toString();
	
	void addListener(NodeListener listener);
	
	void removeListener(NodeListener listener);
	
	void removeAllListener();

	Graph<GraphNode, DefaultEdge> getGraph();
}
