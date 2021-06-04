package update;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * <p>Class containing methods for retrieving info about DiNGO's local files</p>
 */
public class LocalFilesManager{
    /**
     * <p>Directory containing local files</p>
     */
    private String directory;
    /**
     * <p>Name of log file</p>
     */
    private String logFile;

    /**
     * <p>Constructor</p>
     * @param directory path to local files
     * @param logFile name of log file
     */
    public LocalFilesManager(String directory, String logFile){
        this.directory = directory;
        this.logFile = logFile;
    }

    /**
     * <p>Returns all files located in {@link #directory}</p>
     * @return String array containing names of all files
     */
    public String[] getAllFilesInDirectory(){
        File dir = new File(directory);
        String[] dirFiles = dir.list();
        return dirFiles;
    }

    /**
     * <p>Method extracts release date of ontology obo file from the file header</p>
     * @param pathToObo path to obo file
     * @return date of release
     */
    public static Date extractOboReleaseDate(String pathToObo){
        String timeStr = "";
        try(BufferedReader reader = new BufferedReader(new FileReader(pathToObo))){
            String line;

            while((line = reader.readLine()) != null){

                if(line.startsWith("data-version")){
                    timeStr = line.split("/")[1];
                    break;
                }
                if(line.startsWith("!Generated: ")){
                    timeStr = line.split("\\s+")[1];
                    break;
                }

            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }

        Date releaseDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");//parse, for example, 2016-09-03 string to date
        try {
            releaseDate = formatter.parse(timeStr);
        } catch (ParseException e) {
            return null;

        }
        return releaseDate;
    }
    /**
     * <p>Writes down name of file and associated last modification date downloaded from a server to log file. Record has 
     * the following form file=date</p>
     * @param lastModDate last modification date (server)
     * @param fileName name of file downloaded from a server
     * @return boolean value. If operation is successful returns true, otherwise false
     */
    public boolean saveLastModTimeToFile(Date lastModDate,  String fileName){
        try(PrintWriter writer = new PrintWriter(new FileOutputStream(new File(logFile), true))){
            writer.println(fileName + "=" + lastModDate);
        }catch(IOException ex){
            return false;
        }
        return true;
    }

    /**
     * <p>Retrieves date for specified file</p>
     * @param fileName name of file
     * @return date associated with file designated as <code>fileName</code>
     */
    public Date retrieveLastModDateFromFile(String fileName){
        System.out.println("File name: " + fileName);
        Date lastModTime = null;
        List<String> entries = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(logFile))){
            String line;
            while((line = reader.readLine()) != null){
                if(line.contains(fileName)){
                   
                    entries.add(line.split("=")[1]);
                }
            }
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }

        if(!entries.isEmpty()){
            String reprDate = entries.get(entries.size() - 1);

            try {
               lastModTime = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy").parse(reprDate);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
        }

        return lastModTime;
    }
    
    /**
     * <p>
     * Method that renames file. It is OK for small files. There is need for such method
     * because Java native method {@link java.io.File#renameTo(java.io.File)} is not reliable enough.
     * </p>
     * @param oldName current file name
     * @param newName new file name
     * @return true if renaming was successful
     */
    public static boolean renameFile(String oldName, String newName){
        boolean bRet = true;
        try(BufferedReader reader = new BufferedReader(new FileReader(oldName));
                BufferedWriter writer = new BufferedWriter(new FileWriter(newName))){
            String line;
            while((line = reader.readLine()) != null){
                writer.write(line);
                writer.newLine();
            }
            
            File oldFile = new File(oldName);
            boolean bValue = oldFile.delete();
            if(bValue){
                System.out.println("File" + oldName + " renamed successfully to"
                        + newName + "!");
            } else{
                System.out.println("Cannot rename file " + oldName);
                bRet = false;
            }
            
        } catch(IOException ex){
            bRet =  false;
        }
        
        return bRet;
    }
    
}
