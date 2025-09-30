package hpoannotation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HpoFlatAnnotationCreator {

    //list containing labels for HPO subontologies
    //P = phenotypic abnormality
    //C = clinical course
    //I = mode of inheritance
    //M = clinical modifier
    //W = all subontologies

    private static HpoOntology stringToEnum(String hpoOntology)
    {

        return switch (hpoOntology){
            case "P" -> HpoOntology.PHENOTYPIC_ABNORMALITY;
            case "C" -> HpoOntology.CLINICAL_COURSE;
            case "I" -> HpoOntology.MODE_OF_INHERITANCE;
            case "M" -> HpoOntology.CLINICAL_MODIFIER;
            case "W" -> HpoOntology.ALL_SUBONTOLOGIES;
            default -> throw new IllegalArgumentException("Unknown hpo subontology");
        };

    }

    private static boolean checkIfElementExists(String[] arr, String element){

        if(arr == null || arr.length == 0){
            return false;
        }

        return Arrays.asList(arr).contains(element);
    }

    public static void main(String[] args) {

        if(args.length < 3 || checkIfElementExists(args, "-h")){
            System.out.println("Usage:\n");
            System.out.println("-a <hpo annotation file> -g <genes to phenotypes file> -s <hpo subontology> \n");
            System.out.println("Options:\n");
            System.out.println("-a, hpo annotation file in hpoa format");
            System.out.println("-g, genes_to_disease.txt file");
            System.out.println("""
                    -s, hpo subontology.\s
                    Possible values:
                    1. P = phenotypic abnormality
                    2. C = clinical course
                    3. I = mode of inheritance
                    4. M = clinical modifier
                    5. W = all subontologies (default value)""");
            return;
        }

        String annotationFile = "";
        String genesToPhenotypesFile = "";
        String subontology = "W";

        for(int i = 0; i < args.length - 1; i++){
            if(args[i].equals("-a")){
                annotationFile = args[i+1];
            }
            else if(args[i].equals("-g")){
                genesToPhenotypesFile = args[i+1];
            }
            else if(args[i].equals("-s")){
                subontology = args[i + 1];
            }
        }

        File aFile = new File(annotationFile);

        if(!aFile.exists()){
            System.out.println("Can't find file " + aFile);
            return;
        }

        File gFile = new File(genesToPhenotypesFile);

        if(!gFile.exists()){
            System.out.println("Can't find file " + aFile);
            return;
        }

        HpoOntology hpoOntology;

        try{
            hpoOntology = stringToEnum(subontology);
        }catch(IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            return;
        }

        HpoAnnotationReader annotationReader = new HpoAnnotationReader(annotationFile);

        Map<String, String> termsMap = annotationReader.mapTermToSubontology();

        HpoFlatAnnotationWriter annotationWriter = new HpoFlatAnnotationWriter(hpoOntology, termsMap);

        annotationWriter.writerAnnotationFile(genesToPhenotypesFile);


    }
}
