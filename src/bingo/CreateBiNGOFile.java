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
 * * Authors: Steven Maere, Karel Heymans
 * * Date: Mar.25.2005
 * * Description: Class which creates a file with information about the selected
 * * cluster: ontology type and curator, time of creation, alpha,
 * * sort of test and correction, p-values and corrected
 * * p-values, term id and name, x, X, n, N.
 **/

import ontology.Annotation;
import ontology.Ontology;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;

/**
 *
 * <p>
 * CreatebingoFile.java<br> Steven Maere & Karel Heymans (c) March 2005<br>
 * --------------------<br>
 * Modified by Radoslav Davidović January 2019<br>
 * Class which creates a file with information about the selected cluster:
 * ontology type and curator, time of creation, alpha, sort of test and
 * correction, p-values and corrected p-values, term id and name, x, X, n, N.
 * </p> 
 * <p>
 * <ol>Changes:
 *  <li>new constructor {@link #CreateBiNGOFile(CalculateTestTask, CalculateCorrectionTask, AnnotationParser,
 *  StatisticsDescriptor, Set, Set, String, String, String)}  }. Previous
 * constructor was deleted.
 * <li>new method that writes results to file {@link #writeToFile()} replaces old 
 *  method <code>void makeFile()</code>.
 * </li>
 * <li>added two private methods {@link #writeHeaderToFile(java.io.PrintWriter)}
 *  and {@link #loadAnnotatedGenes()}. The methods were part of makeFile() method
 * </li>
 * <li>deleted <code>String[] ordenKeysByPvalues(String[] labels)</code> and 
 * <code>String[] ordenKeysBySmallX(String[] labels)</code>
 * </li>
 * </li>
 * </ol>
 * </p>
 */
public final class CreateBiNGOFile {

    /**
     * hashmap with key termID and value pvalue.
     */
    private Map<Integer, String> testMap;
    /**
     * hashmap with key termID and value corrected pvalue.
     */
    private Map<String, String> correctionMap;
    /**
     * hashmap with key termID and value x.
     */
    private Map<Integer, Integer> mapSmallX;
    /**
     * hashmap with key termID and value n.
     */
    private Map<Integer, Integer> mapSmallN;
    /**
     * integer with X.
     */
    private Map<Integer, Integer> mapBigX;
    /**
     * integer with N.
     */
    private Map<Integer, Integer> mapBigN;
    /**
     * String with alpha value.
     */
    private String alphaString;
    /**
     * String with used test.
     */
    private String testString;
    /**
     * String with used correction.
     */
    private String correctionString;
    /**
     * String for over- or underrepresentation.
     */
    private String overUnderString;
    //added by me
    private String ontologyType;
    //added by me
    private String namespace;
    
    /**
     * the annotation (remapped, i.e. including all parent annotations)
     */
    private Annotation annotation;

    private Set<String> deleteCodes;
    /**
     * the ontology.
     */
    private Ontology ontology;
    /**
     * the annotation file path.
     */
    private String annotationFile;
    /**
     * the ontology file path.
     */
    private String ontologyFile;
    /**
     * the dir for saving the data file.
     */
    private String dirName;
    /**
     * the file name for the data file.
     */
    private String fileName;
    /**
     * the clusterVsString.
     */
    private String clusterVsString;
    /**
     * the categoriesString.
     */
    private String catString;
    /**
     * HashSet with the names of the selected nodes.
     */
    private Set<String> selectedCanonicalNameVector;

    /**
     * hashmap with keys the GO categories and values HashSets of test set genes
     * annotated to that category
     */
    private Set<String> noClassificationsSet;

    private Map<String, HashSet<String>> annotatedGenes;

    private Map<String, HashSet<String>> alias;


    public CreateBiNGOFile(CalculateTestTask test, CalculateCorrectionTask correctionTest, AnnotationParser parser,
                           StatisticsDescriptor descriptor, Set<String> selectedNodes, Set<String> noClassificationsSet,
                           String outputDir, String clusterName, String category) {

        this.testMap = test.getTestMap();
        if (correctionTest != null)
            this.correctionMap = correctionTest.getCorrectionMap();
        this.mapSmallX = test.getMapSmallX();
        this.mapSmallN = test.getMapSmallN();
        this.mapBigX = test.getMapBigX();
        this.mapBigN = test.getMapBigN();
        this.alphaString = descriptor.getpValue();
        this.annotation = parser.getAnnotation();
        this.ontology = parser.getOntology();
        this.annotationFile = parser.getAnnotationFile();
        this.ontologyFile = parser.getOntologyFile();
        this.testString = descriptor.getTest();
        this.correctionString = descriptor.getCorrectionTest();
        this.overUnderString = descriptor.getRepresentation();
        this.dirName = outputDir;
        this.fileName = clusterName + ".bgo";
        this.clusterVsString = descriptor.getReferenceSet();
        this.catString = category;
        this.selectedCanonicalNameVector = selectedNodes;
        this.noClassificationsSet = noClassificationsSet;
        this.annotatedGenes = new HashMap<>();
        this.alias = parser.getAlias();
        this.deleteCodes = parser.getDeleteCodes();
        this.ontologyType = parser.getOntologyType();
        this.namespace = parser.getNamespace();
    }


