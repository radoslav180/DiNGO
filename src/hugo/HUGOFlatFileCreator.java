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

package hugo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * <p>
 * Class responsible for creating tab delimited file from HUGO XML file</p>
 *
 * @author Radoslav Davidović
 */
public class HUGOFlatFileCreator {

    /**
     * <p>
     * Name of XML file downloaded from HUGO
     * https://www.genenames.org/help/rest/</p>
     */
    private final String xmlFile;
    
    /**
     * <p>Constructor</p>
     * @param xmlFile name of HUGO XML file 
     */
    public HUGOFlatFileCreator(String xmlFile) {
        this.xmlFile = xmlFile;
    }
    /**
     * <p>Method which parses XML file and creates tab delimited file</p>
     * @param outFile name of output file
     */
    public void createFlatFile(String outFile) {
        try {
            PrintWriter writer = new PrintWriter(outFile);
            File hugoFile = new File(xmlFile);
            SAXBuilder saxb = new SAXBuilder();
            Document document = saxb.build(hugoFile);
            Element element = document.getRootElement();
            List<Element> elements = element.getChildren("result");
            if (elements == null) {
                System.out.println("No elements found!!");
                return;
            }
            for (int i = 0; i < elements.size(); i++) {
                List<Element> docs = elements.get(i).getChildren("doc");
                
                for (Element doc : docs) {
                    List<Element> ids = doc.getChildren();

                    for (Element id : ids) {
                        //gene symbol
                        if (id.getAttributeValue("name").equals("symbol")) {
                            writer.print(id.getText() + "\t");
                        }
                        //HGNC id
                        if (id.getAttributeValue("name").equals("hgnc_id")) {
                            writer.print(id.getText() + "\t");
                        }
                        //alias of gene symbol
                        if (id.getAttributeValue("name").equals("alias_symbol")) {
                            List<Element> aliasList = id.getChildren("str");

                            for (Element a : aliasList) {
                                writer.print(a.getText() + "\t");
                            }
                        }
                        //entrez id
                        if (id.getAttributeValue("name").equals("entrez_id")) {
                            writer.print(id.getText() + "\t");
                        }
                        //previous symbols 
                        if (id.getAttributeValue("name").equals("prev_symbol")) {
                            List<Element> prevList = id.getChildren("str");

                            for (Element p : prevList) {
                                writer.print(p.getText() + "\t");
                            }
                        }
                        //uniprot ids
                        if (id.getAttributeValue("name").equals("uniprot_ids")) {
                            List<Element> uniprotList = id.getChildren("str");

                            for (Element unip : uniprotList) {
                                writer.print(unip.getText() + "\t");
                            }
                        }
                    }
                    writer.println();
                }

            }
            writer.close();
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(HUGOFlatFileCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
