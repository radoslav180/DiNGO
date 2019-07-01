# DiNGO Manual

## Table of Contents
## 1. Introduction
## 2. Installation
## 3. Requirements
## 4. Usage
   - ## 4.1. First run
   - ## 4.2. Command line options
   - ## 4.3. Batch mode
   - ## 4.4. Supported species
## 5. Modules
   - ## 5.1. Update module
        - ## 5.1.1. GO files
        - ## 5.1.2. HPO files
        - ## 5.1.3. Uniprot mapping files
        - ## 5.1.4. SwissProt file
        - ## 5.1.5. HUGO file
        - ## 5.1.6. Usage
   - ## 5.2. Mapping module
        - ## 5.2.1. Usage
   - ## 5.3. Propagation module
## 6. Dependencies
## 7. References

## 1. Introduction
DiNGO is a standalone application based on open source code from BiNGO [1] a Java based tool aimed to determine which Gene Ontology (GO) categories are overrepresented in a set of genes. DiNGO is a command line application which is able to do GO and HPO term enrichment on a set of genes or proteins. Also, there are additional modules that brings new functionalities to DiNGO.
## 2. Installation
Download [DiNGO.zip](https://www.vin.bg.ac.rs/180/tools/DiNGO.php) file and extract contents. There should be DiNGO.jar file and folder configuration containing conf.properties file. The file defines parameters necessary for program execution.
## 3. Requirements
Java Runtime Enviroment (JRE) 1.8

## 4. Usage

To run DiNGO open terminal (Command prompt in Windows) in folder where jar file is located and type following:

`java -jar Dingo.jar -h`

If everything is ok help text should appear.

### 4.1. First run

When DiNGO is run for the first time it will create three folders: annotations, download and mapping. The first folder is intended to contain default ontology and annotation files. The files can be downloaded and updated by using update module (see Update Module section). Downloaded files are temporally placed in download folder. The third folder, mapping, is location where DiNGO downloads UniProt, HUGO and SwissProt files (see Update Module section).

### 4.2. Command line options

There are several options that should be defined in order to do enrichment analysis. The -o option defines name of the output file that contains results. The -i option defines name of file containing list of gene/proteins (one gene/protein per line). It is mandatory to define namespace (subontology) for which enrichment analysis will be run. For example -ns BP means that only terms belonging to biological process subontology will be taken into consideration. In order to specify species it is necessary to use -s option. Basically, it is possible to run GO enrichment analysis typing following:

`java -jar Dingo.jar -o results -i list_of_genes -ns BP -s Homo_sapiens`

If HPO term enrichment analysis is demanded then usage of option -e is required

`java -jar DiNGO.jar -o results -i list_of_genes -ns O -s Homo_sapiens -e HPO`

Note that in the above case using -s is optional.
Under default settings DiNGO uses Hypergeometric test, but user may switch to Binomial test by using -st option. The following example uses Binomial test:

`java -jar Dingo.jar -o results -i list_of_genes -ns BP -s Homo_sapiens -st 2`

If not specified DiNGO uses whole annotation as a reference set. This can be changed by passing name of the file containing reference set (-rs option). 

User can specify ontology and annotation files by using -of and -af options, respectively. DiNGO supports OBO format for ontology and annotation files in GAF format. Additionally, DiNGO may use custom ontology and annotation files created by user. Under default DiNGO uses default ontology and annotation files. In the case, these files are not in annotations folder, DiNGO will try to download them first and then to do enrichment analysis.

It is possible to exclude some annotation entries from the analysis by defining evidence codes that will not be taken into consideration. The following example does not use Electronic Annotation (IEA) and Traceable Author Statement (TAS) evidence codes:

`java -jar Dingo.jar -o results -i list_of_genes.txt -ns BP -s human -dc IEA:TAS`

Note that the codes are separated by colon. Detailed description of evidence codes can be found at [http://geneontology.org/docs/guide-go-evidence-codes/](http://geneontology.org/docs/guide-go-evidence-codes/). 

Common issue while doing enrichment analysis is use of unsupported gene/protein identifications (IDs). DiNGO supports ID only if ID is contained in annotation file. To overcome this issue, user can specify TAB delimited file which contains in one column supported IDs and in other columns appropriate unsupported IDs (-m option). 

### 4.3. Batch mode

DiNGO batch mode works same as [BiNGO](https://www.psb.ugent.be/cbd/papers/BiNGO/User_Guide.html) batch mode. Firstly, clusters in the input file are separated by keyword batch. Secondly, when defining -o option it is necessary to use `batch`. The following example run DiNGO in batch mode:

`java -jar Dingo.jar -o batch -i input_file -ns BP -s human`

It is important to say that DiNGO is multi-threaded application. So, when batch mode is used, it is possible to speed up processing time by defining number of threads. It can be done by using option -t.

`java -jar Dingo.jar -o batch -i input_file -ns BP -s human -t 4`

### 4.4. Supported species

DiNGO supports GO enrichment analysis for 25 species. List of supported species can be find in configuration file (conf.properties). Note that some species have alias. For instance, GO enrichment analysis on a set of human gene/proteins could be invoked like following:

`java -jar Dingo.jar -o results -i list_of_genes.txt -ns MF -s Homo_sapiens`

or 

`java -jar Dingo.jar -o results -i list_of_genes.txt -ns MF -s human`

Similarly, it is possible to use -s mouse instead of -s Mus_musculus or -s dog instead of -s Canis_domesticus.

## 5. Modules

DiNGO’s functionality is extended by three modules: update, mapping and propagation module. Each of them could be used as separate application.

### 5.1. Update module

The update module performs download and processing of GO and HPO annotation resources upon user request. This functionality enables user to do enrichment analysis based on up-to-date ontology and annotation files. In addition, the module offers option for downloading [UniProt](ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/) [2] and [HUGO Gene Nomenclature Committee (HGNC)](https://www.genenames.org/) [3] mapping files as well as SwissProt uniprot_sprot.dat file.

#### 5.1.1. GO files

Module downloads GO OBO and annotation files in [GAF format](http://geneontology.org/docs/go-annotation-file-gaf-format-2.1/). In the case that GO files in DiNGO annotation folder have older date than downloaded ones they will be replaced.

#### 5.1.2. HPO files

The module downloads HPO OBO file and two annotation files: phenotype_annotation.tab and all_sources_all_frequencies_genes_to_phenotype.txt (https://hpo.jax.org/app/download/annotation). In order to be used by DiNGO the files have to be processed. HPO OBO file has no information about term namespace (subontology) that is used by DiNGO OBO file parser. Given that, after downloading HPO OBO file will be updated with namespace information for each term (see the propagation module). On the other hand, information contained in the downloaded annotation files will be used for creation of DiNGO compatible file. The new annotation file contains gene and the associated HPO term in each line.

#### 5.1.3. Uniprot mapping files

Module downloads UniProt mapping file which can be used by mapping module. Detailed explanation of the file structure can be found at the following link: ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/README. The file is downloaded into mapping folder. 

#### 5.1.4. SwissProt file

Module downloads uniprot_sprot.dat file, containing annotated curated entries, which can be used by mapping module. The file has multi fasta format. 

#### 5.1.5. HUGO file

Update module uses HGNC REST web service to download all records with an approved symbol (https://www.genenames.org/help/rest/). The module downloads XML HUGO file and extracts information from it. As a result TAB delimited file is created which contains HGNC, Uniprot identifiers as well as gene symbols in each line. The file can be used by DiNGO.

#### 5.1.6. Usage

The application has three options. The option -f defines file type that should be downloaded (GO, HPO, HUGO, UniProt, SwissProt) and this option is mandatory. The -sp option defines species (GO and UniProt files) or namespace (HPO file). If not specified it will be assumed that species is human (GO, UniProt) or that all HPO namespaces should be taken into consideration. The third option, -d, is used only with HPO and GO files. The option specifies which files will be updated: ontology file (-d 1), annotation file (-d 2) or both files (-d 3). The example bellow shows how to download and update GO ontology and annotation file:

`java -cp Dingo.jar update.DiNGOFilesUpdater update -f GO -sp Mus_musculus -d 3`

### 5.2. Mapping module

Mapping module converts one set of identifiers (IDs) to another one. The tool is inspired by [UniProt mapping tool](https://www.uniprot.org/uploadlists/). Mapping module relies on Uniprot idmapping.dat and SwiProt uniprot_sprot.fasta files (see Update Module section). As an input the module accepts list of IDs (one ID per line). The output file contains input IDs in the first column and required IDs in the second one. Note that like UniProt mapping tool, the module requires that input IDs or output ones to be UniProt identifiers. In other words, it is possible to convert gene symbols to UniProt IDs, but it is not possible to convert gene symbols to Ensembl gene IDs. The module can be run as standalone application.

#### 5.2.1. Usage

The following example will convert gene symbols identifiers to uniprot ones taking into consideration only manually curated entries:

`java -cp DiNGO.jar uniprot.UniprotMappingParser -i input_file -t UniProtKB -f Gene_Name -m HUMAN_9606_idmapping.dat -s uniprot_sprot.fasta`

As result file mapping.tab will be created. The file contains two columns separated by TAB. The first column contains gene symbol IDs and the second contains uniprot IDs.

The help is invoked in the following way:

`java -cp DiNGO.jar uniprot.UniprotMappingParser`

### 5.3. Propagation module

Propagation module function is to add namespace information to each term in HPO OBO file. It can be invoke in the following way:

`java -cp Dingo.jar propagation.Propagation -i <input HPO obo file> -o <output OBO file>`

## 6. Dependencies

Before building DiNGO from source the following dependencies must be satisfied:
- [colt.jar](https://dst.lbl.gov/ACSSoftware/colt)
- [commons-net-3.6.jar](https://commons.apache.org/proper/commons-net/index.html)
- [jdom-2.0.6.jar](http://www.jdom.org/downloads/)

## 7. References

[1]	Maere S, Heymans K, Kuiper M. BiNGO: a Cytoscape plugin to assess overrepresentation of gene ontology categories in biological networks. Bioinforma Oxf Engl 2005;21:3448–9. doi:10.1093/bioinformatics/bti551.

[2]	UniProt: a worldwide hub of protein knowledge. Nucleic Acids Res 2019;47:D506–15. doi:10.1093/nar/gky1049.

[3]	Yates B, Braschi B, Gray KA, Seal RL, Tweedie S, Bruford EA. Genenames.org: the HGNC and VGNC resources in 2017. Nucleic Acids Res 2017;45:D619–25. doi:10.1093/nar/gkw1033.

[4]	Forbes SA, Beare D, Boutselakis H, Bamford S, Bindal N, Tate J, et al. COSMIC: somatic 
