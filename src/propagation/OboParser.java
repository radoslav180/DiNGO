/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package propagation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *<p>Class that parses OBO file. The class was created with HPO ontology in mind.
 * It 
 * </p>
 * @author Radoslav Davidović
 */
public class OboParser {
    
    private final String fileName;//name of OBO file
    private String formatVersion;//format version of OBO file
    private String ReleaseDate;//release date of OBO file
    private final List<Term> leafTerms = new ArrayList<>();//contains all leaf terms of an ontology
    private final List<Term> nonLeafTerms = new ArrayList<>();//contains all non-leaf terms of an ontology
    
    /**
     * <p>Constructor</p>
     * @param fileName Name of OBO file
     */
    public OboParser(String fileName) {
        this.fileName = fileName;
    }
    
    //init Term object with data contained in Term section of OBO file.
    //id: HP:0000002
    //name: Abnormality of body height
    //def: "Deviation from the norm of height with respect to that which is expected according to age and gender norms." [HPO:probinson]
    //synonym: "Abnormality of body height" EXACT layperson []
    //xref: UMLS:C4025901
    //is_a: HP:0001507 ! Growth abnormality
    //created_by: peter
    //creation_date: 2008-02-27T02:20:00Z
    private Term initTerm(String OboTerm) {
        String[] details = OboTerm.split("\\r?\\n");
        Term term;
        String id = "";
        String name = "";
        String namespace = "";
        List<Term> parents = new ArrayList<>();
        List<String> altIds = new ArrayList<>();
        boolean IsObsolete = false;
        for (String d : details) {
            if (d.startsWith("id:")) {
                id = d.split(":")[2].trim();
            }
            
            if(d.startsWith("alt_id:")){
                altIds.add(d.split(":")[2].trim());
            }

            if (d.startsWith("name:")) {
                name = d.split(":")[1].trim();
            }
            
            if (d.startsWith("namespace:")) {
                namespace = d.split(":")[1].trim();
            }

            if (d.startsWith("is_obsolete: true")) {
                IsObsolete = true;
            }

            if (d.startsWith("is_a:")) {
                Term term1 = new Term(d.split("!")[0].substring(9).trim(), d.split("!")[1].trim());
                parents.add(term1);
                nonLeafTerms.add(term1);
            }
            
            if(d.contains(" part_of ")){
                String termID;
                String termName;
                String[] fields = d.split("\\s");
                termID = fields[2];
                termName = fields[4];
                parents.add(new Term(termID, termName));
            }
        }
        
        term = new Term(IsObsolete, id, name, namespace, parents);
        term.setAltIDList(altIds);
        return term;
    }
    
    /**
     * <p>Method that parses OBO file and returns String containing all ontology
     * terms
     * </p>
     * @return string containing all ontology terms
     */
    public String parseOboFile() {
        StringBuilder bText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean toAdd = false;

            while ((line = reader.readLine()) != null) {
                //do not consider Typedef sections
                if (line.contains("[Typedef]")) {
                    break;
                }

                if (line.contains("[Term]")) {
                    toAdd = true;
                }

                if (toAdd) {
                    bText.append(line);
                    bText.append("\n");

                } else {
                    if (line.startsWith("format-version")) {
                        formatVersion = line.substring(15).trim();
                    }
                    if (line.startsWith("data-version")) {
                        ReleaseDate = line.substring(13).trim();
                    }
                }
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return bText.toString();
    }
    
    /**
     * <p>Returns List of all ontology Terms</p>
     * @return List of Terms
     */
    public List<Term> getAllTerms() {
        String text = parseOboFile();
        String[] OboTerms = text.split("\\[Term\\]");
        List<Term> termsList = new ArrayList<>();
        for (String ot : OboTerms) {
            termsList.add(initTerm(ot));
        }

        return termsList;
    }
    
    /**
     * <p>Returns info about OBO file format version</p>
     * @return String representing OBO format version
     */
    public String getFormatVersion() {
        return formatVersion;
    }
    
    /**
     * <p>Returns info about OBO release date</p>
     * @return OBO release date as string
     */
    public String getReleaseDate() {
        return ReleaseDate;
    }
    
    /**
     * <p>Return OBO file name</p>
     * @return OBO file name as string
     */
    public String getFileName(){
        return fileName;
    }
    
    /**
     * <p>Returns List of leaf Terms</p>
     * @return List of leaf Terms
     */
    public List<Term> getLeafTerms() {
        if(leafTerms.isEmpty()){
            Set<Term> allSet = new HashSet<>();
            Set<Term> nonLeafSet = new HashSet<>();
            allSet.addAll(getAllTerms());
            nonLeafSet.addAll(nonLeafTerms);
            allSet.removeAll(nonLeafSet);
            leafTerms.addAll(allSet);
        }

        return leafTerms;
    }
    
}
