/*
 * Copyright (c) 2019. Institute of Nuclear Sciences Vinča
 *
 * Author: Radoslav Davidović 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package propagation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * <p>Class that contains methods responsible for the propagation of ontology terms (HPO terms)</p>
 * @author Radoslav Davidović
 */
public class Propagation {
    /**
     * <p>Ontology file in OBO format</p>
     */
    private String ontologyFile;
    /**
     * <p>Reference to the root term which is the last in the process of term propagation</p>
     */
    private String marker;

    //private static final String[] ROOTS = {"0008150", "0003674", "0005575"};//ontologies roots IDs
    /**
     * <p>Array containing HPO root terms</p>
     */
    private static final String[] HPROOTS = {"0000118", "0000005", "0012823", "0001461",
            "0000001", "0040279", "0031797"};// HPO ontologies roots "0040006"
    /**
     * <p>Map that contains term ID as key and parents of that terms as value</p>
     */
    private Map<String, Set<String>> synonymsMap;
    /**
     * <p>Maps each term to appropriate namespace</p>
     */
    private Map<String, String> termToNamespace;
    /**
     * <p>Leaf terms</p>
     */
    private Set<String> leafTerms;
    
    /**
     * <p>Constructor</p>
     * @param ontologyFile name of ontology file in OBO format
     */
    public Propagation(String ontologyFile) {
        this.ontologyFile = ontologyFile;
        init();
    }
    
    //initialize variables
    private void init() {

        termToNamespace = new HashMap<>();
        leafTerms = new HashSet<>();
        synonymsMap = new HashMap<>();
        termToNamespace.put("0000118", "phenotypic_abnormality");
        termToNamespace.put("0000005", "mode_of_inheritance");
        termToNamespace.put("0040279", "frequency");
        termToNamespace.put("0012823", "clinical_modifier");
        termToNamespace.put("0031797", "clinical_course");
        termToNamespace.put("0000001", "All");
        buildSynonymsMap();
    }

    //check if term is an ontology root
    private boolean isTermRoot(String GOTerm) {
        for (String id : HPROOTS) {
            if (GOTerm.equals(id)) {
                return true;

            }
        }

        return false;
    }
    //returns root term id if in set, otherwise returns empty string
    private String whatRootInside(Set<String> set) {
        Iterator<String> it = set.iterator();
        String test;
        while (it.hasNext()) {
            test = it.next();
            for (String id : HPROOTS) {
                if (test.equals(id)) {
                    return id;
                }
            }
        }

        return "";
    }

    //check if Set contains any of the ontology roots
    private boolean isItRootInside(Set<String> set) {
        Iterator<String> it = set.iterator();
        String test;
        while (it.hasNext()) {
            test = it.next();
            for (String id : HPROOTS) {
                if (test.equals(id)) {
                    return true;
                }
            }
        }

        return false;
    }

