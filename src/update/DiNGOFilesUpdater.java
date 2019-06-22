/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package update;

import configurator.Configurator;
import hugo.HUGOFlatFileCreator;
import hugo.HUGOXmlDownloader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Class is responsible for updating DiNGO files</p>
 *
 * @author Radoslav Davidović
 */
public final class DiNGOFilesUpdater {

    private final Configurator configurator;
    private IFileDownload fileDownloader;
    private final String speciesName;

    private DiNGOFilesUpdater(String speciesName) {
        this.speciesName = speciesName;
        configurator = new Configurator(speciesName);
    }

    //is there really need to download new file?
    private static boolean compareDate(Date oldDate, Date newDate) {
        if (oldDate == null) {
            return true;
        }
        //System.out.println("Old date: " + oldDate + "\nNew Date: " + newDate);
        return newDate.compareTo(oldDate) > 0;
    }
    
    //replace target with source file
    private static boolean replaceFile(String source, String target) {
        boolean success = true;

        try {
            Files.move(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            success = false;
        }
        return success;
    }
    //implementation of progress bar
    private class ProgressBar extends Thread {

        private boolean showProgressBar = true;

        @Override
        public void run() {
            String label = "Downloading";
            while (showProgressBar) {

                String anim = "|/-\\";
                for (int x = 0; x < anim.length(); x++) {
                    char data = anim.charAt(x);

                    try {
                        System.out.print(label + " " + data + "\r");
                        Thread.sleep(75);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProgressBar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
            System.out.println("                   ");
        }

        public boolean isShowProgressBar() {
            return showProgressBar;
        }

        public void setShowProgressBar(boolean showProgressBar) {
            this.showProgressBar = showProgressBar;
        }

    }

    //add namespace for each term in obo file
    private boolean addNamespacesToObo(String oboFile) {
        //in the case propagation module missing return false
        try {
            Class.forName("propagation.Propagation");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DiNGOFilesUpdater.class.getName()).log(Level.WARNING, null, ex);
            return false;
        }
        System.out.println("Reading " + oboFile);
        propagation.Propagation propagation = new propagation.Propagation(oboFile);

        System.out.println("Reading done!\nFound " + propagation.getLeafTerms().size() + " leaf terms");
        System.out.println("Propagating leaf terms...");

        propagation.propagateAndMapLeafTerms();
        Map<String, String> termToNamespace = propagation.getTermToNamespace();

        System.out.println("Mapped " + termToNamespace.size() + " terms to namespace");
        //System.out.println("Adding namespace info to OBO file...");

        return propagation.addNamespaceInfoToOboFile(configurator.getDownloadFolder()
                + configurator.getHpoOntologyFileName());
    }

    /**
     * <p>
     * Updates selected GO file</p>
     *
     * @param fileName name of downloaded file
     * @param relativePathToGOFile relative path to file on server
     */
    private void updateGOFile(String fileName, String relativePathToGOFile) {
        //String subFolder = configurator.getPathToOboFile();
        String dingoFilesFolder = configurator.getDirFiles();//folder with annotation resources
        String downloadFolder = configurator.getDownloadFolder();//DiNGO download folder
        String link = configurator.getGoConsortiumAddress();//get link

        System.out.println("Downloading " + fileName + " from " + link);
        FtpConnection connection = FtpConnection.getInstance(link, configurator.
            getUserName(), configurator.getPassword(), configurator.getPort());
        
        fileDownloader = new GOFileDownloader(connection, link, relativePathToGOFile, downloadFolder);

        ProgressBar pBar = new ProgressBar();
        pBar.start();

        fileDownloader.downloadFile(fileName);
        pBar.setShowProgressBar(false);
        //if file is gzipped remove gz extensuion from file name
        if (fileName.endsWith(".gz")) {

            fileName = fileName.substring(0, fileName.lastIndexOf("."));

        }

        Date lastModDate = LocalFilesManager.extractOboReleaseDate(downloadFolder + fileName);//fileDownloader.getReleaseDate();
        Date currentFileDate = LocalFilesManager.extractOboReleaseDate(dingoFilesFolder + fileName);

        System.out.println("Releasing date of current file located at " + dingoFilesFolder + ": " + currentFileDate);
        System.out.println("Releasing date of downloaded file located at " + downloadFolder + ": " + lastModDate);

        if (compareDate(currentFileDate, lastModDate)) {

            if (replaceFile(downloadFolder + fileName, dingoFilesFolder + fileName)) {

                System.out.println("File " + configurator.getDirFiles() + fileName + " has been successfully updated!");

            } else {

                System.out.println("File " + configurator.getDownloadFolder() + fileName + " can't be moved to "
                        + configurator.getDirFiles() + " Try moving the file manually.");

            }
        } else {

            System.out.println("File is up-to-date!");
        }
    }

    /**
     * <p>
     * Updates GO ontology file</p>
     */
    private void updateGOOboFile() {
        updateGOFile("go.obo", configurator.getPathToOboFile());
    }

    /**
     * <p>
     * Updates GO annotation file (GAF file)</p>
     */
    private void updateGafFile() {
        boolean isFileExists = true;
        String annotationFileName = configurator.getAnnotationFileName();
        if(annotationFileName == null){
            System.out.println("Can't find annotation file for species " + 
                    speciesName);
            isFileExists = false;
        }
        if(isFileExists){
            updateGOFile(annotationFileName, configurator.getPathToAnnotationFile());
        }
            
    }

    private void updateUniProtMappingFile() {
        String uniProtFtpAddress = configurator.getUniProtFtpAddress();
        String pathToUniProtFiles = configurator.getUniProtByOrganismFolder();
        //System.out.println(pathToUniProtFiles);
        String downloadFolder = configurator.getMappingFolder();
        String mappingFileName = configurator.getUniProtMappingFileName();
        boolean isFileExist = true;
        if (mappingFileName == null) {
            System.out.println("Can't find idmapping.dat file for species "
                    + speciesName);
            isFileExist = false;
        }
        if (isFileExist) {
            FtpConnection connection = FtpConnection.getInstance(uniProtFtpAddress, 
                    configurator.getUserName(), configurator.getPassword(), 
                    configurator.getPort());
            fileDownloader = new GOFileDownloader(connection, uniProtFtpAddress,
                    pathToUniProtFiles, downloadFolder);
            ProgressBar pBar = new ProgressBar();
            pBar.start();
            fileDownloader.downloadFile(mappingFileName);
            pBar.setShowProgressBar(false);

        }

    }

    private void updateSwissProtFastaFile() {
        String uniProtFtpAddress = configurator.getUniProtFtpAddress();
        String pathToUniProtFiles = configurator.getUniprotSwissFastFileFolder();
        String downloadFolder = configurator.getMappingFolder();
        String fileName = "uniprot_sprot.fasta.gz";//hardcoded value not good:(
        FtpConnection connection = FtpConnection.getInstance(uniProtFtpAddress, 
                    configurator.getUserName(), configurator.getPassword(), 
                    configurator.getPort());
        fileDownloader = new GOFileDownloader(connection, uniProtFtpAddress,
                pathToUniProtFiles, downloadFolder);
        ProgressBar pBar = new ProgressBar();
        pBar.start();
        fileDownloader.downloadFile(fileName);
        pBar.setShowProgressBar(false);

    }

    /**
     * <p>
     * Downloads GO ontology and annotation gaf files</p>
     */
    private void updateGOFiles() {
        //download obo file
        updateGOOboFile();
        //download annotation file
        updateGafFile();
    }

    private void updateHPOboFile(String hpOntology) {
        String oboFile = "hp.obo";
        String address = configurator.getHpOboAddress();
        String dataFolder = configurator.getDirFiles();
        String downloadFolder = configurator.getDownloadFolder();
        String currentOboFile = configurator.getHpoOntologyFileName();

        fileDownloader = new HpoFileDownloader(address, downloadFolder, hpOntology);
        System.out.println("Downloading " + oboFile + " from " + address);

        ProgressBar pBar = new ProgressBar();
        pBar.start();

        fileDownloader.downloadFile(oboFile);
        pBar.setShowProgressBar(false);

        Date currentOborelease = LocalFilesManager.extractOboReleaseDate(dataFolder
                + currentOboFile);
        Date newOboRelease = fileDownloader.getReleaseDate();

        System.out.println("Releasing date of current file located at "
                + dataFolder + ": " + currentOborelease);
        System.out.println("Releasing date of downloaded file located at "
                + downloadFolder + ": " + newOboRelease);

        boolean isObsolete = compareDate(currentOborelease, newOboRelease);

        if (isObsolete) {
            boolean isNamespaceAdded = addNamespacesToObo(configurator.getDownloadFolder() + oboFile);
            //String oboFileName = isNamespaceAdded ? configurator.getHpoOntologyFileName() : "hp.obo";
            if (!isNamespaceAdded) {
                LocalFilesManager.renameFile(configurator.getDownloadFolder()
                        + oboFile, configurator.getDownloadFolder()
                        + configurator.getHpoOntologyFileName());
            }
            //if (isNamespaceAdded) {

            boolean isUpdated = replaceFile(configurator.getDownloadFolder()
                    + configurator.getHpoOntologyFileName(),
                    configurator.getDirFiles()
                    + configurator.getHpoOntologyFileName());

            if (isUpdated) {
                System.out.println("File " + configurator.getDirFiles()
                        + configurator.getHpoOntologyFileName()
                        + " has been successfully updated!");
            } else {
                System.out.println("File " + configurator.getDownloadFolder()
                        + configurator.getHpoOntologyFileName()
                        + " can't be moved to " + configurator.getDirFiles());
            }

            //}
        }
    }

    private void updateHPAnnotationFile(String hpOntology) {
        String address = configurator.getHpAnnotationAddress();
        String downloadFolder = configurator.getDownloadFolder();
        fileDownloader = new HpoFileDownloader(address, downloadFolder, hpOntology);

        HpoFileDownloader hfd = (HpoFileDownloader) fileDownloader;
        //System.out.println("Downloading " + fileName + " from " + link);
        ProgressBar pBar = new ProgressBar();
        pBar.start();

        hfd.generateHpoAnnotationFile(configurator.getHpPhenotypeAnnotationAddress());
        pBar.setShowProgressBar(false);

        boolean isAnnotationUpdated = replaceFile(configurator.getDownloadFolder()
                + hfd.getAnnotationFileName(), configurator.getDirFiles() + hfd.getAnnotationFileName());

        if (isAnnotationUpdated) {
            System.out.println("Annotation file " + configurator.getDirFiles() + hfd.getAnnotationFileName()
                    + " has been successfully updated!");
        } else {
            System.out.println("File " + configurator.getDownloadFolder() + hfd.getAnnotationFileName()
                    + " can't be moved to " + configurator.getDirFiles());
        }
    }

    /**
     * <p>
     * Updates HPO ontology and annotation files</p>
     *
     * @param hpOntology namespace (for example, I stands for Mode of
     * Inheritance)
     */
    private void updateHPOFiles(String hpOntology) {
        updateHPOboFile(hpOntology);
        updateHPAnnotationFile(hpOntology);
    }

    /**
     * <p>
     * Downloads XML file and converts it to flat file. Updates HUGO file</p>
     */
    private void updateHUGOFile() {
        String link = configurator.getHugoFileAddress();
        String fileName = configurator.getMappingFolder() + "hugo_mapping_id.xml";

        System.out.println("Downloading HUGO XML file from " + link);
        System.out.println("This may take a while...");

        ProgressBar pBar = new ProgressBar();
        pBar.start();

        HUGOXmlDownloader downloader = new HUGOXmlDownloader(link, fileName);
        pBar.setShowProgressBar(false);

        boolean success = downloader.downloadHUGOFile();
        //String filename = downloader.getFileName();
        if (success) {
            System.out.println("Parsing " + fileName);
            HUGOFlatFileCreator test = new HUGOFlatFileCreator(fileName);
            test.createFlatFile(configurator.getMappingFolder() + "ids_mapping.tab");
            System.out.println("Done!");
        }
    }

    public static void main(String[] args) {
        //prints help
        if (args.length <= 1) {
            System.out.println("Description:");
            System.out.println("DiNGOFilesUpdater downloads GO, HPO, HUGO and "
                    + "UniProt mapping files \nand updates DiNGO default files.");
            System.out.println("Usage:");
            System.out.println("update -f <file type> -sp <species|namespace> -d <files to update>\n");
            System.out.println("Options:");
            System.out.printf("%-27s%s\n", "-f <file type>", "GO GO files");
            System.out.printf("%-27s%s\n", "              ", "HPO HPO files");
            System.out.printf("%-27s%s\n", "              ", "HUGO HUGO id mapping file");
            System.out.printf("%-27s%s\n", "              ", "UniProt UniProt id mapping file");
            System.out.printf("%-27s%s\n", "              ", "SwissProt contains fasta sequences of manually reviewed UniProt entries");
            System.out.printf("%-27s%s\n", "-sp <species|namespace>", "species for GO [default: human]");
            System.out.printf("%-27s%s\n", "                       ", "namespace for HPO [default: W]");
            System.out.printf("%-27s%s\n", "                       ", "species for UniProt [default: human]");
            System.out.printf("%-27s%s\n", "-d <files to update>", "1 update OBO file");
            System.out.printf("%-27s%s\n", "                    ", "2 update annotation file");
            System.out.printf("%-27s%s\n", "                    ", "3 update OBO and annotation file");
            return;
        }
        if (!args[0].equals("update")) {
            System.out.println("The first argument must be update.");
            System.out.println("Unknown command: " + args[0]);
            return;
        }
        //list containing labels for HPO subontologies
        //O = organ abnormality
        //C = clinical course
        //I = mode of inheritance
        //M = clinical modifier
        //W = all subontologies
        List<String> hpSubOntology = new ArrayList<>(Arrays.asList("O", "C", "I", "M", "W"));
        Configurator conf = new Configurator();
        conf.createDiNGOFolders();
        DiNGOFilesUpdater filesUpdater;
        String fileType = "";
        String attribute = "";
        String dValue = "3";
        int len = args.length;

        for (int i = 1; i < len - 1; i++) {

            if (args[i].equals("-f")) {

                fileType = args[i + 1];
            }
            if (args[i].equals("-sp")) {

                attribute = args[i + 1];
            }
            if (args[i].equals("-d")) {
                dValue = args[i + 1];
            }
        }

        if (fileType.equalsIgnoreCase("GO")) {
            if (attribute.length() == 0) {
                attribute = "human";
            }
            filesUpdater = new DiNGOFilesUpdater(attribute);
            switch (dValue) {
                case "1":
                    filesUpdater.updateGOOboFile();
                    break;
                case "2":
                    filesUpdater.updateGafFile();
                    break;
                case "3":
                    filesUpdater.updateGOFiles();
                    break;
                default:
                    System.out.println("Unknown argument " + dValue);
                    break;
            }

        } else if (fileType.equalsIgnoreCase("HPO")) {

            filesUpdater = new DiNGOFilesUpdater("human");
            if (attribute.length() == 0) {
                attribute = "W";
            }
            if (hpSubOntology.contains(attribute)) {
                switch (dValue) {
                    case "1":
                        filesUpdater.updateHPOboFile(attribute);
                        break;
                    case "2":
                        filesUpdater.updateHPAnnotationFile(attribute);
                        break;
                    case "3":
                        filesUpdater.updateHPOFiles(attribute);
                        break;
                    default:
                        System.out.println("Unknown argument " + dValue);
                        break;
                }
                //filesUpdater.updateHPOFiles(attribute);

            } else {

                System.out.println("The following values for HPO subontologies are allowed:");
                System.out.println("1. 0 - phenotypic abnormality\n2. I - inheritance\n"
                        + "3. C - clinical course\n4. M - clinical modifier\n"
                        + "5. W - all subontologies");
                System.out.println("Unknown identifier: " + attribute);
            }

        } else if (fileType.equalsIgnoreCase("HUGO")) {

            filesUpdater = new DiNGOFilesUpdater("human");
            filesUpdater.updateHUGOFile();

        } else if (fileType.equalsIgnoreCase("UniProt")) {
            if (attribute.length() == 0) {
                attribute = "human";
            }
            filesUpdater = new DiNGOFilesUpdater(attribute);
            filesUpdater.updateUniProtMappingFile();

        } else if (fileType.equalsIgnoreCase("SwissProt")) {

            filesUpdater = new DiNGOFilesUpdater(attribute);
            filesUpdater.updateSwissProtFastaFile();

        } else {

            System.out.println("Available files to update:");
            System.out.println("1. GO\n2. HPO\n3. HUGO\n4. SwissProt\n5. UniProt");
            System.out.println("Unknown file type: " + fileType);

        }

    }
}
