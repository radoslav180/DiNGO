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
 * * Description: Class that is the listener for the bingo-button on the settingspanel.
 * * It collects all kinds of information: the ontology and annotation
 * * file, the alpha, which distribution and correction will be used, ...
 * * It also redirects the vizualisation and the making of a file with
 * * information. It also redirects calculation of the p-values and
 * * corrected p-values.
 * * Modified by Radoslav Davidović July 2018
 **/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * *********************************************************************
 * BingoRunner.java --------------------------------
 * <p>
 * Steven Maere & Karel Heymans (c) March 2005
 * </p>
 * <p>
 * Class that is the listener for the bingo-button on the settingspanel. It
 * collects all kinds of information: the ontology and annotation file, the
 * alpha, which distribution and correction will be used, ... It also redirects
 * the vizualisation and the making of a file with information. It also
 * redirects calculation of the p-values and corrected p-values.
 * </p>
 * <p>
 * <p>
 * Modified by Radoslav Davidović July 19 2018</p>
 * <p>
 * Class is renamed to BingoRunner</p>
 * <ul>Changes:
 * <li>new constructors {@link #BingoRunner(AnnotationParser, StatisticsDescriptor, String, String, String, int)} and
 * {@link #BingoRunner()}  }</li>
 * <li>class implements {@link Runnable} interface</li>
 * <li>method {@link #actionPerformed() } instead of
 * <code>void actionPerformed(ActionEvent)</code></li>
 * <li>method <code>String openResourceFile(String)</code> removed</li>
 * <li>method <code>boolean updateParameters()</code> removed</li>
 * <li>method
 * <code>Set &lt;String&gt conformize(Set &lt;String&gt, Set &lt;String&gt)</code>
 * removed</li>
 * <li>method <code>HashSet getBatchClusterFromTextArea(String)</code>
 * removed</li>
 * <li>all final (instance and static) variables were removed</li>
 * <li>instance variable SettingsPanel settingsPanel removed </li>
 * <li>instance variable of type bingo.internal.GOlorize.GoBin removed</li>
 * <li>instance variable of type CyNetworkView removed</li>
 * <li>instance variable of type CyNetwork removed</li>
 * <li>instance variable of type TaskMonitor removed</li>
 * </ul>
 * </p> *********************************************************************
 */
public class BingoRunner implements Runnable {

    private AnnotationParser parser;
    private StatisticsDescriptor descriptor;
    private String ontologyType;
    private Set<String> ecCodes;
    private Map<String, Set<String>> redundantIDs = new HashMap<>();

    private String selectedNodes;
    private String clusterName;
    private String outputDir;
    private int numberOfClusters;
    //follows number of threads
    private static AtomicInteger counter = new AtomicInteger(1);

    /**
     * Constructor with all the settings of the settings panel as arguments.
     *
     */
    public BingoRunner() {

        ecCodes = new HashSet<>();
        ecCodes.add("IEA");
        ecCodes.add("ISS");
        ecCodes.add("TAS");
        ecCodes.add("IDA");
        ecCodes.add("IGI");
        ecCodes.add("IMP");
        ecCodes.add("IEP");
        ecCodes.add("ND");
        ecCodes.add("RCA");
        ecCodes.add("IPI");
        ecCodes.add("NAS");
        ecCodes.add("IC");
        ecCodes.add("NR");
    }
    
    /**
    *<p>Constructor</p>
    *
    *@param parser contains info collected from annotation and ontology files
    *@param descriptor contains description of statistical test
    *@params selectedNodes user input
    *@params clusterName name of protein/gene cluster that is used for result file naming
    *@params outputDir path to folder where result file will be saved
    *@params numberOfClusters number of clusters in input file
    */
    public BingoRunner(AnnotationParser parser, StatisticsDescriptor descriptor, String selectedNodes,
                       String clusterName, String outputDir, int numberOfClusters){
        this();
        this.parser = parser;
        this.descriptor = descriptor;
        this.selectedNodes = selectedNodes;
        this.clusterName = clusterName;
        this.outputDir = outputDir;
        this.numberOfClusters = numberOfClusters;

    }

    /**
     * actual work is going here
     */
    public void actionPerformed() {

        final Set<String> noClassificationsSet = new HashSet<>();
        redundantIDs = new HashMap<>();

        HashSet<String> sNodes;
        Set<String> aNodes;

        if (descriptor.getReferenceSet().equals(Constants.GENOME.getConstant())) {
            sNodes = getSelectedCanonicalNamesFromTextArea();
            aNodes = getAllCanonicalNamesFromAnnotation(sNodes);

        } else {
            sNodes = getSelectedCanonicalNamesFromTextArea();
            aNodes = getAllCanonicalNamesFromReferenceSet(descriptor.getReferenceSet(), sNodes);
        }

        int[] testData = getClassificationsFromVector(sNodes, noClassificationsSet);
        boolean noElementsInTestData = false;
        // testing whether there are elements in sample data
        // array.
        try {
            int firstElement = testData[0];
        } catch (Exception ex) {
            noElementsInTestData = true;
        }
        if (!noElementsInTestData) {

            performCalculations(sNodes, aNodes, noClassificationsSet);

            noClassificationsSet.clear();

        } else {
            System.out.println("The selected annotation does not produce any"
                    + "\n" + "classifications for the selected nodes." + "\n"
                    + "Maybe you chose the wrong type of gene identifier ?"
                    + "You can use DiNGO mapping tool!");
        }

    }

    /**
     * method that gets the canonical names from text input.
     *
     * @return HashSet containing the canonical names.
     */
    public HashSet<String> getSelectedCanonicalNamesFromTextArea() {

        //String textNodes = params.getTextInput();
        String[] nodes = selectedNodes.split("\\s+");
        // HashSet for storing the canonical names
        HashSet<String> canonicalNameVector = new HashSet<>();
        Set<Set<String>> mapNames = new HashSet<>();
        // iterate over every node view to get the canonical names.
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null && nodes[i].length() != 0 && !canonicalNameVector.contains(nodes[i].toUpperCase())) {
                if (mapNames.contains(parser.getAlias().get(nodes[i].toUpperCase()))) {
                    redundantIDs.put(nodes[i].toUpperCase(),
                            parser.getAlias().get(nodes[i].toUpperCase()));

                }
                // else{
                if (parser.getAlias().get(nodes[i]) != null) {
                    mapNames.add(parser.getAlias().get(nodes[i]));
                }
                canonicalNameVector.add(nodes[i]);
                // }
            }
        }
        return canonicalNameVector;
    }

    /**
     * method that gets the canonical names for the whole annotation.
     *
     * @param selectedNodes genes/proteins selected by user
     * @return HashSet containing the canonical names.
     */
    public Set<String> getAllCanonicalNamesFromAnnotation(Set<String> selectedNodes) {
        String[] nodes = parser.getAnnotation().getNames();
        // HashSet for storing the canonical names
        HashSet<String> canonicalNameVector = new HashSet<>();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null && (nodes[i].length() != 0)) {
                canonicalNameVector.add(nodes[i].toUpperCase());
            }
        }

        // replace canonical names in reference set that match one of the
        // canonical names in the selected cluster, to get rid of e.g. splice
        // variants if the non-splice-specific gene is part of the selection,
        // and to avoid conflicts between names in ref set and selection
        Map<String, HashSet<String>> alias = parser.getAlias();
        Iterator it2 = selectedNodes.iterator();
        while (it2.hasNext()) {
            String name = it2.next() + "";
            Set tmp = alias.get(name);
            if (tmp != null) {
                Iterator it = tmp.iterator();
                while (it.hasNext()) {
                    canonicalNameVector.remove(it.next() + "");
                }
                // add selected node name
                canonicalNameVector.add(name);
            }
        }
        return canonicalNameVector;
    }

    /**
     * method that gets the canonical names for the whole annotation.
     *
     * @param refSet reference set
     * @param selectedNodes user input
     * @return HashSet containing the canonical names.
     */
    public Set<String> getAllCanonicalNamesFromReferenceSet(String refSet, Set<String> selectedNodes) {
        HashSet<String> nodes = parseReferenceSet(refSet);
        // HashSet for storing the canonical names
        HashSet<String> canonicalNameVector = new HashSet<>();
        for (String s : nodes) {
            if (s.length() != 0) {
                canonicalNameVector.add(s.toUpperCase());
            }
        }

        // replace canonical names in reference set that match one of the
        // canonical names in the selected cluster, to get rid of e.g. splice
        // variants if the non-splice-specific gene is part of the selection,
        // and to avoid conflicts between names in ref set and selection
        Map<String, HashSet<String>> alias = parser.getAlias();
        Iterator it2 = selectedNodes.iterator();
        while (it2.hasNext()) {
            String name = it2.next() + "";
            Set<String> tmp = alias.get(name);
            if (tmp != null) {
                Iterator it = tmp.iterator();
                while (it.hasNext()) {
                    canonicalNameVector.remove(it.next() + "");
                }
                // add selected node name
                canonicalNameVector.add(name);
            }
        }
        return canonicalNameVector;
    }

    public HashSet<String> parseReferenceSet(String refSetFile) {

        HashSet<String> refSet = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(refSetFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                refSet.add(tokens[0].trim().toUpperCase());
            }
        } catch (IOException e) {
            System.out.println("Error reading reference file: " + e);
        }
        return refSet;
    }

    /**
     * Method that gets the classifications from a HashSet of canonical names.
     *
     * @param canonicalNameVector HashSet of canonical names.
     * @param noClassificationsSet genes/proteins without classifications
     * @return int[] classifications.
     */
    public int[] getClassificationsFromVector(Set<String> canonicalNameVector, Set<String> noClassificationsSet) {
        // HashSet for the classifications.
        Set<String> classificationsVector = new HashSet<>();
        Map<String, HashSet<String>> alias = parser.getAlias();
        // array for go labels.
        int[] goLabelsName;
        Iterator it2 = canonicalNameVector.iterator();
        while (it2.hasNext()) {
            String name = it2.next() + "";
            Set<String> identifiers = alias.get(name);
            Set<String> cls = new HashSet<>();
            // array for go labels.
            if (identifiers != null) {
                Iterator it = identifiers.iterator();
                while (it.hasNext()) {
                    goLabelsName = parser.getAnnotation().getClassifications(it.next() + "");
                    for (int t = 0; t < goLabelsName.length; t++) {
                        cls.add(goLabelsName[t] + "");
                    }
                }
            }
            if (cls.isEmpty()) {
                noClassificationsSet.add(name);
            }
            Iterator it3 = cls.iterator();
            while (it3.hasNext()) {
                classificationsVector.add(it3.next() + "");
            }
        }
        int[] classifications = new int[classificationsVector.size()];
        it2 = classificationsVector.iterator();
        int i = 0;
        while (it2.hasNext()) {
            classifications[i] = Integer.parseInt(it2.next() + "");
            i++;
        }
        return classifications;
    }

    /**
     * Method that redirects the calculations of the distribution and the
     * correction. Redirects the visualization of the network and redirects the
     * making of a file with the interesting data.
     *
     * @param selectedNodes user input
     * @param allNodes background
     * @param noClassificationsSet genes / proteins without annotations
     */
    public void performCalculations(Set<String> selectedNodes, Set<String> allNodes, Set<String> noClassificationsSet) {
        Map testMap = null;
        Map correctionMap = null;
        BingoAlgorithm algorithm = new BingoAlgorithm(parser.getAnnotation(), parser.getOntology(), descriptor,
                parser.getAlias(), selectedNodes, allNodes);
        //BingoAlgorithm algorithm = new BingoAlgorithm(params, selectedNodes, allNodes);
        CalculateTestTask test = algorithm.calculate_distribution();

        try {
            test.calculate();
            testMap = test.getTestMap();
        } catch (Exception e) {

            e.printStackTrace();
        }
        CalculateCorrectionTask correction = null;
        if (!descriptor.getCorrectionTest().equals(Constants.NONE.getConstant())) {
            //System.out.println(params.getCategory());
            correction = algorithm.calculate_corrections(testMap);

            try {
                correction.calculate();
            } catch (Exception e) {
                System.out.println("NULL");
                e.printStackTrace();
            }

        }

        if ((correction != null) && (!descriptor.getTest().equals(Constants.NONE.getConstant()))) {
            correctionMap = correction.getCorrectionMap();

        }

        CreateBiNGOFile file;
        file = new CreateBiNGOFile(test, correction, parser, descriptor, selectedNodes, noClassificationsSet, outputDir,
                clusterName, Constants.CATEGORY_CORRECTION.getConstant());
        file.writeToFile();
        //monitor progress with "progress bar"
        int count = counter.getAndIncrement();
        double percent = (100.0 * count) / numberOfClusters;
        System.out.print(count + " / " + numberOfClusters + " " + (int) percent + "% completed\r");
    }

    @Override
    public void run() {
        actionPerformed();
    }

}
