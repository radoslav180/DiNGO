package configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * <p>Class responsible for retrieving data from configuration file</p>
 */
public final class Configurator{
    private Properties properties;
    /**
     * <p>Name of operating system</p>
     */
    private static final String OS = System.getProperty("os.name").toLowerCase();
    /**
     * <p>Username used to connect to ftp server</p>
     */
    private String userName;
    /**
     * <p>Password required to login. The password is an e-mail address..
     * </p>
     */
    private String password;
    /**
     * <p>Path to configuration file</p>
     */
    private String confFilePath = "./configuration/conf.properties";
    /**
     * <p>Path to log file</p>
     */
    private String logFilePath;
    /**
     * <p>Path to directory containing annotation and ontology files</p>
     */
    private String dirFiles;
    /**
     * <p>Name of species. Default value human</p>
     */
    private String species = "human";
    /**
     * <p>Name of annotation file</p>
     */
    private String annotationFileName;
    
    /**
     * <p>Name of GO default ontology file</p>
     */
    private String goOntologyFileName;
    
    /**
     * <p>Name of HPO default file</p>
     */
    private String hpoOntologyFileName;
    
    private String inheritanceAnnotationFileName;
    
    private String organAnnotationFileName;
    
    private String courseAnnotationFileName;
    
    private String modifierAnnotationFileName;
    
    private String hpoFullAnnotationFileName;
    /**
     * <p>Url of go consortium</p>
     */
    private String goConsortiumAddress;
    /**
     * <p>Path to obo file on server</p>
     */
    private String pathToOboFile;
    /**
     * <p>Path to annotation file on server</p>
     */
    private String pathToAnnotationFile;
    //private String fileName;
    /**
     * <p>Url of HPO where obo file is located</p>
     */
    private String hpOboAddress;
    /**
     * <p>Url of HPO where annotation file is located</p>
     */
    private String hpAnnotationAddress;

    /**
     * <p>Url of HPO phenotype_annotation.tab file</p>
     */
    private String hpPhenotypeAnnotationAddress;
    
    /**
     * <p>Link to HUGO file</p>
     */
    private String hugoFileAddress;
    
    /**
     * <p>Link to UniProt</p>
     */
    private String uniProtFtpAddress;
    
    /**
     * <p>path to UniProt mapping files</p>
     */
    private String uniProtByOrganismFolder;
    
    private String uniprotSwissFastFileFolder;
    
    /**
     * <p>Name of uniprot mapping file marked for download</p>
     */
    private String uniProtMappingFileName;
    
    /**
     * <p>Folder where program puts downloaded files</p>
     */
    private String downloadFolder;
    
    /**
     * <p>Folder containing mapping files</p>
     */
    
    private String mappingFolder;
    
    /**
     * <p>Value of port</p>
     */
    private int port;

    /**
     * <p>Default constructor</p>
     */
    public Configurator(){
        init(species);
    }

    /**
     * <p>Constructor</p>
     * @param species name of species. For example mouse
     */
    public Configurator(String species){
        this.species = species;
        if(OS.contains("win")){
            String osType = confFilePath.replace("/", "\\\\");
        }
        init(species);
    }

    /**
     * <p>private method that initiates all variables using configuration file</p>
     * @param species
     */
    private void init(String species){
        properties = new Properties();
        try(InputStream input = new FileInputStream(this.confFilePath)){
            properties.load(input);
            userName = properties.getProperty("user");
            password = properties.getProperty("password");
            //this.confFilePath = properties.getProperty("conf_file");
            logFilePath = properties.getProperty("log_file");
            dirFiles = properties.getProperty("files_dir");
            annotationFileName = properties.getProperty(species);
            goOntologyFileName = properties.getProperty("go_ontology_file");
            hpoOntologyFileName = properties.getProperty("hpo_ontology_file");
            inheritanceAnnotationFileName = properties.getProperty("human_I");
            courseAnnotationFileName = properties.getProperty("human_C");
            modifierAnnotationFileName = properties.getProperty("human_M");
            organAnnotationFileName = properties.getProperty("human_O");
            hpoFullAnnotationFileName = properties.getProperty("human_W");
            goConsortiumAddress = properties.getProperty("gene_ontology_consortium");
            pathToOboFile = properties.getProperty("go_obo_folder");
            pathToAnnotationFile = properties.getProperty("go_annotation_folder");
            hpOboAddress = properties.getProperty("hpo_obo");
            hpAnnotationAddress = properties.getProperty("hpo_annotation");
            hpPhenotypeAnnotationAddress = properties.getProperty("hpo_phenotype_annotation");
            hugoFileAddress = properties.getProperty("hugo_mapping_file");
            uniProtFtpAddress = properties.getProperty("uniprot_link");
            uniProtByOrganismFolder = properties.getProperty("uniprot_mapping_files");
            mappingFolder = properties.getProperty("mapping_folder");
            uniProtMappingFileName = properties.getProperty(species + "_uni");
            downloadFolder = properties.getProperty("download_folder");
            uniprotSwissFastFileFolder = properties.getProperty("uniprot_swiss_prot");
            port = Integer.parseInt(properties.getProperty("port"));
            
        }catch(IOException ex){
            Logger.getLogger("configuration").info(ex.getMessage());
        }
    }
    /**
     * <p>Method checks if DiNGO folders exist if not creates them</p>
     */
    public void createDiNGOFolders(){
        File downloadFolderLocal = new File(this.downloadFolder);
        File annotationFolder = new File(this.dirFiles);
        File mappingFolderLocal = new File(this.mappingFolder);
        
        if(!downloadFolderLocal.exists()){
            boolean bValue = downloadFolderLocal.mkdir();
            if(bValue){
                System.out.println("Folder " + downloadFolderLocal.getName() + " has been created!");
            }else{
                System.out.println("");
            }
        }
        if(!annotationFolder.exists()){
            boolean bValue = annotationFolder.mkdir();
            if(bValue){
                System.out.println("Folder " + annotationFolder.getName() + " has been created!");
            }
        }
        if(!mappingFolderLocal.exists()){
            boolean bValue = mappingFolderLocal.mkdir();
            if(bValue){
                System.out.println("Folder " + mappingFolderLocal.getName() + " has been created!");
            }
        }
    
    }
    
