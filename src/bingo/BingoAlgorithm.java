package bingo;

import ontology.Annotation;
import ontology.Ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by User: risserlin Date: Jun 13, 2006 Time: 1:00:50 PM
 * Modified by Radoslav Davidović July 11, 2018.
 * <ol>Changes:
 *  <li>Constant variables removed</li>
 * <li>new constructor {@link #BingoAlgorithm(ontology.Annotation, ontology.Ontology, 
 * bingo.StatisticsDescriptor, java.util.Map, java.util.Set, java.util.Set) }</li>
 * <li>old constructor removed</li>
 * <li>instance variable of type {@link BingoParameters} removed</li>
 * <li>new instance variable of type {@link StatisticsDescriptor}</li>
 * <li>new instance variables of type {@link Annotation} and {@link Ontology}</li>
 * <li>new instance variables {@link #selectedNodes}, {@link #allNodes} and {@link #alias}</li>
 *  <li>method {@link #calculate_distribution()} has been changed to be in line with upstream changes</li>
 * </ol>
 */
public class BingoAlgorithm {
    private StatisticsDescriptor descriptor;
    private Map<String, HashSet<String>> alias;
    private Set<String> selectedNodes;
    private Set<String> allNodes;
    private Annotation annotation;
    private Ontology ontology;

    public BingoAlgorithm(Annotation annotation, Ontology ontology, StatisticsDescriptor descriptor,
                          Map<String, HashSet<String>> alias, Set<String> selectedNodes, Set<String> allNodes){
        this.annotation = annotation;
        this.ontology = ontology;
        this.descriptor = descriptor;
        this.alias = alias;
        this.selectedNodes = selectedNodes;
        this.allNodes = allNodes;
    }
    
    public CalculateTestTask calculate_distribution() {
        
        CalculateTestTask test;
        
        //HashMap testMap;
        boolean isOver = descriptor.getRepresentation().equals(Constants.OVERSTRING.getConstant());
        test = new StatisticTestCalculate(new StandardDistributionCount(annotation,
                            ontology, selectedNodes, allNodes, alias, isOver), descriptor.getTest(), isOver);
        return test;
    }

    public CalculateCorrectionTask calculate_corrections(Map testMap) {
        HashMap correctionMap;
        CalculateCorrectionTask correction = null;

        if (descriptor.getCorrectionTest().equals(Constants.NONE.getConstant())) {
        } else {
            if(descriptor.getCorrectionTest().equals(Constants.BONFERRONI.getConstant())){
                correction = new Bonferroni(testMap);
            }else if(descriptor.getCorrectionTest().equals(Constants.BENJAMINI_HOCHBERG_FDR.getConstant())){
                try {
                        correction = new BenjaminiHochbergFDR(testMap);
                    } catch (NullPointerException ex) {
                        correction = null;
                    } 
            }else{
                correctionMap = null;
            }
           
        }
        return correction;
    }
}
