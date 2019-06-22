/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontology;

import java.util.List;

/**
 *
 * @author Radoslav Davidović
 */
public class Term {
    private boolean isObsolete;
    private String name;
    private String namespace;
    private String id;
    private String subset;
    private List<String> alternativeIDs;
    private List<String> parents;
    private List<String> containers;

    public List<String> getContainers() {
        return containers;
    }

    public void setContainers(List<String> containers) {
        this.containers = containers;
    }

    public boolean isIsObsolete() {
        return isObsolete;
    }

    public void setIsObsolete(boolean isObsolete) {
        this.isObsolete = isObsolete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public List<String> getAlternativeIDs() {
        return alternativeIDs;
    }

    public void setAlternativeIDs(List<String> alternativeIDs) {
        this.alternativeIDs = alternativeIDs;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }
    
    
    
}
