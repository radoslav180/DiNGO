package bingo;

/*
 * * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology
 * (VIB) * * Authors : Steven Maere, Karel Heymans * * This program is free
 * software; you can redistribute it and/or modify * it under the terms of the
 * GNU General Public License as published by * the Free Software Foundation;
 * either version 2 of the License, or * (at your option) any later version. * *
 * This program is distributed in the hope that it will be useful, * but WITHOUT
 * ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. * The software and documentation provided
 * hereunder is on an "as is" basis, * and the Flanders Interuniversitary
 * Institute for Biotechnology * has no obligations to provide maintenance,
 * support, * updates, enhancements or modifications. In no event shall the *
 * Flanders Interuniversitary Institute for Biotechnology * be liable to any
 * party for direct, indirect, special, * incidental or consequential damages,
 * including lost profits, arising * out of the use of this software and its
 * documentation, even if * the Flanders Interuniversitary Institute for
 * Biotechnology * has been advised of the possibility of such damage. See the *
 * GNU General Public License for more details. * * You should have received a
 * copy of the GNU General Public License * along with this program; if not,
 * write to the Free Software * Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA * * Authors: Steven Maere, Karel Heymans * Date:
 * Mar.25.2005 * Description: Class implementing the Benjamini and Hochberg FDR
 * correction algorithm.
 */
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * ************************************************************************
 * BenjaminiHochbergFDR.java: Steven Maere & Karel Heymans (c) March 2005<br>
 * Modified by Radoslav Davidović January 2018
 * <p>
 * There are significant changes in this version:
 * <ol>
 * <li>The following variable were removed:
 * <ul>
 * <li><code>HashEntry[] hash</code></li>
 * <li><code>static String[] ordenedGOLabels</code></li>
 * <li><code>static String[] ordenedPvalues</code></li>
 * <li><code>static int numberOfTests</code></li>
 * <li><code>static String[] goLabels</code></li>
 * <li><code>static String[] pvalues</code></li>
 * <li><code>static BigDecimal alpha</code></li>
 * </ul>
 * </li>
 * <li>variable {@link #adjustedPvalues} is instance variable not a static
 * one</li>
 * <li>new constructor {@link #BenjaminiHochbergFDR(java.util.Map) }</li>
 * <li>inner classes <code>HashEntry</code> and <code>HashComprator</code> were
 * removed</li>
 * </ol>
 * </p>
 * <p>
 * Class implementing the Benjamini and Hochberg FDR correction algorithm.
 * </p> ************************************************************************
 */
public class BenjaminiHochbergFDR extends CalculateCorrectionTask {

    /**
     * the adjusted p-values ordered in ascending order.
     */
    private String[] adjustedPvalues;

    /**
     * hashmap with the results (adjusted p-values) as values and the GO labels
     * as keys.
     */
    private HashMap<String, String> correctionMap;

    /**
     * scale for the division in de method 'runFDR'.
     */
    private static final int RESULT_SCALE = 100;

    /**
     * Constructor.
     *
     * @param golabelstopvalues Hashmap of Strings with the goLabels and their p
     * values.
     *
     */
    public BenjaminiHochbergFDR(Map golabelstopvalues) {

        super(golabelstopvalues);
        this.adjustedPvalues = new String[numberOfTests];
    }

    /*--------------------------------------------------------------
	METHODS.
	--------------------------------------------------------------*/
    /**
     * method that calculates the Benjamini and Hochberg correction of the false
     * discovery rate NOTE : convert array indexes [0..numberOfTests-1] to ranks
     * [1..numberOfTests]. orden raw p-values low .. high test
     * p&lt(i/numberOfTests)*alpha from high to low (for i=numberOfTests..1) i*
     * (istar) first i such that the inequality is correct. reject hypothesis
     * for i=1..i* : labels 1..i* are overrepresented
     * <p>
     * adjusted p-value for i-th ranked p-value p_i^adj =
     * min(k=i..numberOfTests)[min(1,numberOfTests/k p_k)]</p>
     */
    @Override
    public void calculate() {
        String[] ordenedPvalues = super.getOrdenedPvalues();
        String[] ordenedGOLabels = super.getOrdenedGOLabels();
        // calculating adjusted p-values.
        BigDecimal min = new BigDecimal("" + 1);
        BigDecimal mkprk;
        for (int i = numberOfTests; i > 0; i--) {
            mkprk = (new BigDecimal("" + numberOfTests).multiply(new BigDecimal
        (ordenedPvalues[i - 1]))).divide(new BigDecimal(""
                    + i), RESULT_SCALE, BigDecimal.ROUND_HALF_UP);
            if (mkprk.compareTo(min) < 0) {
                min = mkprk;
            }
            adjustedPvalues[i - 1] = min.toString();

        }
        correctionMap = new HashMap<>();
        for (int i = 0; i < adjustedPvalues.length && i < ordenedGOLabels.length; i++) {
            correctionMap.put(ordenedGOLabels[i], adjustedPvalues[i]);
        }
    }

    /*--------------------------------------------------------------
	  GETTERS.
	--------------------------------------------------------------*/
    /**
     * getter for the map of corrected p-values.
     *
     * @return HashMap correctionMap.
     */
    @Override
    public HashMap<String, String> getCorrectionMap() {
        return correctionMap;
    }

    /**
     * getter for the adjusted p-values.
     *
     * @return String[] with the adjusted p-values.
     */
    public String[] getAdjustedPvalues() {
        return adjustedPvalues;
    }

}
