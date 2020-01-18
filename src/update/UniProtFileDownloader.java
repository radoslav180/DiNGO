
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.net.ftp.FTPClient;

/**
 * <p>
 * Class responsible for downloading UniProt files from FTP server</p>
 *
 * @author Radoslav Davidović
 */
public class UniProtFileDownloader implements IFileDownload {

    /**
     * <p>
     * FTP address</p>
     */
    private String ftpAddress;
    /**
     * <p>
     * path to folder containing desired file</p>
     */
    private String folderOnServer;
    /**
     * <p>
     * Format version of file</p>
     */
    private String formatVersion;
    /**
     * <p>
     * Folder where file is downloaded</p>
     */
    private String downloadFolder;
    /**
     * <p>
     * Release date of file</p>
     */
    private Date releaseDate;
    /**
     * <p>
     * Instance of {@link FtpConnection} class</p>
     */
    private FtpConnection connection;

    /**
     *
     * @param connection instance of {@link FtpConnection}
     * @param ftpAddress FTP address
     * @param folderOnServer  path to folder containing desired file
     * @param downloadFolder folder where file is downloaded
     */
    public UniProtFileDownloader(FtpConnection connection, String ftpAddress,
                                 String folderOnServer, String downloadFolder){
    
        this.connection = connection;
        this.ftpAddress = ftpAddress;
        this.folderOnServer = folderOnServer;
        this.downloadFolder = downloadFolder;
    }

    private static Date convertStringToDate(String strDate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = null;
        try {
            date = formatter.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //unzip annotation file downloaded from GO Consortium repo
    private void unzipGoAnnotation(String gzipFile, String outFile) {
        System.out.println("Unzipping file " + gzipFile);

        UnGZipFile unGZipFile = new UnGZipFile(gzipFile, outFile);
        unGZipFile.unzipFile();
        //delete gz file after file is extracted
        boolean deleteFile = new File(gzipFile).delete();
        if (deleteFile) {
            System.out.println(gzipFile + " has been deleted!");
        } else {
            System.out.println("Can't delete " + gzipFile + " !");
        }
    }


    /**
     * <p>
     * Implementation of {@link IFileDownload} method</p>
     *
     * @param fileName name of downloaded file
     */
    @Override
    public void downloadFile(String fileName) {
        FTPClient ftpClient = connection.getFTPClient();
        connection.setFtpAddress(ftpAddress);

        try {
            connection.connectAndLogin();

            boolean isDirExists = ftpClient.changeWorkingDirectory(folderOnServer);

            //if directory does not exist stop further execution
            if (!isDirExists) {
                System.out.println("Folder " + folderOnServer + " has not been found!");
                return;
            }

            String downloadedFile = downloadFolder + fileName;
            OutputStream output = new BufferedOutputStream(new FileOutputStream(new File(downloadedFile)));
            boolean success = ftpClient.retrieveFile(fileName, output);

            String strRelease = ftpClient.getModificationTime(fileName);
            releaseDate = convertStringToDate(strRelease);
            //System.out.println(releaseDate);

            if (success) {
                System.out.println("File " + fileName + " has been downloaded successfully!");
            } else {
                System.out.println("File downloading has been failed!");
                return;
            }

            output.close();
            //annotation file is gzipped
            if (fileName.endsWith(".gz")) {
                unzipGoAnnotation(downloadedFile, downloadedFile.substring(0, downloadedFile.lastIndexOf(".")));
            }
            ftpClient.changeWorkingDirectory("/");//back to root directory
            connection.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(UniProtFileDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //class containing method for unzipping gzip files
    private class UnGZipFile {

        private final String gzipFile;//name of zipped file
        private final String txtFile;//name of output file

        private UnGZipFile(String gzipFile, String txtFile) {
            this.gzipFile = gzipFile;
            this.txtFile = txtFile;
        }

        //unzip file
        private void unzipFile() {

            try {

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gzipFile))));
                        BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                System.out.println("");
                System.out.println("Done!");

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    public String getFormatVersion() {
        return "";
    }

    @Override
    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getFtpAddress() {
        return ftpAddress;
    }

    public String getFolderOnServer() {
        return folderOnServer;
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

}
