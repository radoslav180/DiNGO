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
 **/

/* * Created Date: Mar.15.2010
 * * by : Steven Maere
 * * Copyright (c) 2005-2010 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * * Modified by Radoslav Davidović 2018
 * */

import ontology.Ontology;
import ontology.OntologyTerm;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * <p>
 * Class responsible for reading and parsing ontology provided in obo format</p>
 * <p>
 * There are some changes that were introduced in current version.
 * <ol>Changes:
 * <li>class implements {@link IOntologyReader} interface</li>
 * <li>{@link #parse()} method reads and parses ontology simultaneously</li>
 * <li>instance variables have private scope instead package-private one</li>
 * <li>some instance variables were removed as unnecessary</li>
 * </ol>
 * </p>
 */
public class BiNGOOntologyOboReader implements IOntologyReader {

    private Ontology ontology;
    private Ontology fullOntology;
    private String curator = "unknown";
    private String ontologyType = "unknown";
    private String namespace;
    private String filename;
    private HashMap<Integer, Integer> synonymHash;
    private Map<String, String> termToNamespace;
    public BiNGOOntologyOboReader(File file, String namespace) {
        this(file.getPath(), namespace);
    }

    public BiNGOOntologyOboReader(String filename, String namespace) {
        this.filename = filename;
        this.namespace = namespace;
        this.synonymHash = new HashMap<>();
        //this.goMap = new HashMap();

        parse();

    }
    
    public BiNGOOntologyOboReader(String fileName, String namespace, Map<String, String> termToNamespace){
        this(fileName, namespace);
        this.termToNamespace = termToNamespace;
        parse();
    }
    // -------------------------------------------------------------------------

    private int stringToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private boolean parseHeader(String line) {

        if (line.startsWith("ontology: go")) {
            ontologyType = "GO";
        } else if (line.startsWith("ontology: hp")) {
            ontologyType = "HPO";
        }

        if (!ontologyType.equals("unknown")) {
            ontology = new Ontology(curator, ontologyType);
            fullOntology = new Ontology(curator, ontologyType);
        }

        curator = "unknown";
        return !line.trim().equals("[Term]");
    }

    //modified version of Bingo parse() method
    //
    private void parse() {
        //System.out.println("Curator:" + curator + "\nType of ontology: " + ontologyType);
        String name = "";
        String id = "";
        HashSet<String> geneNamespaces = new HashSet<>();
        HashSet<String> alt_id = new HashSet<>();
        HashSet<String> is_a = new HashSet<>();
        HashSet<String> part_of = new HashSet<>();
        boolean obsolete = false;
        boolean update = false;
        boolean isHeader = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (isHeader) {
                    isHeader = parseHeader(line);
                    continue;
                }

                if (line.trim().equals("[Typedef]")) {
                    return;
                }

                if (update) {
                    name = "";
                    id = "";
                    geneNamespaces.clear();
                    alt_id.clear();
                    is_a.clear();
                    part_of.clear();
                    update = false;
                    obsolete = false;
                }

                if (!line.trim().equals("[Term]") && !line.trim().equals("[Typedef]")) {

                    if (!line.trim().equals("")) {

                        String ref = line.substring(0, line.indexOf(":")).trim();

                        String value = line.substring(line.indexOf(":") + 1).trim();

                        switch (ref) {

                            case "name":
                                name = value.trim();
                                break;
                            case "namespace":
                                geneNamespaces.add(value.trim());
                                break;
                            case "subset":
                                geneNamespaces.add(value.trim());
                                break;
                            case "id":
                                id = value.trim().substring(3);
                                break;
                            case "alt_id":
                                alt_id.add(value.trim().substring(3));
                                break;
                            case "is_a":
                                is_a.add(value.split("!")[0].trim().substring(3));
                                break;
                            case "relationship":
                                if (value.startsWith("part_of")) {
                                    part_of.add(value.substring(7).split("!")[0].trim().substring(3));
                                }
                                break;
                            case "is_obsolete":
                                if (value.trim().equals("true")) {
                                    obsolete = true;
                                }

                                break;

                            default:
                                break;
                        }
                    } else {
                        update = true;

                        if (!obsolete) {

                            if (namespace.equals("---") || geneNamespaces.contains(namespace)) {
                                Integer id2 = new Integer(id);
                                synonymHash.put(id2, id2);
                                OntologyTerm term = new OntologyTerm(name, id2);
                                if (!ontology.containsTerm(id2)) {
                                    ontology.add(term);
                                    fullOntology.add(term);

                                    for (String s : alt_id) {
                                        synonymHash.put(new Integer(s), id2);
                                    }
                                    for (String s : is_a) {
                                        term.addParent(new Integer(s));
                                    }
                                    for (String s : part_of) {
                                        term.addContainer(new Integer(s));
                                    }
                                }
                            } else {
                                Integer id2 = new Integer(id);
                                OntologyTerm term = new OntologyTerm(name, id2);
                                if (!fullOntology.containsTerm(id2)) {
                                    fullOntology.add(term);
                                    for (String s : is_a) {
                                        term.addParent(new Integer(s));
                                    }
                                    for (String s : part_of) {
                                        term.addContainer(new Integer(s));
                                    }
                                }
                            }

                        }

                    }

                }

            }

            reRouteConnections();

        } catch (IOException ex) {
            System.out.println("Unable to read ontology file " + filename);
        }
    }
    //added method. The source of the method was part of parse method in BiNGO
    private void reRouteConnections() {
        // explicitely reroute all connections (parent-child relationships) that
        // are missing in subontologies like GOSlim
        // avoid transitive connections
        if (!namespace.equals("biological_process") && !namespace.equals("molecular_function")
                && !namespace.equals("cellular_component") && !namespace.equals("---")
                && !namespace.equals("phenotypic_abnormality") && !namespace.equals("clinical_modifier")
                && !namespace.equals("mode_of_inheritance") && !namespace.equals("clinical_course")) {
            //System.out.println("doSth()!");
            for (Integer j : ontology.getTerms().keySet()) {
                OntologyTerm o = ontology.getTerm(j);
                HashSet<OntologyTerm> ancestors = findNearestAncestors(new HashSet<>(), j);
                HashSet<OntologyTerm> prunedAncestors = new HashSet<>(ancestors);
                for (OntologyTerm o2 : ancestors) {
                    HashSet<OntologyTerm> o2Ancestors = getAllAncestors(new HashSet<>(), o2);
                    for (OntologyTerm o3 : o2Ancestors) {
                        if (ancestors.contains(o3)) {
                            prunedAncestors.remove(o3);
                        }
                    }
                }
                for (OntologyTerm o2 : prunedAncestors) {
                    o.addParent(o2.getId());
                }
            }
        }

    }

    private HashSet<OntologyTerm> findNearestAncestors(HashSet<OntologyTerm> ancestors, Integer k) {
        for (Integer i : fullOntology.getTerm(k).getParentsAndContainers()) {
            if (!ontology.containsTerm(i)) {
                findNearestAncestors(ancestors, i);
            } else {
                ancestors.add(ontology.getTerm(i));
            }
        }
        return ancestors;
    }

    private HashSet<OntologyTerm> getAllAncestors(HashSet<OntologyTerm> ancestors, OntologyTerm o) {
        for (Integer i : o.getParentsAndContainers()) {
            ancestors.add(fullOntology.getTerm(i));
            getAllAncestors(ancestors, fullOntology.getTerm(i));
        }
        return ancestors;
    }

    @Override
    public Ontology getOntology() {
        return ontology;
    }

    @Override
    public HashMap<Integer, Integer> getSynonymHash() {
        return synonymHash;
    }

    @Override
    public String getOntologyType() {
        return ontologyType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void makeOntologyFile(String outputDir) {
        File f = new File(outputDir, "GO_" + namespace);
        try {
            FileWriter fw = new FileWriter(f);
            PrintWriter pw = new PrintWriter(fw);

            pw.println("(curator=bingo)(type=namespace)");
            for (Object a : ontology.getTerms().keySet()) {
                OntologyTerm o = fullOntology.getTerm(new Integer(a.toString()));
                pw.print(o.getId() + " = " + o.getName());
                boolean ok = false;
                for (int i : o.getParentsAndContainers()) {
                    if (!ok) {
                        pw.print("[isa: ");
                        ok = true;
                    }
                    pw.print(i + " ");
                }
                if (ok) {
                    pw.println("]");
                } else {
                    pw.println();
                }
            }
            fw.close();
        } catch (IOException e) {
            //System.out.println("IOException: " + e);
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
} // class bingoOntologyFlatFileReader
