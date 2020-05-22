/*
 Copyright 2019  Damian Murphy <murff@warlock.org>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
 */
package org.warlock.xpathgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author damian
 */
public class XpathGenerator {

    private static final String[] DEFAULT_PREFIXES = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "l", "m", "n", "o", "p", "q",
        "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    private static final ArrayList<String> prefixes = new ArrayList<>();
    private static int prefix = -1;
    private static final HashMap<String, String> bindings = new HashMap<>();
    private static final HashSet<String> xpaths = new HashSet<>();
    
    private static boolean includeIndexes = false;
    private static boolean printvalue = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        int argcount = 0;
        int options = 0;
        
        while (args[argcount].startsWith("-")) {
            if (args[argcount].contentEquals("-i")) {
                includeIndexes = true;
                options++;
            }
            if (args[argcount].contentEquals("-p")) {
                printvalue = true;
                options++;
            }
            argcount++;
        }
        
        switch (args.length - options) {
            case 1:
                loadContext(null);
                break;

            case 2:
                loadContext(args[1 + options]);
                break;

            default:
                System.err.println("Usage: java -jar XPathGenerator inputfile contextfile");
                System.exit(1);
                break;
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(new File(args[options]));
            getXpaths("/", d.getDocumentElement(), 1);
            System.out.println("\nNamespaces:");
            for (String n : bindings.keySet()) {
                String p = bindings.get(n);
                StringBuilder sb = new StringBuilder("xmlns:");
                sb.append(p);
                sb.append("=\"");
                sb.append(n);
                sb.append("\"");
                System.out.println(sb.toString());
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    private static void loadContext(String f) {
        if (f == null) {
            prefixes.addAll(Arrays.asList(DEFAULT_PREFIXES));
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            @SuppressWarnings("UnusedAssignment")
            String l = null;
            while ((l = br.readLine()) != null) {
                if (!l.startsWith("xmlns")) {
                    throw new Exception(f + " is not a namespace context list: " + l);
                }
                @SuppressWarnings("UnusedAssignment")
                String pfx = null;
                String[] parts = l.split("=");
                if (!parts[0].contains(":")) {
                    pfx = "";
                } else {
                    String[] decl = parts[0].split(":");
                    pfx = decl[1];
                }
                String uri = parts[1].substring(1, parts[1].length() - 1);
                prefixes.add(pfx);
                bindings.put(uri, pfx);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void getXpaths(String path, Node n, int nodeindex) {
        String p = null;
        String s = n.getNamespaceURI();
        if (s != null) {
            if (bindings.containsKey(s)) {
                p = bindings.get(s);
            } else {
                p = prefixes.get(++prefix);
                bindings.put(s, p);
            }
        }
        StringBuilder sb = new StringBuilder(path);
        if (p != null) {
            sb.append(p);
            sb.append(":");
        }
        sb.append(n.getLocalName());
        if (includeIndexes) {
            sb.append("[");
            sb.append(nodeindex);
            sb.append("]");            
        }
        String r = sb.toString();
        sb.append("/");
        String cp = sb.toString();
        if (!xpaths.contains(cp)) {
            xpaths.add(cp);
            System.out.println(r);
        }
        NamedNodeMap attrs = n.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Attr at = (Attr) attrs.item(a);
            attributeXpath(cp, at);
        }
        HashMap<String,Integer> nodeindexes = null;
        if (includeIndexes) {
           nodeindexes = new HashMap<>(); 
        }
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    if (includeIndexes) {
                        String nn = node.getLocalName();
                        if (nodeindexes.containsKey(nn)) {
                            int ni = nodeindexes.get(nn);
                            ni++;
                            nodeindexes.put(nn, ni);
                            getXpaths(cp, node, ni);
                        } else {
                            nodeindexes.put(nn, 1);
                            getXpaths(cp, node, 1);
                        }                        
                    } else {
                        getXpaths(cp, node, -1);
                    }
                    
                    break;

                default:
                    break;
            }
        }
    }

    private static void attributeXpath(String path, Attr a) {
        String p = null;
        if (a.getName().contentEquals("xmlns")) {
            return;
        }
        String s = a.getNamespaceURI();
        if (s != null) {
            if (bindings.containsKey(s)) {
                p = bindings.get(s);
            } else {
                p = prefixes.get(++prefix);
                bindings.put(s, p);
            }
        }
        StringBuilder sb = new StringBuilder(path);
        sb.append("@");
        if (p != null) {
            sb.append(p);
            sb.append(":");
        }
        sb.append(a.getNodeName());
        if (printvalue) {
            sb.append("\tValue = ");
            sb.append(a.getValue());
        }
        System.out.println(sb.toString());
    }

}
