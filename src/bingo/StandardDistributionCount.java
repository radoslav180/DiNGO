package bingo;

/* * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * *
 * * Authors : Steven Maere, Karel Heymans
 * *
 * * This program is free software; you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation; either version 2 of the License, or
 * * (at your option) any later version.
 * *
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * * The software and documentation provided hereunder is on an "as is" basis,
 * * and the Flanders Interuniversitary Institute for Biotechnology
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Flanders Interuniversitary Institute for Biotechnology
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * the Flanders Interuniversitary Institute for Biotechnology
 * * has been advised of the possibility of such damage. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program; if not, write to the Free Software
 * * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * *
 * * Authors: Steven Maere, Karel Heymans
 * * Date: Mar.25.2005
 * * Description: class that counts the small n, big N, small x, big X which serve as input for the statistical tests.     
 **/
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import ontology.Annotation;
import ontology.Ontology;

/**
 * ************************************************************
 * DistributionCount.java Steven Maere & Karel Heymans (c) March 2005
 * Modified by Radoslav Davidović
 * <ol>Changes:
 *  <li>added new constructor {@link #StandardDistributionCount(ontology.Annotation, ontology.Ontology, java.util.Set,
 *  java.util.Set, java.util.Map, boolean)}</li>
 *  <li>code in {@link #count(java.util.Set) } method is slightly changed to be in line with upstream changes</li>
 * </ol>
 * ----------------------
 * <p>
 * class that counts the small n, big N, small x, big X which serve as input for
 * the statistical tests.</p>
 * *************************************************************
 */
public class StandardDistributionCount extends DistributionCount {

    /**
     * the annotation.
     */
    private Annotation annotation;

    private Map<String, HashSet<String>> alias;
    /**
     * HashSet of selected nodes
     */
    private Set<String> selectedNodes;
    /**
     * HashSet of reference nodes
     */
    private Set<String> refNodes;
    /**
     * hashmap with values of small n ; keys GO labels.
     */
    private Map<Integer, Integer> mapSmallN;
    /**
     * hashmap with values of small x ; keys GO labels.
     */
    private Map<Integer, Integer> mapSmallX;
    /**
     * hashmap with values of big N.
     */
    private Map<Integer, Integer> mapBigN;
    /**
     * hashmap with values of big X.
     */
    private Map<Integer, Integer> mapBigX;

    private boolean isOver = true;

    public StandardDistributionCount(Annotation annotation, Ontology ontology, Set<String> selectedNodes, Set<String> refNodes,
            Map<String, HashSet<String>> alias) {
        this.annotation = annotation;
        this.alias = alias;
        annotation.setOntology(ontology);

        this.selectedNodes = selectedNodes;
        this.refNodes = refNodes;
    }

    public StandardDistributionCount(Annotation annotation, Ontology ontology, Set<String> selectedNodes, Set<String> refNodes,
            Map<String, HashSet<String>> alias, boolean isOver) {
        this.annotation = annotation;
        this.alias = alias;
        annotation.setOntology(ontology);

        this.selectedNodes = selectedNodes;
        this.refNodes = refNodes;
        this.isOver = isOver;
    }

    @Override
    public void calculate() {

        countSmallX();
        countSmallN();
        countBigX();
        countBigN();
    }

    /**
     * method for compiling GO classifications for given node
     *
     * @param node
     * @return
     */
    @Override
    public HashSet<String> getNodeClassifications(String node) {
        //System.out.print(node + " ");
        // HashSet for the classifications of a particular node
        HashSet<String> classifications = new HashSet<>();
        Set<String> identifiers = alias.get(node + "");
        if (identifiers != null) {
            //System.out.print(identifiers + " ");
            Iterator it = identifiers.iterator();
            int[] goID;
            while (it.hasNext()) {
                goID = annotation.getClassifications(it.next() + "");
                
                for (int t = 0; t < goID.length; t++) {
                    //System.out.print(goID[t] + " ");
                    classifications.add(goID[t] + "");
                    // omitted : all parent classes of GO class that node is
                    // assigned to are also explicitly included in
                    // classifications from the start
                    // up(goID[t], classifications) ;
                }
                
            }
            //System.out.println("");
        }
        return classifications;
    }

