/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.faban.harness.formsgen;

import com.sun.faban.common.FabanNamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sp208304
 */
public class XformsUtil {

    
    static ArrayList<String> propertyLabelsStack = new ArrayList<String>();
    static ArrayList<String> needLabelsStack = new ArrayList<String>();
    static int nodeCount = 0;
    StringBuilder casesBuffer = null;
    ArrayList<String> labelsStack = new ArrayList<String>();
    ArrayList<String> ignoreNodesStack = new ArrayList<String>();
    ArrayList<String> idStack = new ArrayList<String>();
    int idCount = 0;
    Document doc = XformsGenerator.doc;
    FabanNamespaceContext ns = new FabanNamespaceContext();
    HashMap<String, String> map = new HashMap<String, String>();
    static HashMap<String, String> selectsMap = new HashMap<String, String>();
    XformsHandler handle;

    private void loadMap() {
        map.put("scale", "positiveInteger");
        map.put("rampup", "positiveInteger");
        map.put("steadystate", "positiveInteger");
        map.put("rampdown", "positiveInteger");
        map.put("delay", "positiveInteger");
        map.put("statsinterval", "positiveInteger");
        map.put("port", "positiveInteger");
    }

    private void loadLabels() {
        labelsStack.add("threadStart");
        labelsStack.add("unit");
        labelsStack.add("time");
        labelsStack.add("cycles");
        labelsStack.add("yes");
        labelsStack.add("no");
        labelsStack.add("ok");
        labelsStack.add("true");
        labelsStack.add("false");
        labelsStack.add("cancel");
        labelsStack.add("delay");
        labelsStack.add("simultaneous");
        labelsStack.add("parallel");
        labelsStack.add("dbServer");
        labelsStack.add("dbDriver");
        labelsStack.add("connectURL");
        labelsStack.add("reloadDB");
    }

    private void loadIgnoreStack() {
        ignoreNodesStack.add("outputDir");
        ignoreNodesStack.add("audit");
        ignoreNodesStack.add("stats");
        ignoreNodesStack.add("timeSync");
    }

    private void loadNeedLabelsStack() {
        needLabelsStack.add("hostConfig");
        needLabelsStack.add("runControl");
        needLabelsStack.add("threadStart");
        needLabelsStack.add("runtimeStats");
        needLabelsStack.add("driverConfig");
        needLabelsStack.add("service");
    }

    private void loadSelectsStack() {
        selectsMap.put("enabled", "true");
        selectsMap.put("restart", "true");
        selectsMap.put("parallel", "yes");
        selectsMap.put("simultaneous", "yes");
    }

