package bingo;

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
 * *
 * * Modified by : Steven Maere
 * * Date: Apr.11.2005
 * * Description: Class that parses default annotation files in function of the chosen organism.
 **/

import ontology.Annotation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description: Class that parses default annotation files in function of the chosen organism.
 * Modified by Radoslav Davidović 2018
 * <ol>Changes:
 *  <li>class implements {@link IAnnotation} interface</li>
 *  <li>Removed instance variables
 *      <ol>
 *          <li>private <code>String fullText</code></li>
 *          <li>private <code>String[] lines</code></li>
 *      </ol>
 *  </li>
 * <li>new constructor {@link #BiNGOConsortiumAnnotationReader(java.lang.String, java.lang.String, java.util.Map, java.util.Set, java.lang.String, java.lang.String) }</li>
 *
 *  <li>Class does not rely on method provided by other classes. Annotation file is read and
 *  parsed by method {@link #parse()}</li>
 *  <li>The following methods were removed:
 *      <ol>
 *        <li><code>private int parseHeader()</code></li>
 *        <li>private <code>void parse(int)</code> replaced by {@link #parse()}</li>
 *      </ol>
 *  </li>
 * <li>new method {@link #retrieveAlias(java.lang.String) }</li>
 * </ol>
 */
public class BiNGOConsortiumAnnotationReader implements IAnnotation {

    private Annotation annotation;
    /**
     * type for Annotation constructor
     */
    private String annotationType;
    /**
     * species for Annotation constructor
     */
    private String species;
    /**
     * curator for Annotation constructor
     */
    private String curator;

    private String filename;

    private Map<Integer, Integer> synonymHash;
    private HashMap<String, HashSet<String>> alias;

    private Set<String> deleteCodes;

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
    /*
    public BiNGOConsortiumAnnotationReader(File file, Map<Integer, Integer> synonymHash, Set<String> deleteCodes,
                                           String annotationType, String curator) {
        this(file.getPath(), synonymHash, deleteCodes, annotationType, curator);
    }
    */
    // -------------------------------------------------------------------------
    public BiNGOConsortiumAnnotationReader(String filename, String mappingFile, 
            Map<Integer, Integer> synonymHash, Set<String> deleteCodes,
                                           String annotationType, String curator) {
       
        this.filename = filename;
        this.species = filename;
        this.annotationType = annotationType;
        this.curator = curator;
        this.synonymHash = synonymHash;
        this.deleteCodes = deleteCodes;

        parse();
        if(mappingFile != null){
            retrieveAlias(mappingFile);
        }
        

    }

    private int stringToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private void updateAlias(String valuef, String values){
        if (alias.containsKey(valuef)) {
            alias.get(valuef).add(values);
        } else {
            HashSet<String> tmp = new HashSet<>();
            tmp.add(values);
            alias.put(valuef, tmp);
        }
    }

    private void parse() {
        annotation = new Annotation(species, annotationType, curator);
        alias = new HashMap<>();
        HashSet<Integer> orphans = new HashSet<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))){
            String line;
            String[] tokens;

            while((line = reader.readLine()) != null){
                if(line.startsWith("!")){
                    continue;
                }

                tokens = line.split("\t");
                String evidenceCode = tokens[6].trim().toUpperCase();
                String qualifier = tokens[3].trim();

                if (!deleteCodes.contains(evidenceCode) && (qualifier.length() == 0)) {
                    String primaryID = tokens[1].trim().toUpperCase();
                    updateAlias(primaryID, primaryID);

                    String secondaryID = tokens[2].trim().toUpperCase();
                    updateAlias(secondaryID, primaryID);

                    String[] aliases = tokens[10].split("\\|");
                    for (String alias1 : aliases) {
                        updateAlias(alias1, primaryID);
                    }
                    String goID = tokens[4].trim().toUpperCase().substring(3);
                    int id = stringToInt(goID);
                    Integer id2 = id;
                    Integer mainId = synonymHash.get(id2);
                    if (mainId != null) {
                        if ((primaryID.length() != 0) && (id != -1)) {
                            annotation.add(primaryID, mainId);
                        }
                        consistency = true;
                    } else {
                        orphans.add(id2);
                        orphansFound = true;
                    }
                }

            }
        } catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    //method that take alias from tab delimited file 
    private void retrieveAlias(String fileName){
        try(BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String line;
            String[] fields;
            while((line = reader.readLine()) != null){
                fields = line.trim().split("\\t");
                for(String f:fields){
                    if(alias.containsKey(f)){
                        HashSet<String> temp = alias.get(f);
                        temp.addAll(Arrays.asList(fields));
                        alias.put(f, temp);
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
    public boolean getOrphans() {
        return orphansFound;
    }

    @Override
    public boolean getConsistency() {
        return consistency;
    }

    @Override
    public HashMap<String, HashSet<String>> getAlias() {
        return alias;
    }
}
