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
package uniprot;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Class contains methods responsible for parsing UniProt id mapping files</p>
 */
public class UniprotMappingParser {
    /**
     * <p>Map that contains manually reviewed UniProt entries (SwissProt)</p>
     */
    private final Map<String, Integer> reviewedEntries = new HashMap<>();
    /**
     * <p>Name of UniProt file that contains IDs (dat file)</p>
     */
    private final String mappingFileName;
    /**
     * <p>if true only SwissProt entries will be taken into consideration</p>
     */
    private boolean isOnlyReviewed = true;
    
    /**
     * <p>Constructor</p>
     * @param mappingFileName Name of UniProt file that contains IDs
     */
    public UniprotMappingParser(String mappingFileName) {
        this.mappingFileName = mappingFileName;
    }

    //map array elements to their indexes
    private static Map<String, Integer> mapArrayElementToIndex(List<String> arr) {
        Map<String, Integer> elementToIndex = new HashMap<>();
        int len = arr.size();
        for (int i = 0; i < len; i++) {
            elementToIndex.put(arr.get(i), i);
        }

        return elementToIndex;
    }

    //map all set elements to specified boolean value
    private static <T> Map<T, Boolean> mapSetElementToBoolean(Set<T> elements, boolean bValue) {
        Map<T, Boolean> map = new HashMap<>();
        for (T el : elements) {
            map.put(el, bValue);
        }

        return map;
    }
    
    //check if file exists
    private static boolean isFileExists(String fileName) {
        File file = new File(fileName);
        if (!file.isFile()) {
            return false;
        }
        return file.exists();
    }
    //display help on screen
    private static void displayHelp(){
        System.out.println("Description:");
        System.out.println("UniprotMappingParser is a simple tool that converts one set of identifiers to another one.\n"
                + "The tool is inspired by UniProt mapping tool (https://www.uniprot.org/uploadlists/) .\n");
        System.out.println("Usage:");
        System.out.println("java -cp DiNGO.jar uniprot.UniprotMappingParser -i <iFile> -f <from> -t <to> -m <uniprot mapping file> [-s swiss-prot file] [-n]\n");
        System.out.println("Options:");
        System.out.printf("%-30s%s%n", "    -i <iFile>", "Input file name containing list of IDs\n");
        System.out.printf("%-30s%s%n", "    -f <from>", "input ID\n");
        System.out.printf("%-30s%s%n", "    -t <to>", "output ID\n");
        System.out.printf("%-30s%s%n", "    -m <uniprot mapping file>", "UniProt idmapping.dat file\n");
        System.out.printf("%-30s%s%n", "    -s swiss-prot file", "uniprot.sprot.fasta file\n");
        System.out.printf("%-30s%s%n", "    -n", "uses non-reviewed UniProt entries\n");
        
        System.out.println("An example: ");
        System.out.println("java -cp Dingo.jar uniprot.UniprotMappingParser -i input.txt -t "
                + "UniProtKB -f Gene_Name -m HUMAN_9606_idmapping.dat -s uniprot_sprot.fasta\n");
        System.out.println("HUMAN_9606_idmapping.dat and uniprot_sprot.fasta files"
                + " can be downloaded from ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/" + "\n");
        System.out.println("Description of IDs:\n");
        System.out.printf("%-20s%s%n", "ID", "Description");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-20s%s%n", "UniProtKB", "UniProt identification (Q8IZP0 and P30457)");
        System.out.printf("%-20s%s%n", "UniProtKB-ID", "UniProt identification (1433B_HUMAN)");
        System.out.printf("%-20s%s%n", "Gene_Name", "Gene symbol (PTEN or CDKN2A)");
        System.out.printf("%-20s%s%n", "GeneID", "Entrez identification (7531)");
        System.out.printf("%-20s%s%n", "Ensembl", "Ensembl gene identification (ENSG00000108953)");
        System.out.printf("%-20s%s%n", "Ensembl_TRS", "Ensembl transcript identification (ENST00000264335)");
        System.out.printf("%-20s%s%n", "Ensembl_PRO", "Ensembl protein identification (ENSP00000264335)");
        System.out.printf("%-20s%s%n", "UniGene", "UniGene identification (Hs.643544)");
        System.out.println("-------------------------------------------------------------------------------\n");
        System.out.println("For full description look at ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/README");
    }
    
