/*
 * Copyright (c) 2019.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package bingo;

import java.util.Map;
import ontology.Ontology;

/**
 *<p>Interface to be implemented by class responsible for parsing ontology files</p>
 * @author radoslav davidović
 * @version %I% %G%
 * @since 1.1
 */
public interface IOntologyReader {
    Ontology getOntology();
    Map getSynonymHash();
    default String getOntologyType(){
        return "";
    }
}