    //associates ontology term ID with genes / proteins
    //this is part of BiNGO makeFile() method
    private void loadAnnotatedGenes() {
        Iterator<String> it = selectedCanonicalNameVector.iterator();
        while (it.hasNext()) {
            String name = it.next() + "";
            HashSet tmp = alias.get(name);
            if (tmp != null) {
                Iterator it2 = tmp.iterator();
                while (it2.hasNext()) {
                    int[] nodeClassifications = annotation.getClassifications(it2.next() + "");
                    for (int k = 0; k < nodeClassifications.length; k++) {
                        String cat = Integer.toString(nodeClassifications[k]);
                        if (!annotatedGenes.containsKey(cat)) {
                            HashSet<String> catset = new HashSet<>();
                            annotatedGenes.put(cat, catset);
                        }
                        annotatedGenes.get(cat).add(name);
                    }
                }
            }
            //output.write(name + "\t");

        }

    }

    //writes info about enrichment analysis
    //this is part of BiNGO makeFile() method
    //added info about namespace
    private void writeHeaderToFile(PrintWriter output) {
        String dateString = DateFormat.getDateInstance().format(new Date());
        String timeString = DateFormat.getTimeInstance().format(new Date());
        output.println("File created with dingo (c) on " + dateString + " at " + timeString);
        output.println();
        output.println(ontology.toString());
        output.println();
        output.println("Selected ontology file : " + ontologyFile);
        output.println("Selected annotation file : " + annotationFile);
        output.println("Selected namespace: " + namespace);
        output.println("Discarded evidence codes : ");
        Iterator it = deleteCodes.iterator();
        while (it.hasNext()) {
            output.write(it.next().toString() + "\t");
        }
        output.println("Selected statistical test : " + testString);
        output.println("Selected correction : " + correctionString);
        output.println("Selected significance level : " + alphaString);
        output.println("Testing option : " + clusterVsString);
        output.println("Representation: " + overUnderString);

        output.println("The following " + noClassificationsSet.size() + " entities have no annotations:");
        output.println(noClassificationsSet.toString().replace("[", "")
                .replace("]", "").replace(", ", "|"));

        output.println("\n\n\n\n" + ontologyType + "-ID" + "\t" + "p-value" + "\t" + "corr p-value" + "\t" + "x" + "\t" + "n" + "\t" + "X"
                + "\t" + "N" + "\t" + "Description" + "\t" + "Genes in test set");
    }

    /**
     * <p>Method that writes to file results of the enrichment analysis. This method replaced BiNGO method
     *  <code>makeFile()</code>
     * </p>
     */
    public void writeToFile() {
        loadAnnotatedGenes();
        File results = new File(dirName, fileName);
        try (PrintWriter writer = new PrintWriter(results)) {
            writeHeaderToFile(writer);
            List<Pair<Integer>> pairs = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : testMap.entrySet()) {
                pairs.add(new Pair<>(entry.getKey(), entry.getValue()));
            }

            pairs.sort(new Comparator<Pair<Integer>>() {
                @Override
                public int compare(Pair<Integer> o1, Pair<Integer> o2) {
                    return new BigDecimal(o1.getValue()).compareTo(new BigDecimal(o2.getValue()));
                }

            });

            Iterator<Pair<Integer>> it = pairs.iterator();

            while (it.hasNext()) {
                StringBuilder line = new StringBuilder();
                Pair<Integer> pair = it.next();
                Integer key = pair.getKey();
                String value = pair.getValue();
                String correctedPValue;

                if (catString.equals(Constants.CATEGORY_BEFORE_CORRECTION.getConstant())) {
                    if ((new BigDecimal(testMap.get(key)))
                            .compareTo(new BigDecimal(alphaString)) < 0) {
                        //if there is no correction corrected p value is -
                        correctedPValue = correctionMap == null ? "-" : String.format("%6.3e",
                                Double.parseDouble(correctionMap.get(Integer.toString(key))));

                        line.append(key).append("\t").append(String.format("%6.3e", Double.parseDouble(value)))
                                .append("\t").append(correctedPValue)
                                .append("\t").append(mapSmallX.get(key)).append("\t").append(mapSmallN.get(key))
                                .append("\t").append(mapBigX.get(key)).append("\t").append(mapBigN.get(key))
                                .append("\t").append(ontology.getTerm(key).getName())
                                .append("\t").append(annotatedGenes.get(Integer.toString(key)).toString().
                                replace("[", "").replace("]", "")
                                .replace(", ", "|"));
                    }

                } else {
                    //if there is no correction ranks according to p value, otherwise according to corrected p value
                    correctedPValue = correctionMap == null ? "-" : String.format("%6.3e",
                            Double.parseDouble(correctionMap.get(Integer.toString(key))));
                    String comp = correctionMap == null ? testMap.get(key) : correctionMap.get(Integer.toString(key));
                    if ((new BigDecimal(comp))
                            .compareTo(new BigDecimal(alphaString)) < 0) {

                        line.append(key).append("\t").append(String.format("%6.3e", Double.parseDouble(value)))
                                .append("\t").append(correctedPValue)
                                .append("\t").append(mapSmallX.get(key)).append("\t").append(mapSmallN.get(key))
                                .append("\t").append(mapBigX.get(key)).append("\t").append(mapBigN.get(key))
                                .append("\t").append(ontology.getTerm(key).getName())
                                .append("\t").append(annotatedGenes.get(Integer.toString(key)).toString()
                                .replace("[", "").replace("]", "").
                                        replace(", ", "|"));

                    }

                }
                //don't write empty lines
                if (line.toString().length() != 0)
                    writer.println(line.toString());

            }
            //System.out.println("dingo results file: " + results.getPath());

        } catch (IOException ex) {
            System.out.println("Unable to write to file!");
        }
    }

}
