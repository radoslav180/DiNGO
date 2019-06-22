/*
 File: OntologyTerm.java

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
// OntologyTerm.java
package ontology;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents one node in an ontology, which is a hierarchical classification of
 * entities using a controlled vocabulary. Each term has a name, a unique
 * integer identifier, a list of parent terms, and a list of containers. Parent
 * terms capture the "is-child-of" relationship for this term. Container terms
 * capture the "is-part-of" relationship. This data model was inspired by the
 * three ontologies (biological process, molecular function, cellular component)
 * of the Gene Ontology project. Other ontologies should work nicely with this
 * class as well.
 *
 */
public class OntologyTerm implements Serializable {

    String name;
    int id;
    private ArrayList<Integer> parents;
    private ArrayList<Integer> containers;

    /**
     * Creates a new OntologyTerm object.
     *
     * @param name DOCUMENT ME!
     * @param id DOCUMENT ME!
     */
    public OntologyTerm(String name, int id) {
        this.name = name;
        this.id = id;
        parents = new ArrayList<>();
        containers = new ArrayList<>();
    } // ctor

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param newParent DOCUMENT ME!
     */
    public void addParent(int newParent) {
        parents.add(newParent);
    }

    /**
     * DOCUMENT ME!
     *
     * @param newContainer DOCUMENT ME!
     */
    public void addContainer(int newContainer) {
        containers.add(newContainer);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int numberOfParentsAndContainers() {
        return numberOfParents() + numberOfContainers();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int numberOfParents() {
        return parents.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int numberOfContainers() {
        return containers.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int[] getParents() {
        int size = numberOfParents();
        int[] result = new int[size];

        for (int i = 0; i < size; i++) {
            Integer tmp = parents.get(i);
            result[i] = tmp;
        }

        return result;
    } // getParents

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int[] getContainers() {
        int size = numberOfContainers();
        int[] result = new int[size];

        for (int i = 0; i < size; i++) {
            Integer tmp = containers.get(i);
            result[i] = tmp;
        }

        return result;
    } // getContainers

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int[] getParentsAndContainers() {
        int size = numberOfParents() + numberOfContainers();

        int[] result = new int[size];
        int[] parents = getParents();
        int[] containers = getContainers();

        for (int i = 0; i < parents.length; i++) {
            result[i] = parents[i];
        }

        for (int i = 0; i < containers.length; i++) {
            result[i + parents.length] = containers[i];
        }

        return result;
    } // getParentsAndContainers

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isParentOf(OntologyTerm other) {
        int[] otherParents = other.getParents();

        for (int i = 0; i < otherParents.length; i++) {
            if (this.id == otherParents[i]) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param ontology the Ontology that contains the terms
     * @param other
     * @return true if this OntologyTerm has a path to the other ontologyTerm
     * via parent-child or container-contained relationships
     */
    public boolean isAncestorOf(Ontology ontology, OntologyTerm other) {
        if (isParentOrContainerOf(other)) {
            return true;
        }

        int[] myParents = getParentsAndContainers();

        for (int i = 0; i < myParents.length; i++) {
            OntologyTerm parentTerm = ontology.getTerm(myParents[i]);

            if ((parentTerm != null) && parentTerm.isAncestorOf(ontology, other)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isChildOf(OntologyTerm other) {
        int otherId = other.getId();
        int[] myParents = getParents();

        for (int i = 0; i < myParents.length; i++) {
            if (myParents[i] == otherId) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isContainerOf(OntologyTerm other) {
        int[] otherContainers = other.getContainers();

        for (int i = 0; i < otherContainers.length; i++) {
            if (this.id == otherContainers[i]) {
                return true;
            }
        }

        return false;
    } // isContainerOf

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isContainedIn(OntologyTerm other) {
        int[] myContainers = getContainers();
        int otherID = other.getId();

        for (int i = 0; i < myContainers.length; i++) {
            if (myContainers[i] == otherID) {
                return true;
            }
        }

        return false;
    } // isContainedIn

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isChildOfOrContainedIn(OntologyTerm other) {
        int otherID = other.getId();
        int[] myParentsAndContainers = getParentsAndContainers();

        for (int i = 0; i < myParentsAndContainers.length; i++) {
            if (otherID == myParentsAndContainers[i]) {
                return true;
            }
        }

        return false;
    } // isChildOfOrContainedIn

    /**
     * DOCUMENT ME!
     *
     * @param other DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isParentOrContainerOf(OntologyTerm other) {
        int[] otherParentsAndContainers = other.getParentsAndContainers();

        for (int i = 0; i < otherParentsAndContainers.length; i++) {
            if (this.id == otherParentsAndContainers[i]) {
                return true;
            }
        }

        return false;
    } // isParentOrContainerOf

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("name: ");
        sb.append(name);
        sb.append("\n");

        sb.append("id: ");
        sb.append(id);
        sb.append("\n");

        int[] parents = getParents();

        sb.append("parents: ");

        for (int i = 0; i < parents.length; i++) {
            sb.append(parents[i]);
            sb.append(" ");
        }

        sb.append("\n");

        int[] containers = getContainers();

        sb.append("containers: ");

        for (int i = 0; i < containers.length; i++) {
            sb.append(containers[i]);
            sb.append(" ");
        }

        sb.append("\n");

        return sb.toString();
    } // toString
} // OntologyTerm
