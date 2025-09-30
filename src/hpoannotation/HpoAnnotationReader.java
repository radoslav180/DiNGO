package hpoannotation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static update.LocalFilesManager.convertStringToDate;

public class HpoAnnotationReader {

    private final String hpoAnnotationFile;
    private String formatVersion;
    private Date releaseDate;


    public HpoAnnotationReader(String hpoAnnotationFile) {
        this.hpoAnnotationFile = hpoAnnotationFile;
    }

    // initialize termToSubontology map by reading and parsing phenotype.hpoa file
    // compatibility with new hpoa format
    public Map<String, String> mapTermToSubontology()
    {
        Map<String, String> termToSubontology = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(hpoAnnotationFile))){

            String line;
            String[] fields;

            while((line = reader.readLine()) != null){

                if(line.startsWith("#") || line.startsWith("DatabaseID") || line.startsWith("database_id")){
                    if(line.startsWith("#version:") || line.startsWith(("#date:"))){

                        releaseDate = convertStringToDate(line.split(":")[1], "yyyy-MM-dd");
                    }
                    continue;
                }
                fields = line.split("\\t");
                termToSubontology.put(fields[3], fields[10]);//key = term, value = subontology of HPO

            }

        }catch(IOException ex){
            System.out.println(ex.getMessage());
            return termToSubontology;
        }

        return termToSubontology;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getHpoAnnotationFile() {
        return hpoAnnotationFile;
    }
}
