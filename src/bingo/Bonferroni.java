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
 * * Description: Class implementing the Bonferroni multiple testing correction.         
 **/
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * ************************************************************************
 * Bonferroni.java: Steven Maere & Karel Heymans (c) March 2005<br>
 * Modified by Radoslav Davidović January 2018
 * <p>There are significant changes in this version:
 *  <ol>
 *      <li>The following variable were removed:
 *          <ul>
 *              <li><code>HashEntry[] hash</code></li>
 *              <li><code>static String[] ordenedGOLabels</code></li>
 *              <li><code>static String[] ordenedPvalues</code></li>
 *              <li><code>static int numberOfTests</code></li>
 *              <li><code>static String[] goLabels</code></li>
 *              <li><code>static String[] pvalues</code></li>
 *              <li><code>static BigDecimal alpha</code></li>
 *          </ul>
 *      </li>
 *      <li>variable {@link #adjustedPvalues} is instance variable not a static one</li>
 *      <li>new constructor {@link #Bonferroni(java.util.Map) }</li>
 *      <li>inner classes <code>HashEntry</code> and <code>HashComprator</code> were removed</li>
 * </ol>
 * </p>
 * <p>
 * Class implementing the Bonferroni multiple testing correction.
 * </p> ************************************************************************
 */
public class Bonferroni extends CalculateCorrectionTask {

    /*--------------------------------------------------------------
	FIELDS.
	--------------------------------------------------------------*/

    /**
     * the adjusted p-values ordered in ascending order. Bila static
     */
    private String[] adjustedPvalues;

    /**
     * hashmap with the results (adjusted p-values) as values and the GO labels
     * as keys. Bila static
     */
    private HashMap<String, String> correctionMap;

    /*--------------------------------------------------------------
	CONSTRUCTOR.
	--------------------------------------------------------------*/
    /**
     * Constructor.
     *
     * @param golabelstopvalues Hashmap of Strings with the goLabelsLoc and
     * their pvaluesLoc.
     *
     */
    public Bonferroni(Map golabelstopvalues) {

        super(golabelstopvalues);
        this.adjustedPvalues = new String[numberOfTests];

    }

    /*--------------------------------------------------------------
	METHODS.
	--------------------------------------------------------------*/
    /**
     * method that calculates the bonferroni procedure p &lt alpha/numberOfTests i* (istar)
 first i such that the inequality is correct. reject hypotheses for
 i=1...i* adjusted p-value = numberOfTests*p
     */
    @Override
    public void calculate() {
        String[] ordenedPvalues = super.getOrdenedPvalues();
        // calculating adjusted p-values.
        BigDecimal min = new BigDecimal("" + 1);
        BigDecimal mp;

        for (int i = 0; i < numberOfTests; i++) {
            mp = new BigDecimal("" + numberOfTests).multiply(new BigDecimal(ordenedPvalues[i]));
            if (mp.compareTo(min) < 0) {
                adjustedPvalues[i] = mp.toString();
            } else {
                adjustedPvalues[i] = min.toString();
            }
        }

        String[] sortedGOLabels = super.getOrdenedGOLabels();
        correctionMap = new HashMap<>();
        for (int i = 0; i < adjustedPvalues.length && i < sortedGOLabels.length; i++) {
            correctionMap.put(sortedGOLabels[i], adjustedPvalues[i]);
        }
    }

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