    /**
     * method for compiling represented GO categories for all nodes ; for
     * underrepresentation, nodes in the set with 0 occurrences but some
     * occurrence in the reference set are also considered
     *
     * @return
     */
    public HashSet<String> getAllClassifications() {

        HashSet<String> classifications = new HashSet<>();

        Iterator i = refNodes.iterator();
        while (i.hasNext()) {
            Set<String> identifiers = alias.get(i.next() + "");
            if (identifiers != null) {
                Iterator it = identifiers.iterator();
                while (it.hasNext()) {
                    int[] goID = annotation.getClassifications(it.next() + "");
                    for (int t = 0; t < goID.length; t++) {
                        classifications.add(goID[t] + "");
                    }
                }
            }
        }
        return classifications;
    }

    /**
     * method for making the hashmap for small n.
     */
    @Override
    public void countSmallN() {
        mapSmallN = this.count(refNodes);
    }

    /**
     * method for making the hashmap for the small x.
     */
    @Override
    public void countSmallX() {
        mapSmallX = this.count(selectedNodes);
    }

    /**
     * method that counts for small n and small x.
     *
     * @param nodes
     * @return
     */
    @Override
    public Map<Integer, Integer> count(final Set<String> nodes) {

        HashMap<Integer, Integer> map = new HashMap<>();
        Integer id;
        //this check is added
        if (!isOver) {

            HashSet<String> allClassifications = getAllClassifications();

            Iterator<String> iterator1 = allClassifications.iterator();

            while (iterator1.hasNext()) {
                id = Integer.parseInt(iterator1.next());
                if (!map.containsKey(id)) {
                    map.put(id, 0);
                }
            }

        }

        Iterator<String> i = nodes.iterator();
        while (i.hasNext()) {
            HashSet<String> classifications = getNodeClassifications(i.next());
            Iterator<String> iterator = classifications.iterator();
            
            // puts the classification counts in a map
            while (iterator.hasNext()) {
                id = Integer.parseInt(iterator.next());
                if (map.containsKey(id)) {
                    map.put(id, map.get(id) + 1);
                } else if (isOver) {
                    map.put(id, 1);
                }
            }

        }

        return map;
    }

    /**
     * counts big N. unclassified nodes are not counted ; no correction for
     * function_unknown nodes (yet)(requires user input)
     */
    @Override
    public void countBigN() {
        mapBigN = new HashMap<>();
        int bigN = refNodes.size();
        Iterator i = refNodes.iterator();
        while (i.hasNext()) {
            HashSet classifications = getNodeClassifications(i.next().toString());
            Iterator iterator = classifications.iterator();
            if (!iterator.hasNext()) {
                bigN--;
            }
        }
        for (Object id : this.mapSmallX.keySet()) {
            mapBigN.put((Integer) id, bigN);
        }
    }

    /**
     * counts big X. unclassified nodes are not counted ; no correction for
     * function_unknown nodes (yet)(requires user input)
     */
    @Override
    public void countBigX() {
        mapBigX = new HashMap<>();
        int bigX = selectedNodes.size();
        Iterator i = selectedNodes.iterator();
        while (i.hasNext()) {
            HashSet classifications = getNodeClassifications(i.next().toString());
            Iterator iterator = classifications.iterator();
            if (!iterator.hasNext()) {
                bigX--;
            }
        }
        for (Object id : this.mapSmallX.keySet()) {
            mapBigX.put((Integer) id, bigX);
        }
    }

    @Override
    public Map getTestMap() {
        return mapSmallX;
    }

    /**
     * returns small n hashmap.
     *
     * @return hashmap mapSmallN
     */
    @Override
    public Map<Integer, Integer> getMapSmallN() {
        return mapSmallN;
    }

    /**
     * returns small x hashmap.
     *
     * @return hashmap mapSmallX
     */
    @Override
    public Map<Integer, Integer> getMapSmallX() {
        return mapSmallX;
    }

    @Override
    public Map<Integer, Integer> getMapBigN() {
        return mapBigN;
    }

    @Override
    public Map<Integer, Integer> getMapBigX() {
        return mapBigX;
    }
}
