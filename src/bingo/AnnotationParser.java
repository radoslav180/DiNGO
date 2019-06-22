package bingo;

/* * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * *
 * * Authors : Steven Maere, Karel Heymans
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
 * * Date: Apr.11.2005
 * * Description: Class that parses the annotation files in function of the chosen ontology.
 * * Modified by Radoslav Davidović July 12 2018.
 **/
import java.io.IOException;
import java.util.*;

import ontology.Annotation;
import ontology.Ontology;
import ontology.OntologyTerm;

/**
 * <p>
 * ************************************************************
 * AnnotationParser.java --------------------------
 *
 * Steven Maere (c) April 2005
 *
 * Class that parses the annotation files in function of the chosen ontology.
 * Modified by Radoslav Davidović July 2018
 * <ol>Changes:
 * <li>new constructor {@link #AnnotationParser(java.util.Set, java.util.Set, java.lang.String, java.lang.String,
 * java.lang.String, java.lang.String)}</li>
 * <li>method {@link #calculate()} was modified</li>
 * <li>methods <code>String setCustomOntology()</code> and
 * <code>String setFullOntology</code> were replaced by
 * {@link #setOntology()}</li>
 * <li>method <code>setCustomAnnotation()</code> renamed to
 * {@link #setAnnotation()}</li>
 * <li>methods <code>Annotation remap(Annotation annotation, Ontology ontology, Set
 *  * &lt String&gt genes)</code> and <code>Annotation customRemap(Annotation annotation,
 *  * Ontology ontology, Set &lt String&gt genes)</code> replaced by
 * {@link #remap()}</li>
 * <li>Following methods were removed:<ul>
 * <li><code>String openResourceFile(String)</code></li>
 * <li><code>String setDefaultAnnotation()</code></li>
 * <li><code>String setDefaultOntology(Map)</code></li>
 * </ul></li>
 * </ol>
 * ************************************************************* </p>
 */
public class AnnotationParser {

    /**
     * constant string for the loadcorrect of the filechooser.
     */
    private static final String LOADCORRECT = "LOADCORRECT";
    /**
     * <p>
     * is ontology GO or HPO</p>
     */
    private String ontologyType = "GO";
    private final String ontologyFile;
    private final String annotationFile;
    private final String mappingFile;
    private final String namespace;
    /**
     * annotation and ontology
     */
    private Annotation annotation;
    private Annotation parsedAnnotation;
    private Ontology ontology;
    private Map<String, HashSet<String>> alias;

    /**
     * full ontology which is used for remapping the annotations to one of the
     * default ontologies (not for custom ontologies)
     */
    private Ontology fullOntology;
    private Map<Integer, Integer> synonymHash;

    //private BingoParameters params;
    private final Set<String> genes;
    private final Set<String> deleteCodes;
    /**
     * boolean loading correctly ?
     */
    private boolean status = true;
    /**
     * true if found annotation categories which are not in ontology
     */
    private boolean orphansFound = false;
    /**
     * false if none of the categories in the annotation match the ontology
     */
    private boolean consistency = false;

    private Set<Integer> parentsSet;
   
    
    public AnnotationParser(Set<String> genes, Set<String> deleteCodes,
            String ontologyFile, String annotationFile,
            String namespace, String mappingFile) {
        this.genes = genes;
        this.deleteCodes = deleteCodes;
        this.ontologyFile = ontologyFile;
        this.annotationFile = annotationFile;
        this.namespace = namespace;
        this.mappingFile = mappingFile;
    }

    /**
     * method that governs loading and re-mapping of annotation files
     *
     * @throws IOException throws IOException
     *
     */
    public void calculate() throws IOException {

        // always perform full remap for .obo files, allows definition of
        // custom GOSlims
        String loadOntologyString = setOntology();
        if (ontologyFile.endsWith(".obo")) {//params.getOntologyFile()

            if (!loadOntologyString.equals(LOADCORRECT)) {
                status = false;
                System.out.println("Your full ontology file contains errors "
                        + loadOntologyString);
            }
            if (status) {
                // check for cycles
                checkOntology(fullOntology);
            }
        }

        if (status) {
            // loaded a correct ontology file?
            if (!loadOntologyString.equals(LOADCORRECT)) {
                status = false;

                System.out.println("Your ontology file contains errors "
                        + loadOntologyString);
            }
            if (status) {
                // check for cycles
                checkOntology(ontology);
                if (status) {
                    String loadAnnotationString;

                    loadAnnotationString = setAnnotation();

                    // loaded a correct annotation file?
                    if (!loadAnnotationString.equals(LOADCORRECT)) {
                        status = false;
                        System.out.println("Your annotation file contains errors "
                                + loadAnnotationString);
                    }
                    // annotation consistent with ontology ?
                    if ((status) && (!consistency)) {
                        status = false;
                        throw new IOException(
                                "None of the labels in your annotation match "
                                + "with the chosen ontology, please c"
                                + "heck their compatibility.");
                    }
                    if (status) {
                        remap();
                    }
                }
            }
        }
    }

