package bingo;

// Adapted from : OntologyFlatFileReader.java in Cytoscape
//------------------------------------------------------------------------------
// $Revision: 1.4 $  $Date: 2006/07/24 13:29:50 $
//------------------------------------------------------------------------------
// Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute

/* * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
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
 **/

 /* * Modified Date: Mar.25.2005
 * * by : Steven Maere
 * * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * * Changes : 1) correction for GO labels with multiple GO identifiers, these should be included only once in the ontology.
 * * synonymous identifiers are remapped on a unique identifier for each GO label.
 * * 2) ensure that root node (Gene_ontology 003673 in the case of GO) gets read in as well
 * * 3) correction to avoid incorrect category names if they contain [ in the name
 * *
 * */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ontology.Ontology;
import ontology.OntologyTerm;

/**
 * This class is modified class BiNGOOntologyFlatFileReader from BiNGO created by
 * Steven Maere. The class read and parse ontology flat file.
 * The current class has following modifications:
 * <ol>
 *  <li>Some instance variables were removed (String fullText, String lines)</li>
 *  <li>there is no more dependencies on other classes in order to parse and read ontology file</li>
 *  <li>{@link #parse()} method was changed to remove duplicate code</li>
 *  <li>{@link #parseHeader(java.lang.String) } was slightly changed</li>
 *  <li>old method synonyms() was renamed to {@link #updateSynonyms(java.lang.String, int)}. Also,
 *      duplicated code was removed. The method call is in {@link #parse() }.</li>
 * </ol>
 * @author Steven Maere
 * @author Radoslav Davidović
 */
public class BiNGOOntologyFlatFileReader implements IOntologyReader {

    Ontology ontology;
    String curator = "unknown";
    String ontologyType = "unknown";
    String filename;
    
    Map<Integer, Integer> synonymHash;
    Map<String, Integer> goMap;

    public BiNGOOntologyFlatFileReader(File file) throws IllegalArgumentException, IOException, Exception {
        this(file.getPath());
    }

    public BiNGOOntologyFlatFileReader(String filename) throws IllegalArgumentException, IOException, Exception {
        this.filename = filename;
        this.synonymHash = new HashMap<>();
        this.goMap = new HashMap<>();
        parse();

    }

    private int stringToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private void parseHeader(String firstLine) throws Exception {
        
        String[] tokens = firstLine.split("\\)");

        String errorMsg = "error in OntologyFlatFileReader.parseHeader ().\n";
        errorMsg += "First line of " + filename + " must have form:\n";
        errorMsg += "   (curator=GO) (type=all) \n";
        errorMsg += "instead found:\n";
        errorMsg += "   " + firstLine + "\n";

        if (tokens.length != 2) {
            throw new IllegalArgumentException(errorMsg);
        }

        String[] curatorRaw = tokens[0].split("=");
        if (curatorRaw.length != 2) {
            throw new IllegalArgumentException(errorMsg);
        }
        curator = curatorRaw[1].trim();

        String[] typeRaw = tokens[1].split("=");
        if (typeRaw.length != 2) {
            throw new IllegalArgumentException(errorMsg);
        }
        ontologyType = typeRaw[1].trim();

    }

    private void updateSynonyms(String value, int id) {
        int firstLeftBracket = value.indexOf("[isa: ");
        if (firstLeftBracket < 0) {
            firstLeftBracket = value.indexOf("[partof: ");
        }

        String name = firstLeftBracket < 0 ? value.substring(0).trim()
                : value.substring(0, firstLeftBracket);
        if (!goMap.containsKey(name)) {
            goMap.put(name, id);
        }
        synonymHash.put(id, goMap.get(name));

    }

    private void parse() throws Exception {
        ontology = new Ontology(curator, ontologyType);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if(isHeader){
                    parseHeader(line);
                    isHeader = false;
                    continue;
                }
                int equals = line.indexOf("=");
                String idString = line.substring(0, equals).trim();
                int id = stringToInt(idString);
                String value = line.substring(equals + 1);

                int firstLeftBracket = value.indexOf("[isa: ");
                int markerEnd;
                String rawMarker, name;
                String[] allMarkers;
                OntologyTerm term = null;
                if (firstLeftBracket < 0) {
                    firstLeftBracket = value.indexOf("[partof: ");
                    markerEnd = value.indexOf("]", firstLeftBracket);
                    rawMarker = value.substring(firstLeftBracket + 9, markerEnd).trim();
                    name = value.substring(0).trim();
                    if (!ontology.containsTerm((synonymHash.get(id)))) {
                        term = new OntologyTerm(name, (synonymHash.get(id)));
                        ontology.add(term);
                    }
                } else {
                    markerEnd = value.indexOf("]", firstLeftBracket);
                    rawMarker = value.substring(firstLeftBracket + 6, markerEnd).trim();
                    name = value.substring(0, firstLeftBracket).trim();
                    term = new OntologyTerm(name, (synonymHash.get(id)));
                    ontology.add(term);
                }

                allMarkers = rawMarker.split(" ");
                for (String marker : allMarkers) {
                    if (term != null) {
                        term.addParent(synonymHash.get(stringToInt(marker)));
                    }
                }

                updateSynonyms(value, id);

            }
        } catch (IOException ex) {
            System.out.println("Unable to read ontology file " + filename);
        }

    }

    @Override
    public Ontology getOntology() {
        return ontology;
    }

    @Override
    public Map<Integer, Integer> getSynonymHash() {
        return synonymHash;
    }
}
