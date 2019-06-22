package bingo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by User: risserlin Date: Jun 12, 2006 Time: 8:30:34 AM Modified by
 * Radoslav Davidović July 19, 2018 Modified class is able to receive and parse
 * command line arguments. The class was subject to numerous changes, so except
 * name there is a little similarity to the original class.
 * <ol>Changes:
 * class</li>
 * <li>some variables were removed and some were added
 * <ul>Removed variables:
 * <li>private Properties bingo_props</li>
 * <li>private String bingoDir</li>
 * <li>private boolean annotation_default</li>
 * <li>private boolean ontology_default</li>
 * <li>private TreeMap speciesfileHash</li>
 * <li>private TreeMap filespeciesHash</li>
 * <li>private TreeMap ontologyHash</li>
 * <li>private TreeMap namespaceHash</li>
 * <li>private int number_species, number_ontology, number_namespaces</li>
 * </ul>
 * <ul>Added variables:
 * <li>private String inputFileName</li>
 * <li>{@link #numberOfUsedCores}</li>
 * <li>{@link #mappingFile}</li>
 * </ul>
 * </li>
 * <li>the following methods were removed:
 * <ul>
 * <li>public InputStream PropReader(String)</li>
 * <li>public OutputStream PropWriter(String)</li>
 * <li>public void storeParameterSettings()</li>
 * <li>public void initializeSpeciesHash()</li>
 * <li>public void initializeOntologyHash()</li>
 * <li>public String[] getOntologyLabels()</li>
 * <li>public String[] getNamespaceLabels()</li>
 * <li>public boolean isAnnotation_default()</li>
 * <li>public TreeMap getOntologyHash()</li>
 * <li>public String getSpeciesFilename(String)</li>
 * <li>public String getSpeciesNameFromFilename(String)</li>
 * <li>public String[] getSpeciesLabels()</li>
 * <li>public boolean isOntology_default()</li>
 * <li>public Properties getbingo_props()</li>
 * </ul>
 * </li>
 * </ol>
 * <ol>The following methods were added:
 * <li>{@link #getHelp()}</li>
 * <li>{@link #getTextInput()}</li>
 * <li>{@link #getNumberOfUsedCores() }</li>
 * <li>{@link #getMappingFile() }</li>
 * </ol>
 */

public final class BingoParameters {

    // parameters parsed from the command line
    /**
     * <p>
     * name of input cluster. In the case of batch mode value is always
     * batch</p>
     */
    private String clusterName;
    /**
     * <p>
     * contains all gene/protein identifications provided by user</p>
     */
    private String textInput;
    /**
     * <p>
     * string describing statistical test that is going to be used</p>
     */
    private String test;
    /**
     * <p>
     * calculate over- or under-representation</p>
     */
    private String overOrUnder;

    /**
     * <p>
     * string describing statistical correction test that is going to be
     * used</p>
     */
    private String correctionTest;
    /**
     * <p>
     * cutt-off value for p</p>
     */
    private BigDecimal significance;
    /**
     *
     */
    private String category;
    /**
     * <p>
     * defines reference set that is going to be used. Default value genome</p>
     */
    private String referenceSet;
    /**
     * <p>
     * name of species</p>
     */
    private String species;
    // annotation and ontology files
    /**
     * <p>
     * name of annotation file</p>
     */
    private String annotationFile;
    /**
     * <p>
     * name of ontology file</p>
     */
    private String ontologyFile;

    /**
     * <p>TAB delimited file containing various gene/protein identifications</p>
     */
    private String mappingFile;

    /**
     * <p>
     * defines subontology of an ontology</p>
     */
    private String namespace = "O";
    /**
     * <p>
     * contains codes that should be avoided in the analysis</p>
     */
    private Set<String> deleteCodes;//dodat tip

    /**
     * <p>
     * path to the folder where results will be saved</p>
     */
    private String fileOutputDir;
    /**
     * <p>Name of the ontology. For example GO</p>
     */
    private String ontologyName;


    private String inputFileName;

    private int numberOfUsedCores;


    /**
     * <p>Constructor</p>
     * @param argumentsValues command line arguments
     *
     */
    public BingoParameters(String[] argumentsValues){
        //default values
        this.deleteCodes = new HashSet<>();
        this.overOrUnder = "Overrepresentation";
        this.category = Constants.CATEGORY_CORRECTION.getConstant();
        this.test = Constants.HYPERGEOMETRIC.getConstant();
        this.correctionTest = Constants.BENJAMINI_HOCHBERG_FDR.getConstant();
        this.referenceSet = Constants.GENOME.getConstant();
        this.fileOutputDir = "./";
        this.numberOfUsedCores = 1;
        this.significance = new BigDecimal("0.05");
        this.ontologyName = "GO";

        initParameters(argumentsValues);
        getInputFromFile();
    }

    //intitialize parameters
    private void initParameters(String[] args) {
        //initNamespaceMap();
      
        int len = args.length;
        for (int i = 0; i < len - 1; i++) {
            if (args[i].equals("-o")) {
                this.clusterName = args[i + 1];
            }
            if (args[i].equals("-i")) {
                this.inputFileName = args[i + 1];
                if(!new File(this.inputFileName).exists()){
                    throw new IllegalArgumentException("File " + inputFileName +
                            " does not exist!");
                }
            }
            if (args[i].equals("-r")) {
                switch (args[i + 1]) {
                    case "1":
                        this.overOrUnder = "Overrepresentation"; //BingoAlgorithm.OVERSTRING;
                        break;
                    case "2":
                        this.overOrUnder = "Underrepresentation";
                        break;
                    default:
                        throw new IllegalArgumentException("After -r flag one the following values can be expected:\n"
                                + "1 - over-representation\n2 - under-representation\nFound value "
                                + args[i + 1] + "!\n");
                }
            }
            if (args[i].equals("-c")) {
                switch (args[i + 1]) {
                    case "1":
                        this.category = Constants.CATEGORY_BEFORE_CORRECTION.getConstant();
                        break;
                    case "2":
                        this.category = Constants.CATEGORY_CORRECTION.getConstant();
                        break;
                    default:
                        throw new IllegalArgumentException("The category argument (-c flag)"
                                + " can take the following values:\n "
                                + "\n1 - Overrepresented categories after "
                                + "correction\n 2 - Overrepresented categories"
                                + " before correction\nFound value: " + args[i + 1]);
                }
            }
            if (args[i].equals("-st")) {
                switch (args[i + 1]) {
                    case "1":
                        this.test = Constants.HYPERGEOMETRIC.getConstant();
                        break;
                    case "2":
                        this.test = Constants.BINOMIAL.getConstant();
                        break;
                    default:
                        throw new IllegalArgumentException("Flag -st allowed values:\n"
                                + "1 - Hypergeometric test\n2 - Binomial test\n"
                                + "Found value: " + args[i + 1]);
                }
            }
            if (args[i].equals("-ct")) {
                switch (args[i + 1]) {
                    case "1":
                        this.correctionTest = Constants.BENJAMINI_HOCHBERG_FDR.getConstant();
                        break;
                    case "2":
                        this.correctionTest = Constants.BONFERRONI.getConstant();
                        break;
                    case "3":
                        this.correctionTest = Constants.NONE.getConstant();
                        break;
                    default:
                        throw new IllegalArgumentException("Flag -ct allowed values:\n"
                                + "1 - Benjamini & Hochberg False Discovery Rate (FDR) correction"
                                + "\n2 - Bonferroni\n3 - No correction\n"
                                + "Found value: " + args[i + 1]);
                }
            }
            if (args[i].equals("-rs")) {
                if (args[i + 1].equals("1")) {
                    this.referenceSet = Constants.GENOME.getConstant();
                } else {
                    this.referenceSet = args[i + 1];
                }
            }
            if (args[i].equals("-of")) {
                this.ontologyFile = args[i + 1];
                if(!new File(ontologyFile).exists()){
                    throw new IllegalArgumentException("File " + ontologyFile +
                            " does not exist!");
                }
                
            }
            if (args[i].equals("-ns")) {
                this.namespace = args[i + 1].toUpperCase();
            }
            if (args[i].equals("-af")) {
                this.annotationFile = args[i + 1];
                 if(!new File(annotationFile).exists()){
                    throw new IllegalArgumentException("File " + annotationFile +
                            " does not exist!");
                }
            }
            if (args[i].equals("-dc")) {
                this.deleteCodes = new HashSet<>(Arrays.asList(args[i + 1].split(":")));
            }
            if (args[i].equals("-sf")) {
                this.fileOutputDir = args[i + 1];
                File file = new File(fileOutputDir);
                if (!file.exists()) {
                    System.out.println("Folder " + file.getAbsolutePath() + 
                            " has not been found!\nCreating the folder...");

                    boolean isMade = file.mkdir();
                    if(isMade){
                        System.out.println("The folder has been created!");
                    } else{
                        System.out.println("Creation of folder " + file + 
                                " failed! Create folder manually and " +
                                "run application again!");
                        throw new IllegalArgumentException("");
                    }

                }
            }
            if (args[i].equals("-p")) {
                this.significance = new BigDecimal(args[i + 1]);
                if(this.significance.compareTo(new BigDecimal(1)) > 0 || 
                        this.significance.compareTo(new BigDecimal(0)) < 0){
                    throw new IllegalArgumentException("p value must be in the "
                            + "following range 0 \u2264 p \u2264 1");
                }
            }
            if (args[i].equals("-t")) {
                try {
                    numberOfUsedCores = Integer.parseInt(args[i + 1]);

                } catch (NumberFormatException ex) {
                    System.out.println(args[i + 1] + " is not an integer!");
                    throw new IllegalArgumentException("Number of threads must "
                            + "be an integer");
                }
            }
            if (args[i].equals("-s")) {
                this.species = args[i + 1];
            }
            if (args[i].equals("-e")) {
                this.ontologyName = args[i + 1];
            }

            if (args[i].equals("-m")) {
                this.mappingFile = args[i + 1];
            }

            if (args[i].equals("-h")) {
                getHelp();
            }
        }
        
        if(getNamespaceFullName().equals("unspecified")){
            throw new IllegalArgumentException("Unknown namespace " + namespace);
        }
        
        if(ontologyName.equalsIgnoreCase("HPO")){
            this.species = "human";
        }
        if(ontologyName.equalsIgnoreCase("GO")){
            
            if(namespace.equals("O") || namespace.equals("I") || namespace.
                    equals("M") || namespace.equals("C") || namespace.equals("F")
                    ){
                
                throw new IllegalArgumentException("GO does not contain "
                        + " subontology " + namespace + " (" + getNamespaceFullName() + ")");
            
            }
        }
        if(ontologyName.equalsIgnoreCase("HPO")){
            if(namespace.equals("BP") || namespace.equals("CC") || namespace.
                    equals("MF")){
                
                throw new IllegalArgumentException("HPO does not contain "
                        + " subontology " + namespace + " (" + getNamespaceFullName() + ")");
            
            }
        }
        
        namespace = getNamespaceFullName();//namespaceMap.get(namespace);
        //System.out.println(namespace);
    }
    
    private String getNamespaceFullName(){
        String fullName;
        switch(namespace){
            case "O": fullName = "phenotypic_abnormality";
                break;
            case "I": fullName = "mode_of_inheritance";
                break;
            case "M": fullName = "clinical_modifier";
                break;
            case "C": fullName = "clinical_course";
                break;
            case "F": fullName = "frequency";
                break;
            case "MF": fullName = "molecular_function";
                break;
            case "BP": fullName = "biological_process";
                break;
            case "CC": fullName = "cellular_component";
                break;
            case "W": fullName = "---";
                break;
            
            default: fullName = "unspecified";
                break;
        }
        
        return fullName;
    }
   
    /**
     * <p>
     * Prints help on the screen</p>
     */
    public static void getHelp() {
        System.out.println("Usage:");
        System.out.println("    java -jar DiNGO.jar [options]\n");
        System.out.println("Options:");
        System.out.printf("%-30s%s%n", "    -o <oFile>", "Output file name\n");
        System.out.printf("%-30s%s%n", "    -i <iFile>", "Input file containing list of genes/proteins\n");
        System.out.printf("%-30s%s%n", "    -r <representation>", "Over or under [default: over]\n");
        System.out.printf("%-30s%s%n", "    -c <category>", "1 category before correction");
        System.out.printf("%-30s%s%n", "                 ", "2 category after correction [default: 2]\n");
        System.out.printf("%-30s%s%n", "    -st <statistical test>", "1 hypergeometric");
        System.out.printf("%-30s%s%n", "                          ", "2 binomial [default: 1]\n");
        System.out.printf("%-30s%s%n", "    -rs <reference set>", "1 whole annotation");
        System.out.printf("%-30s%s%n", "                       ", "custom reference set [default: 1]\n");
        System.out.printf("%-30s%s%n", "    -ns <namespace>", "O phenotypic abnormality (HPO)");
        System.out.printf("%-30s%s%n", "                   ", "I mode of inheritance (HPO)");
        System.out.printf("%-30s%s%n", "                   ", "M clinical modifier (HPO)");
        System.out.printf("%-30s%s%n", "                   ", "C clinical course (HPO)");
        System.out.printf("%-30s%s%n", "                   ", "MF molecular function (GO)");
        System.out.printf("%-30s%s%n", "                   ", "BP biological process (GO)");
        System.out.printf("%-30s%s%n", "                   ", "CC cellular_component (GO)");
        System.out.printf("%-30s%s%n", "                   ", "W all namespaces (GO or HPO)\n");
        System.out.printf("%-30s%s%n", "    -of <ontology file>", "File in obo or flat file format [default: DiNGO default obo file]\n");
        System.out.printf("%-30s%s%n", "    -af <annotation file>", "Requires gaf or flat file format [default: DiNGO default annotation file]\n");
        System.out.printf("%-30s%s%n", "    -dc <delete codes>", "Evidence codes\n");
        System.out.printf("%-30s%s%n", "    -sf <folder>", "Folder where results will be saved [default: ./]\n");
        System.out.printf("%-30s%s%n", "    -p <significance level>", "Defines treshold value [default: 0.05]\n");
        System.out.printf("%-30s%s%n", "    -e <name of ontology>", "HPO or GO [default: GO]\n");
        System.out.printf("%-30s%s%n", "    -t <number of threads>", "Number of threads (works only for batch mode) [default: 1]\n");
        System.out.printf("%-30s%s%n", "    -s <species>", "Name of species\n");
        System.out.printf("%-30s%s%n", "    -m <mapping file>", "TAB delimited file containing IDs\n");
        System.out.printf("%-30s%s%n", "    -h", "Prints this help");
    }

    /**
     * <p>
     * Extracts gene/proteins identifications from file and stores them in
     * {@link #textInput}</p>
     */
    private void getInputFromFile() {
        StringBuilder inputB = new StringBuilder();
        String input;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {

            while ((input = reader.readLine()) != null) {
                inputB.append(input.replace("\n", "").replace("\t", "")
                        .replace("\r", "").replace(" ", "_")).append(" ");
            }
            textInput = inputB.toString().trim();
        } catch (IOException msg) {
            System.out.println(msg.getMessage());
        }
    }

    public int getNumberOfUsedCores() {
        return numberOfUsedCores;
    }

    public Set<String> getDeleteCodes() {
        return deleteCodes;
    }
   
    //--------------getter and setter methods---------------------------//

    public String getTextInput() {
        return textInput;
    }

    public String getSpecies() {
        return species;
    }

    public String getOverOrUnder() {
        return overOrUnder;
    }

    public String getCategory() {
        return category;
    }

    public String getReferenceSet() {
        return referenceSet;
    }

    public String getAnnotationFile() {
        return annotationFile;
    }

    public String getOntologyFile() {
        return ontologyFile;
    }

    public String getNameSpace() {
        return namespace;
    }

    public String getFileOutputDir() {
        return fileOutputDir;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getTest() {
        return test;
    }

    public String getCorrectionTest() {
        return correctionTest;
    }

    public String getOntologyName(){ return ontologyName;}

    public BigDecimal getSignificance() {
        return significance;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public void setAnnotationFile(String annotationFile) {
        this.annotationFile = annotationFile;
    }

    public void setOntologyFile(String ontologyFile) {
        this.ontologyFile = ontologyFile;
    }
}
