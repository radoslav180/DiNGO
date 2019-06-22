/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bingo;

/**
 * <p>Defines constants used by DiNGO</p>
 * @author Radoslav Davidović
 */
public enum Constants {
    NONE,
    HYPERGEOMETRIC,
    BINOMIAL,
    OVERSTRING,
    BENJAMINI_HOCHBERG_FDR,
    BONFERRONI,
    GENOME,
    CATEGORY,
    CATEGORY_BEFORE_CORRECTION,
    CATEGORY_CORRECTION;
    
    String getConstant(){
        switch(this){
            case NONE: return "---";
            case BINOMIAL: return "Binomial test";
            case HYPERGEOMETRIC: return "Hypergeometric test";
            case OVERSTRING: return "Overrepresentation";
            case BENJAMINI_HOCHBERG_FDR: return "Benjamini & Hochberg False Discovery Rate (FDR) correction";
            case BONFERRONI: return "Bonferroni Family-Wise Error Rate (FWER) correction";
            case GENOME: return "Use whole annotation as reference set";
            case CATEGORY: return "All categories";
            case CATEGORY_BEFORE_CORRECTION: return "Overrepresented categories before correction";
            case CATEGORY_CORRECTION: return "Overrepresented categories after correction";
            default: 
                throw new IllegalArgumentException();
        }
    }
}
