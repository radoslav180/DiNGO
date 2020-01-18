/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package propagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *<p>Class that represents ontology Term
 * Note that class does not know anything about xref and synonyms.
 * </p>
 * @author Radoslav Davidović
 */
public class Term {
    //if term is obsolete
    private boolean isObsolete;
    //ID of term, for instance HP:0000016
    private String id;
    //term name , for instance Urinary retention
    private String name;
    //term namespace
    private String namespace;
    //parents of term (only direct parents)
    private List<Term> parentTerms;
    //alternative Term's IDs
    private List<String> altIDList;
    
    /**
     * <p>Constructor</p>
     * @param id ID of term
     * @param name name of term
     */
    public Term(String id, String name){
        this.id = id;
        this.name = name;
        this.parentTerms = new ArrayList<>();
        this.altIDList = new ArrayList<>();
    }
    
    /**
     * <p>Constructor</p>
     * @param isObsolete if term is obsolete
     * @param id   ID of term 
     * @param name name of term
     * @param parentTerms parents of term
     */
    public Term(boolean isObsolete, String id, String name, List<Term> parentTerms){
        this(id, name);
        this.isObsolete = isObsolete;
        this.parentTerms = parentTerms;
    }
    
    public Term(boolean isObsolete, String id, String name, String namespace, List<Term> parentTerms){
        this(isObsolete, id, name, parentTerms);
        this.namespace = namespace;
        
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /**
     * <p>Method that answer the question if current object is child of specified object</p>
     * @param other instance of Term
     * @return returns true if other is parent of current object. Otherwise false.
     */
    public boolean isChildOf(Term other){
    
        return this.parentTerms.contains(other);
    }
    
    /**
     * <p>Method that answer the question if current object is parent of specified object</p>
     * @param other
     * @return returns true if other is child of current object. Otherwise false
     */
    public boolean isParentOf(Term other){
        return other.parentTerms.contains(this);
    }
    
    /**
     * <p>Method that answer the question if an object is obsolete</p>
     * @return returns true if object is obsolete. Otherwise false.
     */
    public boolean isObsolete() {
        return isObsolete;
    }
    
    /**
     * <p>Returns Term id</p>
     * @return id
     */
    public String getId() {
        return id;
    }
    
    /**
     * <p>Returns Term name</p>
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * <p>Returns List of Term parents</p>
     * @return parentTerms
     */
    public List<Term> getParentTerms() {
        return parentTerms;
    }
    /**
     * <p>Implementation of {@link Object#equals(Object)}
     * Two Terms are equal if and only if their {@link #id} are equal.
     * </p>
     * @param other 
     * @return 
     */
    @Override
    public boolean equals(Object other){
        if(other == null){
            return false;
        }
        
        if (!(other instanceof Term)) {
            return false;
        }
        Term term = (Term) other;
        return this.id.equals(term.id);
    }
    
    /**
     * <p>Implementation of {@link Object#hashCode()}</p>
     * @return hash code of Term instance
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }
    
    /**
     * <p>Implementation of {@link Object#toString() }</p>
     * @return string representation of Term object
     */
    @Override
    public String toString(){
        return String.format("Term id: %s\nName: %s", this.id, this.name);
    }

    public boolean isIsObsolete() {
        return isObsolete;
    }

    public List<String> getAltIDList() {
        return altIDList;
    }

    public void setAltIDList(List<String> altIDList) {
        this.altIDList = altIDList;
    }
}
