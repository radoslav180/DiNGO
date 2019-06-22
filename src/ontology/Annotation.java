/*
 File: Annotation.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ontology;

import java.io.Serializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Store any number of classifications for named entities, each of which is from
 * a specified species, and where the classifications are all terms from an
 * ontology (a controlled vocabulary for some subject domain, expressed as a
 * directed acyclic graph). For example:
 * <p>
 * <ul>
 * <li> for the species Halobacterium
 * <li> with respect to the KEGG metabolic pathways ontology
 * <li> gene VNG0623G has been assigned into the following categories
 * <ol>
 * <li> Valine, leucine and isoleucine degradation
 * <li> Propanoate metabolism
 * </ol>
 * </ul>
 *
 * This simple assignment of two classifications to one gene becomes richer when
 * we refer to the ontology: in the KEGG ontology, these two terms belong to a
 * tree, expressed linearly, and from the top (most general term) down as
 * <ul>
 * <li> metablolism -> amino acid metabolism -> valine, leucine and isoleucine
 * degradation
 * <li> metabolism -> carbohydrate metabolism -> propanoate metabolism
 * </ul>
 *
 * Thus the combination of an annotation (the present class) with an ontology
 * provides a means to richly and flexibly describe an object.
 * Modified by Radoslav Davidović 2018
 * Vector replaced by ArrayList
 */
public class Annotation implements Serializable {

    protected Ontology ontology;
    protected String curator;
    protected String species;
    protected String type;
    private HashMap<String, ArrayList<Integer>> hash; // (name, ArrayList) pairs, the ArrayList contains Integers

    /**
     * Creates a new Annotation object.
     *
     * @param species DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param ontology DOCUMENT ME!
     */
    public Annotation(String species, String type, Ontology ontology) {
        this.ontology = ontology;
        this.species = species;
        this.type = type;
        this.curator = ontology.getCurator();
        hash = new HashMap<>();
    }

    /**
     * Creates a new Annotation object.
     *
     * @param species DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param curator DOCUMENT ME!
     */
    public Annotation(String species, String type, String curator) {
        this.curator = curator;
        this.species = species;
        this.type = type;
        hash = new HashMap<>();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HashMap<String, ArrayList<Integer>> getMap() {
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @param newValue DOCUMENT ME!
     */
    public void setOntology(Ontology newValue) {
        ontology = newValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Ontology getOntology() {
        return ontology;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getCurator() {
        return curator;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOntologyType() {
        if (ontology != null) {
            return ontology.getType();
        }

        return "unknown";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getSpecies() {
        return species;
    }

    /**
     * create a new annotation for an entity (typically a gene) of the current
     * species and with respect to the current ontology. an entity may have more
     * than one classification.
     *
     * @param name usually an ORF name, a unique identifier for this species
     * @param classificationID a pointer into the ontology hierarchy
     */
    public void add(String name, int classificationID) {
        ArrayList<Integer> classifications;

        if (hash.containsKey(name)) {
            classifications = hash.get(name);
        } else {
            classifications = new ArrayList<>();
            hash.put(name, classifications);
        }

        Integer classificationInteger = classificationID;

        if (!classifications.contains(classificationInteger)) {
            classifications.add(classificationID);
        }
    }

    /**
     * returns an array of all the names (usually ORFs) currently annotated
     *
     * @return
     */
    public String[] getNames() {
        return (String[]) hash.keySet().toArray(new String[0]);
    }

    /**
     * returns an array of all the classifications in the current annotation
     * @return 
     */
    public int[] getClassifications() {
        ArrayList<Integer>[] arrayOfIntegerArrayLists = hash.values().toArray(new ArrayList[0]);

        ArrayList<Integer> collector = new ArrayList<>();

        for (int v = 0; v < arrayOfIntegerArrayLists.length; v++) {
            ArrayList<Integer> vec = arrayOfIntegerArrayLists[v];

            collector.addAll(vec);
        } // for v

        int[] result = new int[collector.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = collector.get(i);
        }
        
        return result;
    }

    /**
     * all of the ontology identifiers registered for the specified entity
     * @param name
     * @return 
     */
    public int[] getClassifications(String name) {
        if (!hash.containsKey(name)) {
            return new int[0];
        }

        ArrayList<Integer> classifications = hash.get(name);
        int[] result = new int[classifications.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = classifications.get(i);
        }

        return result;
    }

    /**
     * all of the ontology identifiers registered for the specified entity, as a
     * ArrayList of Integers
     * @param name
     * @return 
     */
    public ArrayList<Integer> getClassificationsArrayList(String name) {
        if (!hash.containsKey(name)) {
            return new ArrayList<>();
        } else {
            return hash.get(name);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[][] getAllHierarchyPathsAsNames(String name) {
        if (ontology == null) {
            return new String[0][0];
        }

        int[] leafClassifications = getClassifications(name);
        String[][] result;

        if (leafClassifications.length == 0) {
            result = new String[0][0];
        }

        ArrayList<String[]> collector = new ArrayList();

        for (int i = 0; i < leafClassifications.length; i++) {
            String[][] paths = ontology.getAllHierarchyPathsAsNames(leafClassifications[i]);

            collector.addAll(Arrays.asList(paths));
        }

        result = new String[collector.size()][];

        for (int i = 0; i < collector.size(); i++) {
            String[] path = (String[]) collector.get(i);
            result[i] = path;
        }

        return result;
    }

    /**
     * total number of entities, usually ORFs.
     */
    public int count() {
        return hash.size();
    }

    /**
     * total number of classifications. this will usually be larger than count
     * (), since entities are frequently given two or more classifications,
     * reflecting the multiple roles of many biological entities.
     */
    public int size() {
        String[] names = getNames();
        int total = 0;

        for (String name : names) {
            total += hash.get(name).size();
        }

        return total;
    }

    /**
     * get all of the full paths (as ints) from the ontology for all of the
     * currently annotated entities. then traverse this (possibly large) list
     * and return the longest path.
     */
    public int maxDepth() {
        if (ontology == null) {
            return 0;
        }

        int[] classifications = getClassifications();
        int max = 0;

        for (int i = 0; i < classifications.length; i++) {
            int[][] paths = ontology.getAllHierarchyPaths(classifications[i]);

            for (int p = 0; p < paths.length; p++) {
                if (paths[p].length > max) {
                    max = paths[p].length;
                }
            }
        } // for i

        return max;
    }

    public HashMap<String, ArrayList<Integer>> getHash() {
        return hash;
    }
    
   
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("annotation: ");
        sb.append(getCurator());
        sb.append(", ");
        sb.append(type);
        sb.append(", ");
        sb.append(species);
        sb.append(" (").append(count()).append(" entities)  ");
        sb.append(" (").append(size()).append(" classifications)  ");

        return sb.toString();
    }
}
