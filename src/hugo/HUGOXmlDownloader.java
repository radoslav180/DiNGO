/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hugo;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
/**
 *<p>Class responsible for downloading HUGO mapping file</p>
 * @author Radoslav Davidović
 */
public class HUGOXmlDownloader {

    /**
     * <p>
     * HTTP address of XML file</p>
     */
    private String xmlFileAddress;
    /**
     * <p>
     * Name of output file</p>
     */
    private String fileName;

    /**
     * <p>
     * Constructor</p>
     *
     * @param xmlFileAddress HTTP address of file
     */
    public HUGOXmlDownloader(String xmlFileAddress) {
        this.xmlFileAddress = xmlFileAddress;
        this.fileName = "hugo_mapping_id.xml";//default value
    }

    /**
     * <p>
     * Constructor</p>
     *
     * @param xmlFileAddress HTTP address of file
     * @param fileName name of output file
     */
    public HUGOXmlDownloader(String xmlFileAddress, String fileName) {
        this(xmlFileAddress);
        this.fileName = fileName;
    }

    /**
     * <p>
     * Method that downloads XML file from HUGO</p>
     *
     * @return true if download was successful, otherwise false
     */
    public boolean downloadHUGOFile() {
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader
        (new URL(xmlFileAddress).openStream()));
                OutputStreamWriter writer = new OutputStreamWriter
        (new FileOutputStream(fileName), "UTF8")) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
   
            System.out.println("\nDownload has been completed!");
        } catch (IOException ex) {
            System.out.println("Download failed!");
            return false;
        }
        return true;
    }
    
    /**
     * <p>Getter method</p>
     * @return {@link #fileName}
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * <p>Setter method</p>
     * @param fileName new file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
