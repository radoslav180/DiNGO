/*
  File: Ontology.java

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
// Ontology.java
package ontology;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

/**
 * Contains a collection of OntologyTerms, each of which may have pointers to
 * other terms, creating a hierarchical controlled vocabulary. The pointers may
 * express either "parent/child" relationships, or "container/contained"
 * relationships. Objects of this class each aggregate a full set of related
 * terms, creating a distinct ontology of a certain type, like KEGG's metabolic
 * pathways, or GeneOntology's biological processes. Extensive navigational
 * methods are provided so that, for instance, the full leaf-to-root biological
 * process hierarchies which may exist for a given gene are conveniently
 * returned.
 *
 */
public class Ontology implements Serializable {

    protected String curator; // KEGG, GO, ...
    protected String ontologyType; // biological process, metabolic pathway, ...
    protected HashMap<Integer, OntologyTerm> termHash;

    /**
     * @param curator The institute or group which maintains this ontology
     * @param ontologyType The nature of this ontology, eg, "metabolic pathway",
     * "molecular function", or "cellular component"
     */
    public Ontology(String curator, String ontologyType) {
        this.curator = curator;
        this.ontologyType = ontologyType;
        termHash = new HashMap<>();
    } // ctor

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
        return ontologyType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param newTerm DOCUMENT ME!
     */
    public void add(OntologyTerm newTerm) {
        termHash.put(newTerm.getId(), newTerm);
    } // add

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int size() {
        return termHash.size();
    } // size

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HashMap<Integer, OntologyTerm> getTerms() {
        return termHash;
    }

    /**
     * DOCUMENT ME!
     *
     * @param id DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean containsTerm(int id) {
        return termHash.containsKey(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param id DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public OntologyTerm getTerm(int id) {
        return termHash.get(id);
    }

    /**
     * get all unique paths from the termID to the root of the ontology, in
     * reverse order, with the most general classification first, and most
     * specific last. for some terms, this will be a path, possibly with only
     * one member. for other terms, which have be assigned to multiple
     * categories, there will be multiple paths. furthermore, some categories
     * belong to multiple pathways, further increasing the number of paths
     * implicit in the termID.
     *
     * @param termID
     * @return
     */
    public int[][] getAllHierarchyPaths(int termID) {
        Vector flattenedList = getAllHierarchyPathsAsVector(termID);
        int size = flattenedList.size();
        int[][] result = new int[size][];

        for (int i = 0; i < size; i++) {
            Vector integerList = (Vector) flattenedList.get(i);
            int max = integerList.size();
            result[i] = new int[max];

            for (int j = 0; j < max; j++) {
                int indexForReversingOrder = max - j - 1;
                result[i][j] = ((Integer) integerList.get(indexForReversingOrder));
            }
        } // for i

        return result;
    } // getAllHierarchyPaths

    /**
     * DOCUMENT ME!
     *
     * @param termID DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[][] getAllHierarchyPathsAsNames(int termID) {
        int[][] hierarchy = getAllHierarchyPaths(termID);
        String[][] result = new String[hierarchy.length][];

        for (int i = 0; i < hierarchy.length; i++) {
            int[] path = hierarchy[i];
            int max = path.length;
            result[i] = new String[max];

            for (int j = 0; j < path.length; j++) {
                result[i][j] = getTerm(path[j]).getName();
            }

            // result [i][j] = getTerm (path [max-j-1]).getName ();
        } // for i

        return result;
    } // getAllHierarchyPathsAsNames

    protected Vector getAllHierarchyPathsAsVector(int termID) {
        Vector nestedLists = recursiveGetPath(termID, new Vector<>());

        if (nestedLists.isEmpty()) {
            return nestedLists;
        }
        System.out.println(nestedLists.toString());
        FlattenIntVectors flattener = new FlattenIntVectors(nestedLists);
        Vector flattenedList = flattener.getResult();
        System.out.println("Flattened list: " + flattenedList.toString());
        return flattenedList;
    } // getAllHierarchyPathsAsVector

    protected Vector recursiveGetPath(int termID, Vector<Integer> path) {
        Integer ID = termID;

        if ((termHash != null) && termHash.containsKey(ID)) {
            OntologyTerm term = termHash.get(ID);
            int parentCount = term.numberOfParentsAndContainers();
            path.addElement(ID);

            if (parentCount == 0) {
                return path;
            } else if (parentCount == 1) {
                int parentID = term.getParentsAndContainers()[0];

                return (recursiveGetPath(parentID, path));
            } else { // assume for now: (parentCount == 2)

                Vector newPath = new Vector();

                for (int p = 0; p < parentCount; p++) {
                    Vector<Integer> subPath = (Vector<Integer>) path.clone();
                    int parent = term.getParentsAndContainers()[p];
                    newPath.addElement(recursiveGetPath(parent, subPath));
                } // for p

                return newPath;
            } // else: 2 or more parents
        } // if ID is in termHas

        return path;
    } // recursiveGetPath

    protected Vector oldRecursiveGetPath(int termID, Vector path) {
        Integer ID = termID;

        if ((termHash != null) && termHash.containsKey(ID)) {
            OntologyTerm term = (OntologyTerm) termHash.get(ID);
            int parentCount = term.numberOfParentsAndContainers();

            if (parentCount == 0) {
                path.addElement(ID);

                return path;
            } else if (parentCount == 1) {
                path.addElement(ID);

                int parentID = term.getParentsAndContainers()[0];

                return (recursiveGetPath(parentID, path));
            } else { // assume for now: (parentCount == 2)
                path.addElement(ID);

                Vector newPath = new Vector();
                Vector path1 = (Vector) path.clone();
                Vector path2 = (Vector) path.clone();
                int parent1 = term.getParentsAndContainers()[0];
                int parent2 = term.getParentsAndContainers()[1];
                newPath.addElement(recursiveGetPath(parent1, path1));
                newPath.addElement(recursiveGetPath(parent2, path2));

                return newPath;
            }
        }

        return path;
    } // oldRecursiveGetPath

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public OntologyDescription getDescription() {
        return new OntologyDescription(curator, ontologyType);
    }

    public HashMap<Integer, OntologyTerm> getTermHash() {
        return termHash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ontology: ").append(ontologyType).append("\n");
        sb.append("curator:  ").append(curator).append("\n");

        return sb.toString();
    } // toString
} // class Ontology
