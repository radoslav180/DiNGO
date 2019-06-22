
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Class responsible for downloading HPO ontology and annotation files</p>
 *
 * @author Radoslav Davidović
 */
public class HpoFileDownloader implements IFileDownload {

    private String annotationFileName;//name of downloaded file
    private String fileAddress;//http address 
    private String hpOntology;//subontology of HPO
    private String downloadFolder;//folder where file will be downloaded
    private String formatVersion;//format version of OBO file
    private Date oboReleaseDate;//release date of HPO ontology

    //Map contains term ID as a key and HPO sub-ontology as a value
    private Map<String, String> termToSubontology = new HashMap<>();

    public HpoFileDownloader(String fileAddress, String downloadFolder, String hpOntology) {
        this.fileAddress = fileAddress;
        this.annotationFileName = "hp_annotation_" + hpOntology + ".txt";
        this.downloadFolder = downloadFolder;
        this.hpOntology = hpOntology;
    }

    //initialize termToSubontology map by reading and parsing phenotype_annotation.tab file (https://hpo.jax.org/app/download/annotation)
    private void initTermToSubontology(String phenotypeAnnotationFileAddress) throws IOException {
        //final String address = configurator.getHpPhenotypeAnnotationAddress();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(phenotypeAnnotationFileAddress).openStream()))) {
            String line;
            String[] fields;
            while ((line = reader.readLine()) != null) {
                fields = line.split("\\t");
                termToSubontology.put(fields[4], fields[10]);//key = term, value = subontology of HPO
            }
        }
    }

    private void writeLineToFile(String line, String phenotypeAnnotationFileAddress, BufferedWriter writer) throws IOException {

        if (termToSubontology.isEmpty()) {
            initTermToSubontology(phenotypeAnnotationFileAddress);
        }

        if (!line.startsWith("#")) {//first line starts with # so skip it
            String[] tokens = line.split("\\t");
            if (termToSubontology.containsKey(tokens[3]) && (termToSubontology.get(tokens[3]).equals(hpOntology)
                    || hpOntology.equals("W"))) {
                writer.write(tokens[1] + "=" + tokens[3].split(":")[1]);
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(fileAddress).openStream()));
                BufferedWriter writer = new BufferedWriter(new FileWriter(downloadFolder + fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                //works only for hp.obo file
                if (line.startsWith("format-version")) {
                    formatVersion = line;
                }
                //works only for hp.obo file
                if (line.startsWith("data-version:")) {
                    String[] tokens = line.split("/");
                    String strRelease = tokens[tokens.length - 1].trim();
                    try {
                        oboReleaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(strRelease);
                    } catch (ParseException ex) {
                        Logger.getLogger(HpoFileDownloader.class.getName()).log(Level.INFO, "Unparsable date " + strRelease, "");
                    }
                }

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    /**
     * <p>
     * Generates annotation file that can be read by DiNGO or BiNGO. The first
     * line is header containing info about species, sub-ontology and
     * curator:<br>
     * (species=Homo Sapiens)(type=Mode Of Inheritance)(curator=HPO)<br>
     * Other lines contain gene/protein identifier and associated term separated
     * by equals sign: <br>
     * CLPP=0000007 <br>
     * A2M=0000006 <br>
     * MKKS=0000007 <br>
     * GDF5=0000006<br>
     * GDF5=0000007 <br>
     * TSR2=0001419 <br>
     * AARS=0000006 <br>
     * AARS=0000007<br>
     * </p>
     *
     * @param phenotypeAnnotationFileAddress phenotype_annotation.tab file 
     * <a href="https://hpo.jax.org/app/download/annotation">
     * https://hpo.jax.org/app/download/annotation</a>
     */
    public void generateHpoAnnotationFile(String phenotypeAnnotationFileAddress) {
        //String fileName = "hp_annotation_" + hpOntology + ".txt";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(fileAddress).openStream()));
                BufferedWriter writer = new BufferedWriter(new FileWriter(downloadFolder + annotationFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writeLineToFile(line, phenotypeAnnotationFileAddress, writer);
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
        return oboReleaseDate;
    }

}
