
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

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * <p>Class responsible for downloading GO Consortium files</p>
 * @author Radoslav Davidović
 * @version 1.01
 */
public class GOFileDownloader implements IFileDownload {
    private String fileAddress;//full path to net resource
    private String downloadFolder;//folder where file will be downloaded
    private String formatVersion;//format version of OBO file
    private Date releaseDate;//release date of GO ontology

    /**
     * <p>Constructor</p>
     * @param fileAddress full path to net resource
     * @param downloadFolder folder where GO file will be downloaded
     */
    public GOFileDownloader(String fileAddress, String downloadFolder){

        this.fileAddress = fileAddress;
        this.downloadFolder = downloadFolder;
    }

    /**
     * <p>Implementation of {@link IFileDownload} method</p>
     * <p>Downloads OBO (ontology file) or GAF(annotation file) depending on file extension. If the extension is .obo, ontology
     * file wil be downloaded, otherwise an annotation file</p>
     * @param fileName name of downloaded file
     */
    @Override
    public void downloadFile(String fileName){
        InputStream inStream;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            inStream = fileName.endsWith(".obo") ? new URL(fileAddress).openStream() : new GZIPInputStream(new URL(fileAddress).openStream());
            reader = new BufferedReader(new InputStreamReader(inStream));
            writer = new BufferedWriter(new FileWriter(downloadFolder + fileName));

            String line;
            while((line = reader.readLine()) != null){
                
                if(line.startsWith("!gaf-version:") || line.startsWith("format-version")){
                    formatVersion = line.split("\\s")[1];
                }
                
                if(line.startsWith("data-version:") || line.startsWith("!Date Generated by GOC:") || line.startsWith("!date-generated:")){
                    String strRelease = line.split(":")[1].trim();
                    // gaf 2.2 format
                    if(strRelease.contains("T")){
                        strRelease = strRelease.split("T")[0];
                    }
                       
                    if(strRelease.contains("/")){
                        strRelease = strRelease.split("/")[1];
                    }
                    try {
                        releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(strRelease);
                    } catch (ParseException ex) {
                        Logger.getLogger(GOFileDownloader.class.getName()).log(Level.INFO, "Unparsable date " + strRelease, "");
                    }
                }

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            Logger.getLogger(GOFileDownloader.class.getName()).log(Level.SEVERE, "File can not be found " + fileAddress, "");
        }
        finally{
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
            }
            if(writer != null){
                try{
                    writer.close();
                }catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    /**
     * <p>get info about format version</p>
     * @return format version
     */
    @Override
    public String getFormatVersion() {
        return formatVersion;
    }

    /**
     * <p>get info about release date</p>
     * @return release date
     */
    @Override
    public Date getReleaseDate() {
        return releaseDate;
    }
}

