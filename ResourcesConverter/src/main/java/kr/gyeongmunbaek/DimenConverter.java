package kr.gyeongmunbaek;
 * ReadXMLFile.java
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DimenConverter {
    Document mDocument = null;

    public void convertDimenXMLFile(String pFileName, float pRatio) {
        StringBuilder xmlData = new StringBuilder();

        try {
            Reader reader = new FileReader(pFileName);
            BufferedReader br = new BufferedReader(reader);

            String str;
            while ((str = br.readLine()) != null)
                xmlData.append(str + "\n");

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(xmlData);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream is = new ByteArrayInputStream(xmlData.toString()
                    .getBytes("utf-8"));
            Document document = builder.parse(is);
            Element device = document.getDocumentElement();
            NodeList items = device.getElementsByTagName("dimen");

            for (int i = 0; i < items.getLength(); i++) {
                Element lDimenElmnt = (Element) items.item(i);
                Node lDimenNode = lDimenElmnt.getFirstChild();
                String lValue = lDimenNode.getNodeValue();
                String lNewValue = "";
                float lDimen = 0.0f;
                if (lValue.contains("dp") || lValue.contains("dip") || lValue.contains("px")) {
                    String lFormat = "";
                    if (lValue.contains("dp")) {
                        lValue = lValue.replaceAll("dp", "");
                        lFormat = "dp";
                    } else if (lValue.contains("dip")) {
                        lValue = lValue.replaceAll("dip", "");
                        lFormat = "dip";
                    } else if (lValue.contains("px")) {
                        lValue = lValue.replaceAll("px", "");
                        lFormat = "px";
                    }
                    // System.out.println("\n Value : " + lValue);
                    lDimen = Float.parseFloat(lValue.trim());
                    lDimen = lDimen * pRatio;
                    
                    if (lFormat.equals("px") && lDimen < 1.0f) {
                        lDimen = 1.0f;
                    }
                    lNewValue = String.valueOf(lDimen) + lFormat;
                } else if (lValue.contains("dimen")) {
                    lNewValue = lValue;
                }
                lDimenNode.setNodeValue(lNewValue);
            }
            mDocument = document;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mergeDimenXMLFile(String pSrcFileName) {
        StringBuilder xmlData = new StringBuilder();

        try {
            Reader reader = new FileReader(pSrcFileName);
            BufferedReader br = new BufferedReader(reader);

            String str;
            while ((str = br.readLine()) != null)
                xmlData.append(str + "\n");

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(xmlData);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream is = new ByteArrayInputStream(xmlData.toString()
                    .getBytes("utf-8"));
            Document document = builder.parse(is);
            Element device = document.getDocumentElement();
            NodeList items = device.getElementsByTagName("dimen");

            for (int i = 0; i < items.getLength(); i++) {
                Element lDimenElmnt = (Element) items.item(i);
                Node lDimenNode = lDimenElmnt.getFirstChild();
                String lValue = lDimenNode.getNodeValue();
                if (lValue.contains("dp") || lValue.contains("dip") || lValue.contains("dimen")) {
                    // lDimenElmnt.removeChild(lDimenNode);
                } else {
                    System.out.println("lDimenNode : " + lDimenNode.toString());
                    Element resElement = mDocument.getDocumentElement();
                    System.out.println("resElement : " + resElement.toString());
                    Element lDimen = mDocument.createElement("dimen");
                    System.out.println("lDimenElmnt.getAttributes getNodeValue() : " + lDimenElmnt.getAttributes().getNamedItem("name").getNodeValue());
                    lDimen.setAttribute("name", lDimenElmnt.getAttributes().getNamedItem("name").getNodeValue());
                    lDimen.appendChild(mDocument.createTextNode(lValue));
                    resElement.appendChild(lDimen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createXMLFile(String pDirectory, String pFileName) {
        DOMSource xmlDOM = new DOMSource(mDocument);
        File lDir = new File(pDirectory);
        if (lDir.exists() == false) {
            lDir.mkdir();
        } else {
            File[] destroy = lDir.listFiles(); 
            for(File des : destroy){
                if (des.getName().equalsIgnoreCase(pFileName)) {
                    des.delete();
                }
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(pDirectory + "/"
                    + pFileName));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        StreamResult xmlFile = new StreamResult(fw);
        try {
            TransformerFactory lTransformerFactory = TransformerFactory.newInstance();
            lTransformerFactory.setAttribute("indent-number", new Integer(4));
            
            Transformer lTransformer = lTransformerFactory.newTransformer();
            lTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            lTransformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            lTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            lTransformer.transform(xmlDOM, xmlFile);
        } catch (TransformerException | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
    }
}
