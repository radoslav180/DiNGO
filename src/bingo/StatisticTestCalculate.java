/* * Copyright (c) 2018 Institute of Nuclear Sciences Vinča
 * *
 * * Authors : Radoslav Davidović based on work done by Steven Maere, Karel Heymans
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
 * * and the Institute of Nuclear Sciences Vinča
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Institute of Nuclear Sciences Vinča
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Institute of Nuclear Sciences Vinča
 * * has been advised of the possibility of such damage. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program; if not, write to the Free Software
 * * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * *
 * *
 **/
package bingo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 *<p>Class that calculates statistic tests: Hypergeometric and Binomial. This class
 *    replaces old classes HypergeometricTestCalculate(under) and BinomialTestCalculate(under).
 *    Class is based on the aforementioned old classes</p>
 * @author Radoslav Davidović
 * 
 */
public class StatisticTestCalculate extends CalculateTestTask{
    /**
     * hashmap with as values the values of small n ; keys = GO labels.
     */
    private Map<Integer, Integer> mapSmallN;
    /**
     * hashmap with as values the values of small x ; keys = GO labels.
     */
    private Map<Integer, Integer> mapSmallX;
    /**
     * hashmap containing values for big N.
     */
    private Map<Integer, Integer> mapBigN;
    /**
     * hashmap containing values for big X.
     */
    private Map<Integer, Integer> mapBigX;
    /**
     * hashmap with the hypergeometric distribution results as values ; keys =
     * GO labels
     */
    private Map<Integer, String> statisticTestMap;
    
    /**
     * <p>Name of distribution</p>
     */
    private String distributionType;
    
    /**
     * <p>to calculate over- or under-representation</p>
     */
    private boolean isOver;

    
    public StatisticTestCalculate(DistributionCount dc, String distributionType, boolean isOver){
        dc.calculate();
        this.mapSmallN = dc.getMapSmallN();
        this.mapSmallX = dc.getMapSmallX();
        this.mapBigN = dc.getMapBigN();
        this.mapBigX = dc.getMapBigX();
        this.distributionType = distributionType;
        this.isOver = isOver;
    }
    

    @Override
    public void calculate() {
        IDistribution hd;
        statisticTestMap = new HashMap<>();

        HashSet<Integer> set = new HashSet<>(mapSmallX.keySet());
        
        Iterator<Integer> iterator = set.iterator();
        Integer id;
        Integer smallXvalue;
        Integer smallNvalue;
        Integer bigXvalue;
        Integer bigNvalue;
        
        while (iterator.hasNext()) {
            id = new Integer(iterator.next().toString());
            
            smallXvalue = mapSmallX.get(id);
            smallNvalue = mapSmallN.get(id);
            bigXvalue = mapBigX.get(id);
            bigNvalue = mapBigN.get(id);
            
            if(distributionType.equalsIgnoreCase("Hypergeometric test")){
                hd = new HypergeometricDistribution(smallXvalue, bigXvalue, smallNvalue, bigNvalue, isOver);
            } else if(distributionType.equalsIgnoreCase("Binomial test")){
                hd = new BinomialDistribution(smallXvalue, bigXvalue, smallNvalue, bigNvalue, isOver);
            } else{
                throw new IllegalArgumentException("Supported tests:\n1. Hypergeometric test\n2. Binomial test\n");
            }
            statisticTestMap.put(id, hd.calculateDistribution());
            
        }
    }

    @Override
    public Map<Integer, String> getTestMap() {
        return statisticTestMap;
    }

    @Override
    Map<Integer, Integer> getMapSmallX() {
        return mapSmallX;
    }

    @Override
    Map<Integer, Integer> getMapSmallN() {
        return mapSmallN;
    }

    @Override
    Map<Integer, Integer> getMapBigX() {
        return mapBigX;
    }

    @Override
    Map<Integer, Integer> getMapBigN() {
        return mapBigN;
    }


}
