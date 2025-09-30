
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

    private String fileAddress;// http address
    private String downloadFolder;// folder where file will be downloaded
    private String formatVersion;// format version of OBO file
    private Date releaseDate;// release date of HPO ontology or annotation file

    /**
     * <p>Constructor</p>
     * @param fileAddress hhtp address of file
     * @param downloadFolder folder where file will be placed after downloading
     */
    public HpoFileDownloader(String fileAddress, String downloadFolder) {
        this.fileAddress = fileAddress;
        this.downloadFolder = downloadFolder;
    }

    private static boolean isHpoTerm(String id){

        return id.matches("^HP:\\d{7}$");
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

                if (line.startsWith("format-version")) {
                    formatVersion = line;
                }

                if (line.startsWith("data-version:")) {
                    String[] tokens = line.split("/");
                    releaseDate = LocalFilesManager.convertStringToDate(line.split(":")[1], "yyyy-MM-dd");
                }

                writer.write(line);
                writer.newLine();

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

    @Override
    public Date getReleaseDate() {
        return releaseDate;
    }

}