    //build map from obo file. Key is HPO term id, value is Set containing HPO terms with is_a relations to the key value
    private void buildSynonymsMap() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(ontologyFile));
            String line;
            String key = "";
            String value;
            boolean isInTermField = false;
            Set<String> obsoleteSet = new HashSet<>();
            Set<String> nonLeafTerms = new HashSet<>();
            Set<String> synonymsSet = new HashSet<>();

            while ((line = reader.readLine()) != null) {
                if (line.contains("[Term]")) {
                    isInTermField = true;
                }
                if (line.contains("[Typedef]")) {
                    break;
                }
                if (isInTermField) {
                    if (line.startsWith("id: HP")) {
                        key = line.split(":")[2];

                    }
                    if (line.startsWith("is_a:")) {
                        value = line.substring(9, 16);
                        synonymsSet.add(value);
                    }
                    if (line.startsWith("is_obsolete:")) {
                        obsoleteSet.add(key);
                    }
                    if (line.contains("[Term]")) {
                        if (!synonymsSet.isEmpty()) //leafs.add(key);
                        {
                            synonymsMap.put(key, new HashSet<>(synonymsSet));
                        } else {

                            synonymsMap.put(key, null);
                        }
                        //System.out.println("Map size " + synonymsMap.size());
                        nonLeafTerms.addAll(synonymsSet);
                        synonymsSet.clear();

                    }
                    //for last Term
                    if(!synonymsSet.isEmpty() && synonymsMap.get(key) == null){
                        synonymsMap.put(key, synonymsSet);
                    }
                    if (!synonymsMap.containsKey(key)) {

                        synonymsMap.put(key, null);
                    }

                }

            }
            //remove obsolete terms from the map
            for (String s : obsoleteSet) {
                if (synonymsMap.containsKey(s)) synonymsMap.remove(s);
            }
            //collect all leaf terms
            for (Map.Entry<String, Set<String>> en : synonymsMap.entrySet()) {
                if (!nonLeafTerms.contains(en.getKey())) {
                    leafTerms.add(en.getKey());
                }

            }

        } catch (IOException msg) {
            System.out.println(msg.getMessage());
        }
    }
    
    /**
     * <p>Propagate and map to namespace all leaf terms of the given ontology.</p>
     * @return List of all propagated terms
     */
    public List<String> propagateAndMapLeafTerms() {
        Set<String> allTerms = new HashSet<>();
        for (String l : leafTerms) {

            if (l.length() > 0) {
                try{
                    Set<String> props = new HashSet<>(Arrays.asList(propagateTerm(l)));
                    allTerms.addAll(props);
                    String namespace = termToNamespace.get(marker);
                    for (String p : props) {
                        termToNamespace.put(p, namespace);
                    }
                    termToNamespace.put(l, namespace);
                } catch(NullPointerException ex){
                    System.out.println(l + " " + ex.getCause());
                }

            }
        }

        return new ArrayList<>(allTerms);
    }

    /**
     * <p>Performs propagation of ontology term up to the root</p>
     * @param HPOTerm starting term for propagation
     * @return Array containing all propagated terms
     */
    public String[] propagateTerm(String HPOTerm) {

        Set<String> terms = new HashSet<>();//set that is going to contain propagated terms
        boolean isItStartTerm = true;
        if (!synonymsMap.containsKey(HPOTerm)) {
            //System.out.println("Key " + HPOTerm + " is not found!");
            return new String[]{};
        }
        Set<String> values;
        Set<String> newValues = new HashSet<>();

        while (true) {
            if (isItStartTerm) {
                values = synonymsMap.get(HPOTerm);
                terms.addAll(values);
                if (isItRootInside(values)) {
                    marker = whatRootInside(values);
                    break;
                }
                //System.out.println(values);
                isItStartTerm = false;
            } else {
                values = new HashSet<>(newValues);
                newValues.clear();
            }
            Iterator<String> it = values.iterator();
            while (it.hasNext()) {

                Set<String> temp = synonymsMap.get(it.next());
                if (temp != null) {
                    terms.addAll(temp);
                    newValues.addAll(temp);
                }

            }

            if (isItRootInside(newValues)) {
                marker = whatRootInside(newValues);
                break;
            }
        }

        String[] propagatedTerms = terms.toArray(new String[terms.size()]);

        return propagatedTerms;
    }
    /**
     * <p>Adds line containing namespace to each term in OBO file</p>
     * @param oboFile name of OBO file
     * @return 
     */
    public boolean addNamespaceInfoToOboFile(String oboFile){
        System.out.println("Adding namespace info to OBO file......");
        try (BufferedReader reader = new BufferedReader(new FileReader(this.ontologyFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(oboFile))) {
            String line;
            propagateAndMapLeafTerms();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id: HP")) {
                    writer.write(line);
                    writer.newLine();
                    if (termToNamespace.containsKey(line.split(":")[2])) {
                        writer.write("namespace: " + termToNamespace.get(line.split(":")[2]));
                        writer.newLine();
                    }
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
            System.out.println("Done!");
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public String getOntologyFile() {
        return ontologyFile;
    }

    public Map<String, Set<String>> getSynonymsMap() {
        return synonymsMap;
    }

    public Map<String, String> getTermToNamespace() {
        return termToNamespace;
    }

    public Set<String> getLeafTerms() {
        return leafTerms;
    }
    
    //main method
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("java Propagation -i <input HPO obo file> -o <output OBO file>");
            return;
        }

        String iFile = "", oFile = "";

        for(int i = 0; i < args.length - 1; i++){
            if(args[i].equals("-i")){
                iFile = args[i+1];
            }
            else if(args[i].equals("-o")){
                oFile = args[i+1];
            }
        }

        if(iFile.length() == 0){
            System.out.println("Please specify HPO OBO file!");
            return;
        }
        if(oFile.length() == 0){
            System.out.println("Please specify name of output file!");
            return;
        }
        
        Propagation test = new Propagation(iFile);
        //test.buildSynonymsMap();
        boolean result = test.addNamespaceInfoToOboFile(oFile);
        if(!result){
            System.out.println("Operation failed!");
        }
    }
}
