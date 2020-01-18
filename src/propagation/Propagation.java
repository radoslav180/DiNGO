/*
 * Copyright (c) 2019.
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
    //namespace or subontology
    private String namespace = "";
    //map that contains term as key and its parents as value
    private final Map<Term, List<Term>> termToParents;
    private final OboParser oboParser;

    public Propagation(OboParser oboParser) {

        termToParents = new HashMap<>();
        this.oboParser = oboParser;
    }

    private void initTemToParentsMap() {
        List<Term> terms = oboParser.getAllTerms();

        for (Term t : terms) {
            try {
                List<Term> temp1 = t.getParentTerms();
                termToParents.put(t, temp1);
            } catch (NullPointerException ex) {
                System.out.println(ex.getMessage());

            }

        }
    }
    /**
     * <p>Method finds path from specified term to root term</p>
     * @param term 
     * @return list of terms that represents path from term to the root
     */
    public List<Term> propagateTerm(Term term) {
        if (termToParents.isEmpty()) {
            initTemToParentsMap();
        }

        Set<Term> props = new HashSet<>();//contains propagated terms
        List<Term> terms = new ArrayList<>();//contains parents of a term
        List<Term> tempList = new ArrayList<>();
        terms.add(term);

        while (true) {
            //if term does not have parents, that means we reached root term
            if (terms.isEmpty()) {
                break;
            }
            for (Term t : terms) {
                if (termToParents.containsKey(t)) {

                    if (termToParents.get(t).isEmpty()) {
                        break;
                    }
                    //test if term is subontology root term
                    if(termToParents.get(t).size() == 1 && termToParents.get((termToParents.get(t).get(0))).isEmpty()){
                        //System.out.println("Namespace: " + t.getName());
                        namespace = t.getName().toLowerCase().replace(" ", "_");
                    }
                    tempList.addAll(termToParents.get(t));
                    props.add(t);
                } else {
                    System.out.println("Term " + t.getId() + " not found!");
                    return terms;
                }
            }
            terms = new ArrayList<>(tempList);
            tempList.clear();
        }
        return new ArrayList<>(props);
    }
    
    //associates each term to appropriate namespace
    private Map<Term, String> mapTermToNamespace() {
        Map<Term, String> namespaces = new HashMap<>();
        List<Term> leafTerms = oboParser.getLeafTerms();
        List<Term> props;
        for (Term t : leafTerms) {
            props = propagateTerm(t);
            
            if (props.size() > 1) {
                for (Term p : props) { 
                    namespaces.put(p, namespace);
                }
               
            }

        }
        return namespaces;
    }
    
    /**
     * <p>Adds line containing namespace to each term in OBO file. Returns true if
     * operation was successful
     * </p>
     * @param oboFile name of OBO file
     * @return returns true if operation succeeds otherwise false
     */
    public boolean addNamespaceInfoToOboFile(String oboFile){
        String oldObo = oboParser.getFileName();
        System.out.println("Adding namespace info to OBO file...");
        try (BufferedReader reader = new BufferedReader(new FileReader(oldObo));
                BufferedWriter writer = new BufferedWriter(new FileWriter(oboFile))) {
            String line;
            Map<Term, String> termToNamespace = mapTermToNamespace();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("id: HP")) {
                    writer.write(line);
                    writer.newLine();
                    if (termToNamespace.containsKey(new Term(line.split(":")[2], ""))) {
                        writer.write("namespace: " + termToNamespace.get(new Term(line.split(":")[2], "")));
                        writer.newLine();
                    }
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
            System.out.println("Done!");
            
        } catch (IOException ex) {
            //System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

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
        
        Propagation test = new Propagation(new OboParser(iFile));
        test.addNamespaceInfoToOboFile(oFile);
    }
}
