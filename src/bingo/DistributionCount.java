/*
 * DistributionCount.java
 *
 * Created on 30 november 2007, 10:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bingo;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author stmae
 */
public abstract class DistributionCount extends CalculateTestTask {
	
        @Override
	abstract public void calculate();

	/**
	 * method for compiling GO classifications for given node
	 */

	abstract Set getNodeClassifications(String node);

	/**
	 * method for making the hashmap for small n.
	 */
	abstract void countSmallN();

	/**
	 * method for making the hashmap for the small x.
	 */
	abstract void countSmallX();

	/**
	 * method that counts for small n and small x.
	 */
	abstract Map<Integer, Integer> count(Set<String> nodes);

	/**
	 * counts big N. unclassified nodes are not counted ; no correction for
	 * function_unknown nodes (yet)(requires user input)
	 */
	abstract void countBigN();

	/**
	 * counts big X. unclassified nodes are not counted ; no correction for
	 * function_unknown nodes (yet)(requires user input)
	 */
	abstract void countBigX();

}