    /**
     * Method that parses the custom annotation file into an annotation-object
     * and returns a string containing whether the operation is correct or not.
     *
     * @return string string with either loadcorrect or a parsing error.
     */
    private String setAnnotation() {

        String fileString = annotationFile;//params.getAnnotationFile()
        annotation = null;
        String resultString;
        IAnnotation readerAnnotation;

        // if fileString contains "gene_association" then assume you're using GO
        // Consortium annotation files
        try {

            if (fileString.contains("gene_association")
                    || fileString.contains("phenotype_annotation")
                    || fileString.endsWith(".gaf")) {
                //System.out.println("reading file...");

                readerAnnotation = new BiNGOConsortiumAnnotationReader(fileString, mappingFile,
                        synonymHash, deleteCodes, "Consortium", "GO");//params.getDeleteCodes()
            } else {
                readerAnnotation = new BiNGOAnnotationFlatFileReader(fileString, mappingFile, synonymHash);
            }
            annotation = readerAnnotation.getAnnotation();
            if (readerAnnotation.getOrphans()) {
                orphansFound = true;
            }
            if (readerAnnotation.getConsistency()) {
                consistency = true;
            }
            alias = readerAnnotation.getAlias();
            //System.out.println("alias = " + alias.size());
            resultString = LOADCORRECT;
        } catch (IllegalArgumentException e) {
            resultString = "ANNOTATION FILE PARSING ERROR, PLEASE CHECK FILE FORMAT:  \n" + e;
        } catch (Exception e) {
            resultString = "" + e;
        }

        return resultString;
    }

    /**
     * <p>
     * This method unifies two previous methods in BiNGO setCustomOntology() and
     * setFullOntology(). Method that parses the ontology file into an
     * ontology-object and returns a string containing whether the operation is
     * correct or not.</p>
     *
     * @return string string with either {@link #LOADCORRECT} or a parsing
     * error.
     */
    private String setOntology() {
        String fileString = ontologyFile;//params.getOntologyFile()
        ontology = null;
        fullOntology = null;
        String resultString;
        IOntologyReader readerOntology;
        try {
            this.synonymHash = null;
            if (fileString.endsWith(".obo")) {
                readerOntology = new BiNGOOntologyOboReader(fileString, namespace);
                if (!namespace.equals(Constants.NONE.getConstant())) {
                    BiNGOOntologyOboReader full = new BiNGOOntologyOboReader(fileString, Constants.NONE.getConstant());
                    fullOntology = full.getOntology();
                    this.synonymHash = full.getSynonymHash();
                } else {
                    fullOntology = readerOntology.getOntology();
                    this.synonymHash = readerOntology.getSynonymHash();
                }

                ontology = readerOntology.getOntology();

                ontologyType = readerOntology.getOntologyType();
            } else {
                readerOntology = new BiNGOOntologyFlatFileReader(fileString);
                ontology = readerOntology.getOntology();
                this.synonymHash = readerOntology.getSynonymHash();
            }

            if (ontology.size() == 0 || (fullOntology != null && fullOntology.size() == 0)) {
                throw (new IllegalArgumentException());
            } else {
                //this.synonymHash = readerOntology.getSynonymHash();
                //System.out.println("synonymHash = " + synonymHash.size());
                resultString = LOADCORRECT;
            }
        } catch (IllegalArgumentException e) {
            resultString = "ONTOLOGY FILE PARSING ERROR, PLEASE CHECK FILE FORMAT AND VALIDITY OF NAMESPACE:  \n"
                    + e;
        } catch (IOException e) {
            resultString = "Ontology file could not be located...";
        } catch (Exception e) {
            resultString = "" + e;
        }

        return resultString;
    }

