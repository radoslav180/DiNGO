
/*
 * Copyright (c) 2019. Institute of Nuclear Sciences Vinča
 * Author: Radoslav Davidović
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
package update;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Class responsible for downloading HPO ontology and annotation files</p>
 * @author Radoslav Davidović
 */
public class HpoFileDownloader implements IFileDownload {

    private String annotationFileName;// name of downloaded file
    private String fileAddress;// http address
    private String hpOntology;// subontology of HPO
    private String downloadFolder;// folder where file will be downloaded
    private String formatVersion;// format version of OBO file
    private Date releaseDate;// release date of HPO ontology or annotation file

    // Map contains term ID as a key and HPO sub-ontology as a value
    private Map<String, String> termToSubontology = new HashMap<>();

    /**
     * <p>Constructor</p>
     * @param fileAddress hhtp address of file
     * @param downloadFolder folder where file will be placed after downloading
     * @param hpOntology HPO namespace
     */
    public HpoFileDownloader(String fileAddress, String downloadFolder, String hpOntology) {
        this.fileAddress = fileAddress;
        this.annotationFileName = "hp_annotation_" + hpOntology + ".txt";
        this.downloadFolder = downloadFolder;
        this.hpOntology = hpOntology;
    }

    private static boolean isHpoTerm(String id){

        return id.matches("^HP:\\d{7}$");
    }


    // initialize termToSubontology map by reading and parsing phenotype.hpoa file (https://hpo.jax.org/app/download/annotation)
    // compatibility with new hpoa format
    private void initTermToSubontology(String phenotypeAnnotationFileAddress) throws IOException {
        URL url = new URL(phenotypeAnnotationFileAddress);
        String fileName = url.getFile();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(phenotypeAnnotationFileAddress).openStream()))) {
            String line;
            String[] fields;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("#") || line.startsWith("DatabaseID")){
                    if(line.startsWith("#date:")){

                        releaseDate = LocalFilesManager.convertStringToDate(line.split(":")[1], "yyyy-MM-dd");
                    }
                    continue;
                }
                fields = line.split("\\t");
                // keep compatibility with previous version
                if(fields[10].equals("P")) fields[10] = "O";
                termToSubontology.put(fields[3], fields[10]);//key = term, value = subontology of HPO
            }
        }
    }
    
    //parses line from genes_to_phenotype.txt (https://hpo.jax.org/app/download/annotation) and extracts info
    //about gene and associated HPO term ID.
    //The input file has the following header : 
    //#Format: entrez-gene-id<tab>entrez-gene-symbol<tab>HPO-Term-Name<tab>HPO-Term-ID<tab>Frequency-Raw<tab>Frequency-HPO<tab>Additional Info from G-D source<tab>G-D source<tab>disease-ID for link
    //Input line: 8192	CLPP	HP:0001250	Seizure	-	HP:0040283	-	mim2gene	OMIM:614129
    //Output line: CLPP=0001250
    private void writeLineToFile(String line, String phenotypeAnnotationFileAddress, BufferedWriter writer) throws IOException {

        if (termToSubontology.isEmpty()) {
            initTermToSubontology(phenotypeAnnotationFileAddress);
        }

        if (!line.startsWith("#")) {//if line starts with # that is header
            String[] tokens = line.split("\\t");
            if (termToSubontology.containsKey(tokens[2]) && (termToSubontology.get(tokens[2]).equals(hpOntology)
                    || hpOntology.equals("W"))) {
                writer.write(tokens[1] + "=" + tokens[2].split(":")[1]);
                writer.newLine();
            }

        } else {
            //instead of writing first line beginning with # write to file (species=Homo Sapiens)(type=Full)(curator=HPO)
            switch (hpOntology) {
                case "O":
                    writer.write("(species=Homo Sapiens)(type=Phenotypic Abnormality)(curator=HPO)");
                    break;
                case "C":
                    writer.write("(species=Homo Sapiens)(type=Clinical Course)(curator=HPO)");
                    break;
                case "I":
                    writer.write("(species=Homo Sapiens)(type=Mode Of Inheritance)(curator=HPO)");
                    break;
                case "M":
                    writer.write("(species=Homo Sapiens)(type=Clinical Modifier)(curator=HPO)");
                    break;
                case "W":
                    writer.write("(species=Homo Sapiens)(type=Full)(curator=HPO)"); //all sub-ontologies
                    break;
                default:
                    writer.write("HPO subontology has not been specified. Choose between: C, I, M, O and W");
                    break;
            }
            writer.newLine();
        }

    }

    /**
     * <p>
     * Downloads HPO ontology or annotation file</p>
     *
     * @param fileName name of the file
     */
    @Override
    public void downloadFile(String fileName) {

        String outFile = fileName.endsWith(".obo") ? fileName : annotationFileName;
        boolean isAnnotationFile = !fileName.endsWith(".obo");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(fileAddress).openStream()));
                BufferedWriter writer = new BufferedWriter(new FileWriter(downloadFolder + outFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                //works only for hp.obo file
                if (line.startsWith("format-version")) {
                    formatVersion = line;
                }
                //works only for hp.obo file
                if (line.startsWith("data-version:")) {
                    String[] tokens = line.split("/");
                    releaseDate = LocalFilesManager.convertStringToDate(line.split(":")[1], "yyyy-MM-dd");
                }
                if(isAnnotationFile){
                    writeLineToFile(line, fileName, writer);
                }else{
                    writer.write(line);
                    writer.newLine();
                }

            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public String getFileAddress() {
        return fileAddress;
    }

    public void setFileAddress(String fileAddress) {
        this.fileAddress = fileAddress;
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

    @Override
    public String getFormatVersion() {
        return formatVersion;
    }

    public void setDownloadFolder(String downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

    public String getHpOntology() {
        return hpOntology;
    }

    public void setHpOntology(String hpOntology) {
        this.hpOntology = hpOntology;
    }

    public String getAnnotationFileName() {
        return annotationFileName;
    }

    @Override
    public Date getReleaseDate() {
        return releaseDate;
    }

}
