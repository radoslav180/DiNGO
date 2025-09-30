package hpoannotation;

import java.io.*;
import java.util.Map;

public class HpoFlatAnnotationWriter {

    private final String annotationFileName;
    private String hpOntology;
    private final Map<String, String> termToSubontology;

    public HpoFlatAnnotationWriter(String hpOntology, Map<String, String> termToSubontology)
    {
        this.annotationFileName = "hp_annotation_" + hpOntology + ".txt";
        this.termToSubontology = termToSubontology;
        this.hpOntology = hpOntology;
    }

    public HpoFlatAnnotationWriter(HpoOntology hpoOntology, Map<String, String> termToSubontology){
        this.hpOntology = hpoOntology.toString().toLowerCase();
        this.annotationFileName = "hpo_" + hpOntology + ".txt";
        this.termToSubontology = termToSubontology;

    }

    private String getOntologySymbol()
    {
        return switch(hpOntology){
            //PHENOTYPIC_ABNORMALITY
            case "phenotypic_abnormality" -> "P";
            case "mode_of_inheritance" -> "I";
            case "clinical_course" -> "C";
            case "clinical_modifier" -> "M";
            default -> "W";
        };
    }

    private String header()
    {
        return String.format("(species=Homo Sapiens)(type=%s)(curator=HPO)", hpOntology);

    }

    //parses line from genes_to_phenotype.txt and extracts info
    //about gene and associated HPO term ID.
    //The input file has the following header :
    //#Format: entrez-gene-id<tab>entrez-gene-symbol<tab>HPO-Term-Name<tab>HPO-Term-ID<tab>Frequency-Raw<tab>Frequency-HPO<tab>Additional Info from G-D source<tab>G-D source<tab>disease-ID for link
    //Input line: 8192	CLPP	HP:0001250	Seizure	-	HP:0040283	-	mim2gene	OMIM:614129
    //Output line: CLPP=0001250
    private void writeLineToFile(String line, BufferedWriter writer) throws IOException
    {
        if(termToSubontology == null ||termToSubontology.isEmpty()){
            return;
        }

        String ontSymbol = getOntologySymbol();

        if (!line.startsWith("ncbi")) {//if line starts with ncbi or # that is header
            String[] tokens = line.split("\\t");
            if (termToSubontology.containsKey(tokens[2]) && (termToSubontology.get(tokens[2]).equals(ontSymbol)
                    || ontSymbol.equals("W"))) {
                if(!tokens[1].equals("-")){
                    writer.write(tokens[1] + "=" + tokens[2].split(":")[1]);
                    writer.newLine();
                }

            }

        } else {
            //instead of writing first line beginning with # write to file (species=Homo Sapiens)(type=Full)(curator=HPO)
            writer.write(header());
            writer.newLine();
        }

    }

    public void writerAnnotationFile(String geneToPhenotypeFile){
        try(BufferedReader reader = new BufferedReader(new FileReader(geneToPhenotypeFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(annotationFileName))){

            String line;

            while ((line = reader.readLine()) != null) {

                writeLineToFile(line, writer);
            }


        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

}