    private void checkOntology(Ontology ontology) throws IOException {
        HashMap<Integer, OntologyTerm> ontMap = ontology.getTerms();
        Iterator<Integer> it = ontMap.keySet().iterator();
        while (it.hasNext()) {
            parentsSet = new HashSet<>();
            int childNode = Integer.parseInt(it.next().toString());
            up_go(childNode, childNode, ontology);
        }
    }

    /**
     * <p>
     * Method unifies two previous BiNGO methods Annotation remap(Annotation
     * annotation, Ontology ontology, <code></code>Set<String> genes) throws
     * InterruptedException</code> and <code></code>Annotation
     * customRemap(Annotation annotation, Ontology ontology, Set<String> genes)
     * throws InterruptedException</code>
     * </p>
     */
    private void remap() {
        parsedAnnotation = new Annotation(annotation.getSpecies(), annotation.getType(),
                annotation.getCurator());
        HashMap<String, ArrayList<Integer>> annMap = annotation.getMap();
        Iterator it = annMap.keySet().iterator();

        HashSet<String> ids = new HashSet<>();
        for (String gene : genes) {
            if (alias.get(gene) != null) {
                ids.addAll(alias.get(gene));
            }
        }
        while (it.hasNext()) {

            parentsSet = new HashSet<>();
            String node = it.next() + "";
            if (genes.isEmpty() || ids.contains(node)) {
                // array with go labels for gene it.next().
                int[] goID;
                goID = annotation.getClassifications(node);
                for (int t = 0; t < goID.length; t++) {
                    if (ontology.getTerm(goID[t]) != null) {
                        parsedAnnotation.add(node, goID[t]);
                    }
                    // all parent classes of GO class that node is assigned
                    // to are also explicitly included in classifications
                    // CHECK IF goID EXISTS IN fullOntology...
                    if (ontologyFile.endsWith(".obo")) {//params.getOntologyFile()
                        if (fullOntology.getTerm(goID[t]) != null) {
                            up(node, goID[t], parsedAnnotation, ontology, fullOntology);
                        } else {
                            System.out.println("Orphan found " + goID[t]);
                            orphansFound = true;
                        }
                    } else {
                        up(node, goID[t], parsedAnnotation, ontology, ontology);
                    }
                }
            }

        }

    }

    /**
     * method for recursing through tree to root
     */
    private void up(String node, int id, Annotation parsedAnnotation,
            Ontology ontology, Ontology flOntology) {
        OntologyTerm child = flOntology.getTerm(id);
        int[] parents = child.getParentsAndContainers();
        for (int t = 0; t < parents.length; t++) {
            if (!parentsSet.contains(parents[t])) {
                parentsSet.add(parents[t]);
                if (ontology.getTerm(parents[t]) != null) {
                    parsedAnnotation.add(node, parents[t]);
                }
                up(node, parents[t], parsedAnnotation, ontology, flOntology);
                // else{System.out.println("term not in ontology: "+
                // parents[t]);}
            }
        }
    }

    /**
     * method for recursing through tree to root and detecting cycles
     *
     * @throws IOException
     */
    private void up_go(int startID, int id, Ontology ontology) throws IOException {
        OntologyTerm child = ontology.getTerm(id);
        int[] parents = child.getParentsAndContainers();
        for (int t = 0; t < parents.length; t++) {
            if (parents[t] == startID) {
                status = false;
                throw new IOException("Your ontology file contains a cycle at ID " + startID);
            } else if (!parentsSet.contains(parents[t])) {
                if (ontology.getTerm(parents[t]) != null) {
                    parentsSet.add(parents[t]);
                    up_go(startID, parents[t], ontology);
                } else {
                    System.out.println("term not in ontology: " + parents[t]);
                }
            }
        }
    }

    /**
     * @return the parsed annotation
     */
    public Annotation getAnnotation() {
        return parsedAnnotation;
    }

    /**
     * @return the ontology
     */
    public Ontology getOntology() {
        return ontology;
    }

    public Map<String, HashSet<String>> getAlias() {
        return alias;
    }

    public String getOntologyType() {
        return ontologyType;
    }

    /**
     * @return true if there are categories in the annotation which are not
     * found in the ontology
     */
    public boolean getOrphans() {
        return orphansFound;
    }

    /**
     * @return the parser status : true if OK, false if something's wrong
     */
    public boolean getStatus() {
        return status;
    }

    public String getOntologyFile() {
        return ontologyFile;
    }

    public String getAnnotationFile() {
        return annotationFile;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public String getNamespace() {
        return namespace;
    }

    public Set<String> getDeleteCodes() {
        return deleteCodes;
    }

}
