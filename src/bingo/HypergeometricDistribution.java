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
 * * Description: Class that calculates the Hypergeometric probability P(x or more |X,N,n) for given x, X, n, N.    
 * * Modified by Radoslav Davidović 11.6.2018.
 **/
import cern.jet.stat.Gamma;

/**
 * *****************************************************************
 * HypergeometricDistribution.java Steven Maere & Karel Heymans (c) March 2005
 * Modified by Radoslav Davidović 2018
 * <ol>Changes:
 *  <li>All static variables are now instance variables</li>
 *  <li>class implements {@link IDistribution} interface</li>
 *  <li>method {@link #calculateHypergDistr()} is private</li>
 *  <li>new constructor {@link #HypergeometricDistribution(int, int, int, int, boolean)}</li>
 * </ol>
 * 
 * -------------------------------
 * <p>
 * Class that calculates the Hypergeometric probability P(x or more |X,N,n) for
 * given x, X, n, N.</p>
 * ******************************************************************
 */
public class HypergeometricDistribution implements IDistribution{

    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/
    /**
     * if variable is true over-representation will be calculated
     */
    private boolean isOver = true;
    
    // x out of X genes in cluster A belong to GO category B which
    // is shared by n out of N genes in the reference set.
    /**
     * number of successes in sample.
     */
    private int x;
    /**
     * sample size.
     */
    private int bigX;
    /**
     * number of successes in population.
     */
    private int n;
    /**
     * population size.
     */

    private int bigN;
    /**
     * scale of result.
     */

    //private static final int SCALE_RESULT = 100;

    /*--------------------------------------------------------------
    CONSTRUCTOR.
    --------------------------------------------------------------*/
    /**
     * constructor with as arguments strings containing numbers.
     *
     * @param x number of genes with GO category B in cluster A.
     * @param bigX number of genes in cluster A.
     * @param n number of genes with GO category B in the whole genome.
     * @param bigN number of genes in whole genome.
     */
    public HypergeometricDistribution(int x, int bigX, int n, int bigN) {
        this.x = x;
        this.bigX = bigX;
        this.n = n;
        this.bigN = bigN;
    }
    
    public HypergeometricDistribution(int x, int bigX, int n, int bigN, boolean isOver) {
        this(x, bigX, n, bigN);
        this.isOver = isOver;
    }
    
    /*--------------------------------------------------------------
    METHODS.
    --------------------------------------------------------------*/
    /**
     * method that conducts the calculations. P(x or more |X,N,n) = 1 -
     * sum{[C(n,i)*C(N-n, X-i)] / C(N,X)} for i=0 ... x-1
     *
     * @return String with result of calculations.
     */
    private String calculateHypergDistr() {
        if (bigN >= 2) {
            double sum = 0;
            //mode of distribution, integer division (returns integer <= double result)!
            int mode = (bigX + 1) * (n + 1) / (bigN + 2);
            int i;
            int newX = (isOver == false) ? x + 1 : x;
           
            if (newX >= mode) {
                i = newX;
                while ((bigN - n >= bigX - i) && (i <= Math.min(bigX, n))) {
                    double pdfi = Math.exp(Gamma.logGamma(n + 1) - Gamma.logGamma(i + 1) - Gamma.logGamma(n - i + 1) + Gamma.logGamma(bigN - n + 1) - Gamma.logGamma(bigX - i + 1) - Gamma.logGamma(bigN - n - bigX + i + 1) - Gamma.logGamma(bigN + 1) + Gamma.logGamma(bigX + 1) + Gamma.logGamma(bigN - bigX + 1));
                    sum = sum + pdfi;
                    i++;
                }
                if(isOver == false) sum = 1 - sum;
            } else {
                i = newX - 1;
                while ((bigN - n >= bigX - i) && (i >= 0)) {
                    double pdfi = Math.exp(Gamma.logGamma(n + 1) - Gamma.logGamma(i + 1) - Gamma.logGamma(n - i + 1) + Gamma.logGamma(bigN - n + 1) - Gamma.logGamma(bigX - i + 1) - Gamma.logGamma(bigN - n - bigX + i + 1) - Gamma.logGamma(bigN + 1) + Gamma.logGamma(bigX + 1) + Gamma.logGamma(bigN - bigX + 1));
                    sum = sum + pdfi;
                    i--;
                }
                if(isOver == true) sum = 1 - sum;
            }
            return (new Double(sum)).toString();
        } else {
            return (new Double(1)).toString();
        }
    }
    
    public boolean getIsOver(){return isOver;}
    
    public void setIsOver(boolean newValue){isOver = newValue;}

    @Override
    public String calculateDistribution() {
        return calculateHypergDistr();
    }
}
