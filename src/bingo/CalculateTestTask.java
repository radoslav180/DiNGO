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
 * * Description: Interface for statistical tests.
 **/

import java.util.Map;

/**
 * <p>Modified by Radoslav Davidović</p>
 * <ol>Changes:
 * <li>does not extends BingoTask class</li>
 * <li>added {@link #calculate()}</li>
 * </ol>
 */

public abstract class CalculateTestTask {

    // implement for statistical tests
    public abstract Map<Integer, String> getTestMap();

    abstract Map<Integer, Integer> getMapSmallX();

    abstract Map<Integer, Integer> getMapSmallN();

    abstract Map<Integer, Integer> getMapBigX();

    abstract Map<Integer, Integer> getMapBigN();

    abstract public void calculate() throws Exception;

}
