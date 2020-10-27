/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dingo;

import bingo.*;
import configurator.Configurator;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import update.DiNGOFilesUpdater;

/**
 *
 * @author Radoslav Davidović
 */
public final class Dingo {

    /**
     * <p>
     * method that extracts data sets from user's file</p>
     *
     * @param text file containing list of genes/proteins
     * @return array containing individual data sets
     */
    private static String[] getDataSets(String text) {
        //System.out.println("text:\n " + text);
        return text.split("batch");
    }
    //display parameters of enrichment analysis
    private static void displayDiNGOParameters(BingoParameters params) {
        System.out.printf("%-30s%s%n", "Ontology:", params.getOntologyName());
        System.out.printf("%-30s%s%n", "Statistical test: ", params.getTest());
        System.out.printf("%-30s%s%n", "Correction: ",
                params.getCorrectionTest());
        System.out.printf("%-30s%s%n", "Significance: ",
                params.getSignificance());
        System.out.printf("%-30s%s%n", "Namespace: ", params.getNameSpace());
        System.out.printf("%-30s%s%n", "Representation: ",
                params.getOverOrUnder());
        System.out.printf("%-30s%s%n", "Reference set: ",
                params.getReferenceSet());
        System.out.printf("%-30s%s%n", "Species: ", params.getSpecies());
        System.out.printf("%-30s%s%n%n", "Discarded evidence code: ",
                params.getDeleteCodes());

    }

    /**
     * <p>
     * method returns object instance of {@link AnnotationParser}</p>
     *
     * @param params instance of {@link BingoParameters} containing command line
     * arguments
     * @return instance of {@link AnnotationParser}
     */
    private AnnotationParser getAnnotationParser(BingoParameters params) {
        //the followed code is part of Bingo's SettingsActionListener class
        Set<String> genes = new HashSet<>();
        AnnotationParser annParser = new AnnotationParser(genes, params.getDeleteCodes(),
                params.getOntologyFile(), params.getAnnotationFile(),
                params.getNameSpace(), params.getMappingFile());//params.initializeAnnotationParser();

       
        if (annParser.getStatus()) {
            try {
                annParser.calculate();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return null;
            }

        }
        
        return annParser;
    }

    private StatisticsDescriptor getStatisticsDescriptor(BingoParameters params){
            StatisticsDescriptor descriptor = new StatisticsDescriptor();
            descriptor.setTest(params.getTest());
            descriptor.setCorrectionTest(params.getCorrectionTest());
            descriptor.setRepresentation(params.getOverOrUnder());
            descriptor.setpValue(params.getSignificance().toString());
            descriptor.setReferenceSet(params.getReferenceSet());

            return descriptor;
    }

    public static void main(String[] args) {
       
        Instant start = Instant.now();
        //command line options
        if (args == null || args.length == 0 || args[0].equals("-h")) {
            BingoParameters.getHelp();
            System.exit(0);
        }

        Dingo sa = new Dingo();
        BingoParameters params;
        try {
            params = new BingoParameters(args);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        String ontologyName = params.getOntologyName();
        String ontologyFile = params.getOntologyFile();
        String annotationFile = params.getAnnotationFile();
        String species = params.getSpecies();

        Configurator config;

        if (species == null || species.length() == 0) {
            if (ontologyName.equalsIgnoreCase("HPO")) {
                species = "human";
                config = new Configurator(species);
            } else {
                System.out.println("Species must be defined!");
                return;
            }
        } else {
            config = new Configurator(species);
            if (!config.isSpeciesSupported(species)) {
                System.out.println("Species " + species + " is not supported!");
                return;
            }
        }

        config.createDiNGOFolders();
        //if annotation file is not specified try to use default one. In the case
        //default annotation file does not exist download it
        if (ontologyName.equalsIgnoreCase("GO")) {
            if (annotationFile == null) {
                String fileString = config.getDirFiles()
                        + config.getAnnotationFileName();
                int ln = fileString.length();
                annotationFile = fileString.substring(0, ln - 3);
                if (!new File(annotationFile).exists()) {
                    DiNGOFilesUpdater.main(new String[]{"update", "-f", "GO",
                        "-d", "2", "-sp", species});
                }
                params.setAnnotationFile(annotationFile);

            }
            //if ontology file is not specified try to use default one. In the case
            //default ontology file does not exist download it
            if (ontologyFile == null) {
                ontologyFile = config.getDirFiles()
                        + config.getGoOntologyFileName();
                if (!new File(ontologyFile).exists()) {
                    DiNGOFilesUpdater.main(new String[]{"update", "-f", "GO",
                        "-d", "1", "-sp", species});
                }

                params.setOntologyFile(ontologyFile);
            }
        } else if (ontologyName.equalsIgnoreCase("HPO")) {
            if (ontologyFile == null) {
                ontologyFile = config.getDirFiles()
                        + config.getHpoOntologyFileName();
                if (!new File(ontologyFile).exists()) {
                    DiNGOFilesUpdater.main(new String[]{"update", "-f", "HPO",
                        "-d", "1"});
                }
                params.setOntologyFile(ontologyFile);
            }
            if (annotationFile == null) {
                annotationFile = config.getDirFiles()
                        + config.getHpoFullAnnotationFileName();
                if (!new File(annotationFile).exists()) {
                    DiNGOFilesUpdater.main(new String[]{"update", "-f", "HPO",
                        "-d", "2"});
                }
                params.setAnnotationFile(annotationFile);
            }

        } else {
            System.out.println("Unknown ontology: " + ontologyName);
            return;
        }

        AnnotationParser annParser = sa.getAnnotationParser(params);
        StatisticsDescriptor descriptor = sa.getStatisticsDescriptor(params);
        String[] dataSets = getDataSets(params.getTextInput());

        displayDiNGOParameters(params);

        int numThreads = params.getNumberOfUsedCores();
        int numberOfSets = dataSets.length;
        //if batch mode is on use multi-threading
        if (params.getClusterName().equals("batch")) {

            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            System.out.println("Number of sets: " + numberOfSets);

            String clusterName;

            for (int i = 0; i < numberOfSets; ++i) {

                clusterName = dataSets[i].split("\\s+")[0].equals("")
                        ? dataSets[i].split("\\s+")[1]
                        : dataSets[i].split("\\s+")[0];

                executorService.execute(new BingoRunner(annParser, descriptor, dataSets[i], clusterName, params.getFileOutputDir(),
                        numberOfSets));
            }

            executorService.shutdown();
            boolean t = true;
            //wait all tasks to terminate than measure DiNGO execution time
            while (t) {
                boolean bMeasure = executorService.isTerminated();
                if (bMeasure) {
                    t = false;
                }
            }

        } else {
            
            System.out.println("Cluster name: " + params.getClusterName());
            BingoRunner test = new BingoRunner(annParser, descriptor, dataSets[0], params.getClusterName(), params.getFileOutputDir(),
                    numberOfSets);
            test.actionPerformed();
            System.out.println();
        }

        Instant end = Instant.now();
        long execTime = Duration.between(start, end).getSeconds();
        System.out.println("");
        System.out.println("Execution time: " + execTime + " s");
    }

}