    //exctracts reviewed UniProt entries from uniprot_sprot.fasta file!
    public boolean extractCuratedUniProtEntries(String swissProtFile) {

        try (BufferedReader reader = new BufferedReader(new FileReader(swissProtFile))) {
            String line;
            String uniProtKB;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(">sp")) {
                    continue;
                }
                int first = line.indexOf("|");
                int last = line.lastIndexOf("|");
                uniProtKB = line.substring(first + 1, last);
                reviewedEntries.put(uniProtKB, 1);
            }
        } catch (IOException ex) {
            Logger.getLogger(UniprotMappingParser.class.getName()).log(
                    Level.INFO, swissProtFile + " not found!", "Results will "
                    + "include TrEMBL and Swiss-Prot entries");
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Generate TAB delimited file which first column is UniProtKB and other
     * columns correspond to user input</p>
     *
     * @param outFile name of output file
     * @param ids array of IDs that correspond to UniProtKB ID
     */
    public void mapSelectedIDsInTabFormat(String outFile, String... ids) {

        try (BufferedReader reader = new BufferedReader(new FileReader(mappingFileName));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {

            if (ids == null || ids.length == 0) {
                System.out.println("Please provide list of IDs!");
                return;
            }

            String line;
            String id;
            String protein = "";
            boolean isFirst = true;
            List<String> targets = Arrays.asList(ids);
            Map<String, Integer> map = mapArrayElementToIndex(targets);
            String[] hits = new String[ids.length];
            //StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                int fPos = line.indexOf("\t");//first tab
                int lPos = line.lastIndexOf("\t");//second tab

                if (line.substring(0, fPos).contains("-")) {
                    continue;//avoid isoforms
                }
                //when next protein is encountered write to the file
                if (!protein.equals("") && !protein.equals(line.substring(0, fPos))) {
                    //writer.write(protein + "\t" + builder.toString().trim());
                    writer.write(protein);
                    for (String e : hits) {
                        if (e != null) {
                            writer.write(e);
                        } else {
                            writer.write("");
                        }
                    }
                    writer.newLine();
                    //builder.delete(0, builder.length());
                    hits = new String[ids.length];
                    protein = line.substring(0, fPos);
                }

                if (isFirst) {
                    protein = line.substring(0, fPos);
                    isFirst = false;
                }

                id = line.substring(fPos + 1, lPos);//tell us what kind of id in the third column is

                if (targets.contains(id)) {
                    String element = hits[map.get(id)];
                    if (element == null) {
                        hits[map.get(id)] = line.substring(lPos);
                    } else {

                        hits[map.get(id)] = element.substring(0, element.length() - 1) + ", "
                                + line.substring(lPos).substring(1);
                    }
                    //builder.append(line.substring(lPos));
                }

            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * <p>
     * Method that maps one set of IDs to another one (from -> to) and writes to
     * file</p>
     *
     * @param inputIDs input identifications
     * @param from identification to be mapped
     * @param to target identification
     * @param swissProtFile file containing only reviewed UniProt entries
     */
    public void mappingIDs(Set<String> inputIDs, String from, String to, String swissProtFile) {
        //check input
        if (inputIDs == null || inputIDs.isEmpty()) {
            System.out.println("Please provide list of IDs!");
            return;
        }

        if (!from.equals("UniProtKB") && !to.equals("UniProtKB")) {
            if (!from.equals("UniProtKB-ID") && !to.equals("UniProtKB-ID")) {
                System.out.println("Can not do mapping from " + from + " to " + to);
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mappingFileName));
                BufferedWriter writer = new BufferedWriter(new FileWriter("mapping.tab"))) {
            //key UniProtKB value UniProtKB AC/ID
            Map<String, String> uniToUniID = new HashMap<>();
            //tracks unmapped IDs
            Map<String, Boolean> map = mapSetElementToBoolean(inputIDs, false);
            String line;
            String id = "";
            //String previousID;
            boolean bValue = extractCuratedUniProtEntries(swissProtFile);
            while ((line = reader.readLine()) != null) {
                //previousID = id;
                int fPos = line.indexOf("\t");//first tab
                int lPos = line.lastIndexOf("\t");//second tab
                id = line.substring(fPos + 1, lPos);//second column contains info about ID in the third column
                //solves the issue with gene synonyms
                if (id.equals("Gene_Synonym")) {
                    id = "Gene_Name";
                }
                String firstColumn = line.substring(0, fPos);//UniProtKB
                String thirdColumn = line.substring(lPos + 1);//corresponding ID
                if (isOnlyReviewed && bValue) {
                    if (!reviewedEntries.containsKey(firstColumn)) {
                        continue;
                    }
                }
                if (from.equals("UniProtKB")) {

                    if (!id.equals(to)) {

                        continue;
                    }

                    if (inputIDs.contains(firstColumn)) {

                        writer.write(firstColumn + line.substring(lPos));
                        writer.newLine();
                        map.put(firstColumn, true);
                    }
                } else if (from.equals("UniProtKB-ID")) {
                    //we can not map other IDs to UniProtKB-ID directly, but we must translate UniProtKB-ID to UniProtKB
                    if (id.equals("UniProtKB-ID")) {

                        uniToUniID.put(firstColumn, thirdColumn);

                    }

                    if (id.equals(to)) {

                        if (inputIDs.contains(uniToUniID.get(firstColumn))) {

                            writer.write(uniToUniID.get(firstColumn) + "\t" + thirdColumn);
                            writer.newLine();
                            map.put(uniToUniID.get(firstColumn), true);
                        }

                    }

                    if (to.equals("UniProtKB")) {

                        if (inputIDs.contains(line.substring(lPos + 1))) {

                            writer.write(thirdColumn + "\t" + firstColumn);
                            writer.newLine();
                            map.put(thirdColumn, true);
                        }

                    }
                } else {
                    if (to.equalsIgnoreCase("UniProtKB-ID")) {
                        if (id.equals("UniProtKB-ID")) {
                            uniToUniID.put(firstColumn, thirdColumn);
                        }
                    }

                    if (!id.equals(from)) {

                        continue;
                    }

                    if (inputIDs.contains(thirdColumn)) {
                        if (to.equals("UniProtKB-ID")) {
                            writer.write(thirdColumn + "\t" + uniToUniID.get(firstColumn));
                        } else {
                            writer.write(thirdColumn + "\t" + firstColumn);
                        }
                        writer.newLine();
                        map.put(thirdColumn, true);
                    }

                }

            }
            System.out.println("\nResults have been saved to mapping.tab file!\n");
            System.out.println("Not mapped entries: ");
            
            for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                if (!entry.getValue()) {
                    System.out.println(entry.getKey());
                }
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    /**
     * <p>Getter method</p>
     * @return {@link #isOnlyReviewed}
     */
    public boolean isIsOnlyReviewed() {
        return isOnlyReviewed;
    }
    
    /**
     * <p>Setter method</p>
     * @param isOnlyReviewed boolean value
     */
    public void setIsOnlyReviewed(boolean isOnlyReviewed) {
        this.isOnlyReviewed = isOnlyReviewed;
    }
    
    //main method
    public static void main(String[] args) {
        
        if (args.length < 4) {
            displayHelp();
            return;
        }

        String fileName = "", uniprotMapingFile = "", from = "", to = "",
                swissProt = "";
        boolean bReviewed = true;
        int ln = args.length;
        for (int i = 0; i < ln; i++) {
            if (args[i].equals("-i")) {
                if (i + 1 < ln) {
                    fileName = args[i + 1];
                }
            } else if (args[i].equals("-f")) {
                if (i + 1 < ln) {
                    from = args[i + 1];
                }
            } else if (args[i].equals("-t")) {
                if (i + 1 < ln) {
                    to = args[i + 1];
                }
            } else if (args[i].equals("-m")) {
                if (i + 1 < ln) {
                    uniprotMapingFile = args[i + 1];
                }
            } else if (args[i].equals("-s")) {
                if (i + 1 < ln) {
                    swissProt = args[i + 1];
                }
            } else if (args[i].equals("-n")) {
                bReviewed = false;
            }
        }

        if (!isFileExists(fileName)) {
            System.out.println("File " + fileName + " has not been found!");
            return;
        }

        if (!isFileExists(uniprotMapingFile)) {
            System.out.println("Mapping file " + uniprotMapingFile + " does not exist!");
            return;
        }

        Set<String> input = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                input.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        UniprotMappingParser test = new UniprotMappingParser(uniprotMapingFile);

        test.setIsOnlyReviewed(bReviewed);
        test.mappingIDs(input, from, to, swissProt);
    }
}