    public void buildPropertyLabelsStack(Node eNode) {
        String st = null;
        NamedNodeMap attrList = eNode.getAttributes();
        for (int j = 0; j < attrList.getLength(); j++) {
            if (attrList.item(j).getNodeType() == Node.ATTRIBUTE_NODE) {
                if ("property".equals(eNode.getLocalName()) && attrList.item(j).getNodeName().equals("name")) {
                    st = attrList.item(j).getNodeValue();
                }
            }
        }
        if("property".equals(eNode.getLocalName()) && hasMoreElements(eNode) && st == null){
            st = getNodeValueForMatchingNode(eNode, "name");
        }
        if (!propertyLabelsStack.contains(st) && st != null) {
            propertyLabelsStack.add(st);
        }
        NodeList list = eNode.getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE){
                buildPropertyLabelsStack(list.item(i));
            }
        }
    }

    public void buildXforms() {
        loadMap();
        loadLabels();
        loadNeedLabelsStack();
        loadIgnoreStack();
        loadSelectsStack();
        String id = "";
        buildXformsBind(doc.getDocumentElement(), 0, id);
        buildXformsLabels(doc.getDocumentElement());
    }

    public void buildXformsBind(Node eNode, int spaces, String id) {
        String tab = indent(spaces);
        Node prevNode = null;
        String strg = eNode.getLocalName();
        String binds = strg;
        String nsStr = null;
        String attr = null;
        String inputs = " ";
        String s = null;
        if (id.lastIndexOf("-") > 0) {
            s = id.substring(id.lastIndexOf("-") + 1, id.length());
        }
        s = map.get(s);
        if (s != null) {
            s = "xforms:type='" + s + "'";
        } else {
            s = " ";
        }
        nsStr = eNode.getNamespaceURI();
        if (nsStr != null) {
            nsStr = ns.getPrefix(nsStr)+":";
        }else{
            nsStr = "";
        }
        NamedNodeMap attrList = eNode.getAttributes();
        for (int i = 0; i < attrList.getLength(); i++) {
            if (attrList.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
                if (attrList.item(i).getNodeName().equals("name")) {
                    attr = attrList.item(i).getNodeValue();
                }
                if (attrList.item(i).getNodeName().equals("enabled") &&
                        "false".equals(attrList.item(i).getNodeValue())) {
                    ignoreNodesStack.add(strg);
                }
                if ("property".equals(eNode.getLocalName()) && attrList.item(i).getNodeName().equals("name")) {
                    String st = attrList.item(i).getNodeValue();
                    addLabel(st);
                }
            }
        }
        if(doc.getDocumentElement() == eNode){
            binds = tab + "<xforms:bind id='bind-"+ strg +"' xforms:nodeset='/" + strg + "'>\n";
        } else if("property".equals(eNode.getLocalName()) && hasMoreElements(eNode)){
            if (hasMoreElements(eNode) && attr == null) {
                String st = getNodeValueForMatchingNode(eNode, "name");
                binds = tab + "<xforms:bind id='bind-" + id + "' xforms:nodeset='" + nsStr + strg + "[" + nsStr + "name=" + '"' + st + '"' + "]/" + nsStr + "value'>\n";
                addLabel(st);
            } else if (hasMoreElements(eNode) && attr != null){
                binds = tab + "<xforms:bind id='bind-" + id + "' xforms:nodeset='" + nsStr + strg + "[@name=" + '"' + attr + '"' + "]/" + nsStr + "value'>\n";
            } else if (!hasMoreElements(eNode) && attr != null){
                binds = tab + "<xforms:bind id='bind-" + id + "' xforms:nodeset='" + nsStr + strg + "[@name=" + '"' + attr + '"' + "]'>\n";
            }
        } else {
            if (attr != null) {
                binds = tab + "<xforms:bind id='bind-" + id + "' xforms:nodeset='" + nsStr + strg + "[@name=" + '"' + attr + '"' + "]' " + s + ">\n";
            } else {
                binds = tab + "<xforms:bind id='bind-" + id + "' xforms:nodeset='" + nsStr + strg + "' " + s + ">\n";
            }
        }
        if (XformsGenerator.xformsBindBuffer == null) {
            XformsGenerator.xformsBindBuffer = new StringBuilder(binds);
        } else {
            XformsGenerator.xformsBindBuffer.append(binds);
        }

        //Handling cases section
        if(eNode != doc.getDocumentElement() && eNode.getParentNode() == doc.getDocumentElement()){
            inputs = tab + "<xforms:case id='case-" + strg + "'>" + "\n";           
        }else if (eNode.getParentNode().getParentNode() == doc.getDocumentElement()) {
            if (!ignoreNodesStack.contains(strg)) {                
                handle = new XformsHandler(eNode, id);
                inputs = handle.executeElement().toString();
            }
        }
        if (XformsGenerator.xformsCasesBuffer == null) {
            XformsGenerator.xformsCasesBuffer = new StringBuilder(inputs);
        } else {
            XformsGenerator.xformsCasesBuffer.append(inputs);
        }

        loopBack(eNode, spaces, id, prevNode, "buildXformsBind", null);
        XformsGenerator.xformsBindBuffer.append(tab + "</xforms:bind>\n");
        if(eNode != doc.getDocumentElement() && eNode.getParentNode() == doc.getDocumentElement()){
            XformsGenerator.xformsCasesBuffer.append(tab + "</xforms:case>\n");
        }
        spaces--;
    }

    private void loopBack(Node eNode, int spaces, String id,
                                        Node prevNode, String methodName, ArrayList<String> stack){
        NodeList list = eNode.getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE){
                spaces++;
                int j=i;
                while(j>0){
                    if(list.item(j).getPreviousSibling().getNodeType() == Node.ELEMENT_NODE){
                            prevNode = list.item(j);
                            break;
                    }
                    j--;
                }
                String idStr = list.item(i).getLocalName();
                if(prevNode != null){
                    int index = id.lastIndexOf("-");
                    String targetStr = id.substring(index+1, id.length());
                    id = id.replace(targetStr,idStr);
                }else{
                    if(!"".equals(id))
                        id = id + "-" + idStr;
                    else
                        id = idStr;
                }
                if (idStack.size() > 0 && idStack.contains(id)) {
                        for (int x = 0; x < idStack.size(); x++) {
                            if (idStack.get(x).equals(id)) {
                                int cnt = idCount++;
                                id = id + "_" + cnt;
                            }
                        }
                }
                idStack.add(id);
                if(methodName != null && methodName.trim().length() > 0) {
                    if ("buildXformsBind".equals(methodName)) {
                        buildXformsBind(list.item(i),spaces,id);
                    } else if ("buildXformsCases".equals(methodName)) {
                        nodeCount++;
                        buildXformsCases(list.item(i),spaces,id,stack);
                        nodeCount--;
                    }
                }
                spaces--;
            }
        }
    }

    public StringBuilder buildXformsCases(Node eNode, int spaces, String id, ArrayList<String> stack) {
        ArrayList<String> ignoreStack = stack;
        String tab = indent(spaces);
        Node prevNode = null;
        String strg = eNode.getLocalName();
        String inputs = " ";
        if("property".equals(eNode.getLocalName()) && propertyLabelsStack.size() > 0) {
            inputs = tab + "<xforms:input id='input-" + id + "' xforms:bind='bind-" + id + "'>" + "\n" +
                    "\t\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + propertyLabelsStack.get(0) + "'/>" + "\n" +
                    "\t\t</xforms:input>\n";
            propertyLabelsStack.remove(0);
        } else if (!ignoreStack.contains(strg)) {
            if (hasMoreElements(eNode)) {
                if(nodeCount == 0) {
                    if(needLabelsStack.contains(strg)){
                        inputs = tab + tab + tab + "<xforms:group id='group-" + strg + "'>" + "\n"+
                                tab + tab + tab + "<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + strg + "' />" + "\n";
                    }else{
                        inputs = tab + tab + tab + "<xforms:group id='group-" + strg + "'>" + "\n";
                    }
                } 
            } else if (!hasMoreElements(eNode)) {
                if (nodeCount == 0) {
                    if (strg.equalsIgnoreCase("description")) {
                        inputs = tab + "<xforms:group id='group-" + strg + "'><xforms:textarea id='input-" + id + "' xforms:bind='bind-" + id + "'>" + "\n" +
                                "\t\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + strg + "'/>" + "\n" +
                                "\t\t</xforms:textarea></xforms:group>\n";
                    } else {
                        inputs = tab + "<xforms:group id='group-" + strg + "'><xforms:input id='input-" + id + "' xforms:bind='bind-" + id + "'>" + "\n" +
                                "\t\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + strg + "'/>" + "\n" +
                                "\t\t</xforms:input></xforms:group>\n";
                    }
                } else {
                    if (selectsMap.containsKey(strg)) {
                        String label = selectsMap.get(strg);
                        String choice1 = "true";
                        String choice2 = "false";
                        if("yes".equals(label)){
                            choice1 = "yes";
                            choice2 = "no";
                        }
                        inputs = tab + "<xforms:select1 id='input-" + id + "' xforms:bind='bind-" + id + "'>" + "\n" +
                                "\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + strg + "'/>" + "\n" +
                                "\t\t\t<xforms:choices>" + "\n" +
                                "\t\t\t\t<xforms:item>" + "\n" +
                                "\t\t\t\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + choice1 + "'/>" + "\n" +
                                "\t\t\t\t\t<xforms:value>true</xforms:value>" + "\n" +
                                "\t\t\t\t</xforms:item>" + "\n" +
                                "\t\t\t\t<xforms:item>" + "\n" +
                                "\t\t\t\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + choice2 + "'/>" + "\n" +
                                "\t\t\t\t\t<xforms:value>false</xforms:value>" + "\n" +
                                "\t\t\t\t</xforms:item>" + "\n" +
                                "\t\t\t</xforms:choices>" + "\n" +
                                "\t\t</xforms:select1>\n";
                    } else {
                        inputs = tab + "<xforms:input id='input-" + id + "' xforms:bind='bind-" + id + "'>" + "\n" +
                                "\t\t\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/" + strg + "'/>" + "\n" +
                                "\t\t</xforms:input>\n";
                    }
                }
            }
        }

        if (casesBuffer == null) {
            casesBuffer = new StringBuilder(inputs);
        } else {
            casesBuffer.append(inputs);
        }

        loopBack(eNode, spaces, id, prevNode, "buildXformsCases", ignoreStack);
        
        if (!ignoreStack.contains(strg) && hasMoreElements(eNode)) {
            if(nodeCount == 0) {
                casesBuffer.append(tab + tab + "</xforms:group>\n");
            }else{
                casesBuffer.append(tab);
            }
        }
        spaces--;
        return casesBuffer;
    }

    public boolean hasMoreElements(Node node) {
        boolean hasNodes = false;
        NodeList list = node.getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE){
                hasNodes = true;
                break;
            }
        }
        return hasNodes;
    }

    public String getNodeValueForMatchingNode(Node eNode, String s) {
        String nodeVal = null;
        NodeList list = eNode.getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            Node childNode = list.item(i);
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE &&
                    s.equals(list.item(i).getLocalName())){
                NodeList childList = childNode.getChildNodes();
                int length = childList.getLength();
                for (int l = 0; l < length; l++) {
                    Node valueNode = (Node) childList.item(l);
                    if (valueNode.getNodeType() == Node.TEXT_NODE) {
                        nodeVal = valueNode.getNodeValue().trim();
                        break;
                    }
                }
            }
        }
        return nodeVal;
    }
    
    public String indent(int spaces) {
        StringBuffer buffer = new StringBuffer();
         for (int i = 0; i < spaces; i++) {
             buffer.append("\t");
         }
         return buffer.toString();
     }

    public boolean needGroup(Node node) {
        boolean need = true;
        NodeList list = node.getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE){
               if (hasMoreElements(list.item(i))) {
                   need = false;
                   break;
               }
            }
        }
        return need;
    }

    /**
     * Generates a label from the given string.
     * @param s
     * @return String
     */
    public String makeLabel(String s) {
        int cnt = 0;
        ArrayList str = new ArrayList();
        for (int i = 0; i < s.length(); i++) {
            for (char c = 'A'; c <= 'Z'; c++) {
                if (s.charAt(i) == c) {
                    str.add(s.substring(cnt,i));
                    cnt = i;
                    //j++;
                }
            }
        }
        str.add(s.substring(cnt,s.length()));
        String newStr = (String) str.get(0);
        if(!str.isEmpty()){
            for(int i = 1; i < str.size(); i++){
                newStr = newStr + " " + str.get(i);
            }
            s = newStr;
        }
        return (s.length()>0)? Character.toUpperCase(s.charAt(0))+s.substring(1) :s;
    }

    private void addLabel(String s) {
        if(!labelsStack.contains(s)) {
            labelsStack.add(s);
            s = "<"+s+">"+ makeLabel(s) +"</"+s+">" + "\n";
            if (XformsGenerator.xformsLabelsBuffer == null) {
                XformsGenerator.xformsLabelsBuffer = new StringBuilder(s);
            } else {
                XformsGenerator.xformsLabelsBuffer.append(s);
            }
        }
    }

    public void buildXformsLabels(Node eNode) {
        String s = eNode.getLocalName();
        addLabel(s);
        NodeList list = eNode.getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE){
                buildXformsLabels(list.item(i));
                if(list.item(i).getParentNode() == doc.getDocumentElement())
                    buildXformsTriggers(list.item(i).getLocalName());
            }
        }
    }

    private void buildXformsTriggers(String s) {
        String trigger = "<xforms:trigger id='trigger-"+s+"'>" + "\n" +
                "\t<xforms:label xforms:model='benchmark-labels' xforms:ref='/labels/"+s+"'/>" + "\n" +
                "\t<xforms:action id='action-"+s+"'>" + "\n" +
                    "\t\t<xforms:revalidate xforms:model='benchmark-model' id='revalidate-"+s+"'/>" + "\n" +
                    "\t\t<xforms:toggle id='toggle-"+s+"' xforms:case='case-"+s+"'/>" + "\n" +
                "\t</xforms:action>" + "\n" +
            "</xforms:trigger>" + "\n";
        if (XformsGenerator.xformsTriggersBuffer == null) {
            XformsGenerator.xformsTriggersBuffer = new StringBuilder(trigger);
        } else {
            XformsGenerator.xformsTriggersBuffer.append(trigger);
        }
    }

}
