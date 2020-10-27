package bingo;

/* * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * *
 * * Authors : Steven Maere
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
 * * Authors: Steven Maere
 * * Date: Apr.20.2005
 * * Description: Interface for multiple testing correction.
 * * Modified by Radoslav Davidović 2018
 **/

import java.math.BigDecimal;
import java.util.*;

/**
 * <ol>Changes:
 * <li>does not extends BingoTask class</li>
 * <li>added {@link #calculate()}</li>
 * <li>added constructor {@link #CalculateCorrectionTask(Map)}</li>
 * <li>the following instance variables were added:
 *  <ul>
 *      <li>{@link #hash}</li>
 *      <li>{@link #ordenedGOLabels}</li>
 *      <li>{@link #ordenedPvalues}</li>
 *      <li>{@link #numberOfTests}</li>
 *  </ul>
 *  <li>the following methods were added:</li>
 *      <ul>
 *          <li>{@link #parse()}</li>
 *
 *      </ul>
 *  <li>methods {@link #getOrdenedGOLabels()} and {@link #getOrdenedPvalues()} are not abstract any more</li>
 * </li>
 * </ol>
 */

public abstract class CalculateCorrectionTask {
    /**
     * <p>array containing pairs of terms and corresponding p values.</p>
     *
     */
    private HashEntry[] hash;

    /**
     * the goLabels ordered according to the ordered p values.
     */
    private String[] ordenedGOLabels;
    /**
     * the raw p-values ordered in ascending order.
     */
    private String[] ordenedPvalues;

    /**
     * the number of tests.
     */
    protected int numberOfTests;

    /**
     * <p>Constructor that initializes instance variables</p>
     * @param golabelstopvalues Hashmap of Strings with the goLabels and their p values.
     */
    public CalculateCorrectionTask(Map golabelstopvalues) {

        // Get all the go labels and their corresponding pvalues from the map

        Iterator iteratorGoLabelsSet = golabelstopvalues.keySet().iterator();

        hash = new HashEntry[golabelstopvalues.size()];

        String goLabel;
        for (int i = 0; iteratorGoLabelsSet.hasNext(); i++) {
            goLabel = iteratorGoLabelsSet.next().toString();
            hash[i] = new HashEntry(goLabel, golabelstopvalues.get(new Integer(goLabel)).toString());
        }

        Arrays.sort(hash, new Comparator<HashEntry>() {
            @Override
            public int compare(HashEntry o1, HashEntry o2) {
                return (new BigDecimal((o1).value)).compareTo(new BigDecimal((o2).value));
            }
        });
        this.numberOfTests = golabelstopvalues.size();
        parse();

    }

    private void parse() {
        ordenedGOLabels = new String[numberOfTests];
        ordenedPvalues = new String[numberOfTests];
        for (int i = 0; i < numberOfTests; i++) {
            ordenedGOLabels[i] = hash[i].key;
            ordenedPvalues[i] = hash[i].value;
        }

    }

    private final class HashEntry {

        public String key;
        public String value;

        public HashEntry(String k, String v) {
            this.key = k;
            this.value = v;
        }
    }

    protected String[] getOrdenedPvalues(){
        return ordenedPvalues;
    }

    protected String[] getOrdenedGOLabels(){
        return ordenedGOLabels;
    }

    abstract Map<String, String> getCorrectionMap();

    abstract public void calculate();

}
