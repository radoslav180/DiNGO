package bingo;

// Adapted from : AnnotationFlatFileReader.java in Cytoscape
//------------------------------------------------------------------------------
// $Revision: 1.5 $  $Date: 2006/07/24 13:29:50 $
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
 * * Changes : case-insensitive input ; correction for GO labels with multiple GO identifiers, 
 * * synonymous identifiers are remapped on a unique identifier for each GO label through a synonyms HashMap 
 * * made in the bingoOntologyFlatFileReader class
 * * Modified by Radoslav Davidović
 * * Changes: new constructor, new method void retrieveAlias(String)
 * */
import ontology.Annotation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BiNGOAnnotationFlatFileReader implements IAnnotation {

    private Annotation annotation;
    private String annotationType;
    private String species;
    private String curator;
    private String filename;
    private Map<Integer, Integer> synonymHash;
    private Map<String, HashSet<String>> alias;

    /**
     * true if there are categories in the annotation which are not defined in
     * the ontology
     */
    private boolean orphansFound = false;
    /**
     * false if none of the categories in the annotation match the ontology
     */
    private boolean consistency = false;

    // -------------------------------------------------------------------------
    //public BiNGOAnnotationFlatFileReader(File file, Map<Integer, Integer> synonymHash){
      //  this(file.getPath(), synonymHash);
    //}

    // -------------------------------------------------------------------------
    BiNGOAnnotationFlatFileReader(String filename, String mappingFile, Map<Integer, Integer> synonymHash){
        //System.out.println ("AnnotationFlatFileReader on " + filename);
        this.filename = filename;

        this.synonymHash = synonymHash;

        parse();
        if(mappingFile != null)
            retrieveAlias(mappingFile);
        /*
        for (Map.Entry<String, HashSet<String>> entry : alias.entrySet()) {
            String key = entry.getKey();
            HashSet<String> value = entry.getValue();
            System.out.println(key + " -> " + value);
            
        }
        */
        System.out.println("Reading done!");
    }

    // -------------------------------------------------------------------------
    private int stringToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    private void parseHeader(String firstLine) {
        String[] tokens = firstLine.trim().split("\\)");

        String errorMsg = "error in AnnotationFlatFileReader.parseHeader ().\n";
        errorMsg += "First line of " + filename + " must have form:\n";
        errorMsg += "   (species=Homo sapiens) (type=Biological Process) (curator=GO)\n";
        errorMsg += "instead found:\n";
        errorMsg += "   " + firstLine + "\n";

        if (tokens.length != 3) {
            throw new IllegalArgumentException(errorMsg);
        }

        for (String token : tokens) {
            String[] subTokens = token.split("=");
            if (subTokens.length != 2) {
                throw new IllegalArgumentException(errorMsg);
            }
            String name = subTokens[0].trim();
            String value = subTokens[1].trim();

            if (name.equalsIgnoreCase("(species")) {
                species = value;
            } else if (name.equalsIgnoreCase("(type")) {
                annotationType = value;
            } else if (name.equalsIgnoreCase("(curator")) {
                curator = value;
            }
        }

    } // parseHeader
    // -------------------------------------------------------------------------

    private void parse() {
        //System.out.println("Parsing annotation...");

        HashSet<Integer> orphans = new HashSet<>();
        alias = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))){
            String line;
            String[] tokens;
            boolean isHeader = true;
            while((line = reader.readLine()) != null){
                if(isHeader){
                    parseHeader(line);
                    annotation = new Annotation(species, annotationType, curator);
                    isHeader = false;
                } else{
                    tokens = line.split("=");
                    String entityName = tokens[0].trim().toUpperCase();
                    int id = stringToInt(tokens[1].trim());

                    Integer mainId = synonymHash.get(id);
                    if (mainId != null) {
                        if ((entityName.length() != 0) && (id != -1)) {
                            annotation.add(entityName, mainId);
                            
                            HashSet<String> tmp = new HashSet<>();
                            tmp.add(entityName);
                            alias.put(entityName, tmp);
                            
                        }
                        consistency = true;
                    } else {
                        orphans.add(id);
                        orphansFound = true;
                    }
                }
            }
        } catch(IOException ex){
            System.out.println(ex.getMessage());
        }

        //System.out.println("Annotation file processed!");
        
    } // parse
    // -------------------------------------------------------------------------
    
    private void retrieveAlias(String mappingFile){
       
        try(BufferedReader reader = new BufferedReader(new FileReader(mappingFile))){
            String line;
            String[] fields;
            while((line = reader.readLine()) != null){
                fields = line.trim().split("\\t");
                for(String f:fields){
                    if(alias.containsKey(f)){
                        HashSet<String> temp = alias.get(f);
                        temp.addAll(Arrays.asList(fields));
                        alias.put(f, temp);
                        //System.out.println(alias.get(f));
                    } else{
                        alias.put(f, new HashSet<>(Arrays.asList(fields)));
                    }
                    
                }
                
            }
        } catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    
    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public Map<String, HashSet<String>> getAlias() {
        return alias;
    }

    @Override
    public boolean getConsistency() {
        return consistency;
    }

    @Override
    public boolean getOrphans() {
        return orphansFound;
    }
}
