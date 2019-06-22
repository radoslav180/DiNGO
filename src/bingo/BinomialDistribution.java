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
 * * Description: Class that calculates the value of 1 - Cumulative Binomial Distribution for given parameters.         
 **/
import java.math.BigDecimal;
import java.math.BigInteger;

import cern.jet.stat.Probability;

/**
 * *****************************************************************<br>
 * BinomialDistribution.java Steven Maere & Karel Heymans (c) March 2005<br>
 * Modified by Radoslav Davidović 2018
 * <ol>Changes:
 *  <li>All static variables are now instance variables</li>
 *  <li>class implements {@link IDistribution} interface</li>
 *  <li>methods {@link #calculateBinomialDistribution()} and {@link #decimalPow(java.math.BigDecimal, java.math.BigInteger)} are private</li>
 * </ol>
 * --------------------------
 * <p>
 * Class that calculates the value of 1 - Cumulative Binomial Distribution for
 * given parameters.</p>
 * ******************************************************************
 */
public class BinomialDistribution implements IDistribution {

    /*--------------------------------------------------------------
	FIELDS.
	--------------------------------------------------------------*/
    // x out of X genes in a cluster A belong to GO category B which
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
    /* chance of success */
    private double p;
    /**
     * <p>If true calculates over-representation, otherwise underrepresentation</p>
     */
    private boolean isOver = true;
    
    /**
     * scale of result.
     */
    private static final int SCALE_RESULT = 100;

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
    public BinomialDistribution(int x, int bigX, int n, int bigN) {
        this.x = x;
        this.bigX = bigX;
        this.n = n;
        this.bigN = bigN;
        this.p = n * 1.0 / bigN;
    }
    
     public BinomialDistribution(int x, int bigX, int n, int bigN, boolean isOver) {
        this.x = x;
        this.bigX = bigX;
        this.n = n;
        this.bigN = bigN;
        this.p = n * 1.0 / bigN;
        this.isOver = isOver;
    }

    /*--------------------------------------------------------------
	METHODS.
	--------------------------------------------------------------*/
    /**
     * method that conducts the calculations. p = 1 -
     * sum{C(X,i)*(n/N)^i*(1-n/N)^(X-i)} for i=0 ... x-1
     *
     * @return String with value of calculations.
     */
    private String calculateBinomialDistribution() {

        double tmp = isOver ? Probability.binomialComplemented(x - 1, bigX, p) : Probability.binomial(x, bigX, p);
        BigDecimal sum = new BigDecimal(tmp);
        return sum.toString();

    }

    /**
     * Method that calulates a bigdecimal to a certain power (BigInteger). e.g.
     * 0.002^3.
     *
     * @param x the BigDecimal
     * @param pow the power
     * @return BigDecimal the result
     */
    private BigDecimal decimalPow(BigDecimal x, BigInteger pow) {

        if (pow.equals(new BigInteger("0"))) {
            return new BigDecimal("1");
        } else {
            BigDecimal product = x;

            for (BigInteger i = new BigInteger("1"); !i.equals(pow); i = i.add(new BigInteger("1"))) {
                product = product.multiply(x);
            }
            return product.setScale(SCALE_RESULT, BigDecimal.ROUND_HALF_UP);
        }
    }

    @Override
    public String calculateDistribution() {
        return calculateBinomialDistribution();
    }

}
