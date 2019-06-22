/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bingo;

import java.util.HashSet;
import java.util.Map;
import ontology.Annotation;

/**
 *
 * @author radoslav davidović
 */
public interface IAnnotation {
    Annotation getAnnotation();
    Map<String, HashSet<String>> getAlias();
    boolean getOrphans();
    boolean getConsistency();
}