    /**
     * <p>Getter method</p>
     * @return value of {@link #userName}
     */
    public String getUserName(){
        return this.userName;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #password}
     */
    public String getPassword(){
        return password;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #confFilePath}
     */
    public String getConFilePath(){
        return confFilePath;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #logFilePath}
     */
    public String getLogFilePath(){
        return logFilePath;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #dirFiles}
     */
    public String getDirFiles(){
        return dirFiles;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #species}
     */
    public String getSpecies(){
        return species;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #annotationFileName}
     */
    public String getAnnotationFileName(){
        return annotationFileName;
    }
    
    public String getGoOntologyFileName(){
        return goOntologyFileName;
    }
    
    public String getHpoOntologyFileName(){
        return hpoOntologyFileName;
    }

    public String getInheritanceAnnotationFileName() {
        return inheritanceAnnotationFileName;
    }

    public String getOrganAnnotationFileName() {
        return organAnnotationFileName;
    }

    public String getCourseAnnotationFileName() {
        return courseAnnotationFileName;
    }

    public String getModifierAnnotationFileName() {
        return modifierAnnotationFileName;
    }

    public String getHpoFullAnnotationFileName() {
        return hpoFullAnnotationFileName;
    }
    /**
     * <p>Method that checks if species is supported</p>
     * @param sp species name
     * @return true if species is supported
     */
    public boolean isSpeciesSupported(String sp){
        return properties.containsKey(sp);
    }
    
    /**
     * <p>Getter method</p>
     * @return value of {@link #pathToOboFile}
     */
    public String getPathToOboFile(){
        return pathToOboFile;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #goConsortiumAddress}
     */
    public String getGoConsortiumAddress(){
        return goConsortiumAddress;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #pathToAnnotationFile}
     */
    public String getPathToAnnotationFile(){
        return pathToAnnotationFile;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #hpOboAddress}
     */
    public String getHpOboAddress(){
        return hpOboAddress;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #hpAnnotationAddress}
     */
    public String getHpAnnotationAddress(){
        return hpAnnotationAddress;
    }

    /**
     * <p>Getter method</p>
     * @return value of {@link #hpPhenotypeAnnotationAddress}
     */
    public String getHpPhenotypeAnnotationAddress(){
        return hpPhenotypeAnnotationAddress;
    }
    
    /**
     * <p>Getter method</p>
     * @return value of {@link #downloadFolder}
     */
    public String getDownloadFolder() {
        return downloadFolder;
    }
    
    /**
     * <p>Getter method</p>
     * @return value of {@link #hugoFileAddress}
     */
    public String getHugoFileAddress() {
        return hugoFileAddress;
    }
    
    /**
     * <p>Getter method</p>
     * @return value of {@link #mappingFolder}
     */
    public String getMappingFolder() {
        return mappingFolder;
    }

    public String getUniProtFtpAddress() {
        return uniProtFtpAddress;
    }

    public String getUniProtByOrganismFolder() {
        return uniProtByOrganismFolder;
    }

    public String getUniProtMappingFileName() {
        return uniProtMappingFileName;
    }

    public String getUniprotSwissFastFileFolder() {
        return uniprotSwissFastFileFolder;
    }
    
    /**
     * <p>Getter method</p>
     * @return value of {@link #port}
     */
    public int getPort(){
        return port;
    }
}