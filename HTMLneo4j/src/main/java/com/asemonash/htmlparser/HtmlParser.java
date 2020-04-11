package com.asemonash.htmlparser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.w3c.dom.css.ElementCSSInlineStyle;


public class HtmlParser<E> {
	
	private File htmlFile;
	private Document document;
	private DiagramNode<E> diagramNode;
	private DiagramEdge<E> diagramEdge;
	private boolean isNode;
	private List<DiagramNode<E>> diagramNodesList;
	private List<DiagramEdge<E>> diagramEdgesList;
	private List<Relationships> relationshipsList;
	private List<E> graphElementsList;
	/**
	 * afterCounter and beforeCounter are debug
	 * statements. Remove in the final version
	 */
	private int afterCounter, beforeCounter = 0;
	
	public HtmlParser(File file) {
		this.htmlFile = file;
		diagramNodesList = new LinkedList<DiagramNode<E>>();
//		diagramEdgesList = new LinkedList<DiagramEdge<E>>();
		graphElementsList = new LinkedList<E>();
		relationshipsList = new LinkedList<Relationships>();
	}
	
	public void initHtmlParser() {
		try {
			document = Jsoup.parse(htmlFile, "UTF-8", "");
			Elements map = document.getElementsByTag("map");
			String mapHtml = map.toString();
			Document mapHtmlDoc = Jsoup.parse(mapHtml);
			Elements areaTagElements = mapHtmlDoc.getElementsByTag("area");

			for(Element areaTagElement : areaTagElements) {
				diagramNode = new DiagramNode<E>();
				diagramEdge = new DiagramEdge<E>();
				extractDataFrmAttr(areaTagElement.attributes());
				if(isNode) {
					if(diagramNode.getId() == null) {
						continue;
					}
					else {
						diagramNodesList.add(diagramNode);
						//graphElementsList.add((E) diagramNode);
					}
				}
				else {
					//diagramEdgesList.add(diagramEdge);
					graphElementsList.add((E)diagramEdge);
				}
				beforeCounter++;
			}
			
			createRelationships(document, diagramNodesList);
			displayRelatiosnhips();
			
			//rowCounterDebugFunc();
			//displayNodeList();
			//System.out.println("******************");
			//displayEdgeList();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void extractDataFrmAttr(Attributes attributes) {
	
		for(Attribute attribute: attributes.asList()) {

			if(attribute.getKey().equalsIgnoreCase("alt") && 
				(attribute.getValue().equalsIgnoreCase("from")|| 
						attribute.getValue().equalsIgnoreCase("to"))){
					if(attribute.getValue().equalsIgnoreCase("from")) {
						diagramEdge.setLabel(Label.FROM);
					}
					else {
						diagramEdge.setLabel(Label.TO);
					}
					isNode = false;
			}
			
			else if(attribute.getKey().equalsIgnoreCase("alt") && 
					(attribute.getValue().toLowerCase().contains("CONDITION".toLowerCase())|| 
							attribute.getValue().toLowerCase().contains("OPERATION".toLowerCase()))) {
				isNode = true;
				if(attribute.getValue().toLowerCase().contains("OPERATION".toLowerCase())) {
					
					diagramNode.setLabel(Label.OPERATION);
					diagramNode.setName((E) attribute.getValue());
				}
				else {
					diagramNode.setLabel(Label.CONDITION);
					diagramNode.setName((E) attribute.getValue());
				}
			}
			
			else if(attribute.getKey().equalsIgnoreCase("alt") && 
					(attribute.getValue().toLowerCase().contains("PROBLEM DEFINITION".toLowerCase()))) {
				isNode = true;
				diagramNode.setLabel(Label.DAP);
				String value = attribute.getValue();
				String subValue = value.substring(0, value.indexOf(":"));
				
				//String subLabel = value.substring(value.indexOf(":"), value.length());
				diagramNode.setName((E)subValue);
				//diagramNode.setSubLabel((E)subLabel);
			}
			
			else if(attribute.getKey().equalsIgnoreCase("alt") &&
					attribute.getValue().toLowerCase().contains("INITIAL STEP".toLowerCase()) ||
					attribute.getValue().toLowerCase().contains("STEPS".toLowerCase())) {
				
				isNode = true;
				if(attribute.getValue().toLowerCase().contains("INITIAL STEP".toLowerCase())) {
					diagramNode.setLabel(Label.INITIAL_STEP);
					diagramNode.setName((E) attribute.getValue());
				}
				else {
					diagramNode.setLabel(Label.STEPS);
					diagramNode.setName((E) attribute.getValue());
				}
			}
			
			else if(attribute.getKey().equalsIgnoreCase("alt") &&
					attribute.getValue().toLowerCase().contains("START".toLowerCase()) ||
					attribute.getValue().toLowerCase().contains("END".toLowerCase())) {
				
				isNode = true;
				if(attribute.getValue().toLowerCase().contains("START".toLowerCase())) {
					diagramNode.setLabel(Label.START);
					diagramNode.setName((E) attribute.getValue());
				}
				else {
					diagramNode.setLabel(Label.END);
					diagramNode.setName((E) attribute.getValue());
				}
			}
			
			else if(attribute.getKey().equalsIgnoreCase("alt") &&
					attribute.getValue().toLowerCase().contains("HIGH LEVEL".toLowerCase()) ||
					attribute.getValue().toLowerCase().contains("LOW LEVEL".toLowerCase())) {
				
				isNode = true;
				if(attribute.getValue().toLowerCase().contains("HIGH LEVEL".toLowerCase())) {
					diagramNode.setLabel(Label.HIGH_LEVEL);
					diagramNode.setName((E) attribute.getValue());
				}
				else {
					diagramNode.setLabel(Label.LOW_LEVEL);
					diagramNode.setName((E) attribute.getValue());
				}
			}
			
			else if(attribute.getKey().equalsIgnoreCase("alt") &&
					attribute.getValue().toLowerCase().contains("TASK".toLowerCase())) {
				
				isNode = true;
				diagramNode.setLabel(Label.TASK);
				String value = attribute.getValue();
				String subValue = value.substring(0, value.indexOf(":"));
				diagramNode.setName((E)subValue);
			}
			else if(attribute.getKey().equalsIgnoreCase("alt")) {
				
				isNode = true;
				diagramNode.setLabel(Label.SUB_TASK);
				String value = attribute.getValue();
				String subValue = value.substring(0, value.indexOf(":"));
				String subLabel = value.substring(value.indexOf(":"), value.length());
				diagramNode.setName((E)subValue);
				diagramNode.setSubLabel((E)subLabel);
			}
			
			if(attribute.getKey().equalsIgnoreCase("href")) {
				if(isNode == true) {
					String nodeID = attribute.getValue().substring(1);
					diagramNode.setId(nodeID);
					
				}
				else {
					String edgeID = attribute.getValue().substring(1);
					diagramEdge.setId((E) edgeID);
				}
			}
		}
		
		afterCounter++;
	}
	
	
	private void createRelationships(Document htmlDoc, List<DiagramNode<E>> graphElementsList) {
		Map<String, Elements> map = new HashMap();
		List<String> mapKeys = new LinkedList<String>();
		Elements divElement = htmlDoc.getElementsByTag("div");
		//System.out.println(divElement);
		
		Elements dElements = htmlDoc.select("div#3_20101");
		
		List<Node> eList = null;
		for(Element element : dElements) {
			eList = element.childNodes();
			//System.out.println(eList);
		}
		
		
		
		
		for(DiagramNode<E> element: graphElementsList) {
			
			//System.out.println(element.getClass().toString().contains("DiagramNode"));
			//System.out.println("ELEMENT " + element);
			if(element.getClass().toString().contains("DiagramNode")) {
				
//				System.out.println("The sub Label is-->"+((DiagramNode<E>) element).getSubLabel() +
//						" The Label is-->"+ ((DiagramNode<E>) element).getLabel() + 
//						" THE NAME IS " + ((DiagramNode<E>) element).getName());
				
			}
			//System.out.println("The element is-->" + ((DiagramNode<E>) element).getSubLabel());
		}
		
		
		
		for (DiagramNode<E> element : diagramNodesList) {
			if(element.getClass().toString().toLowerCase().contains("DIAGRAMNODE".toLowerCase()) 
					&& ((DiagramNode<E>) element).getId() != null ) { 
				//THE NOT NULL CHECK IS TEMPORARY REMOVE AFTER RESOLVING
				//System.out.println("div#"+ ((DiagramNode<E>) element).getId());
				String id = (String) ((DiagramNode<E>) element).getId();
				String divID = "div#"+ id;
				//Elements eTest = htmlDoc.select(divID);s
				String rTable = divID + "_RelationshipsTable";
				Elements innerrelTable = htmlDoc.select(rTable);
				String name = (String) ((DiagramNode<E>) element).getName();
				mapKeys.add(id);
				for(Element e : innerrelTable) {
					//System.out.println(name+" --> "+e.getElementsByTag("tr"));
					Elements innerEle = e.getElementsByTag("tr");
					map.put(id, innerEle);
				}
				//System.out.println(id+"\n"+ innerrelTable);
				
				
				
				//Document innerHtmlTable = Jsoup.parse(innerrelTable);
				
				//System.out.println(innerHtmlTable);
				
				//System.out.println("**********************");
			}
		}
		for(String key: mapKeys) {
			Elements vElements = map.get(key);
			//System.out.println("Key-->" + key);
			
			if(vElements != null) {
				//int i = 0;
				for(Element ev : vElements) {
					
					//if(i < ev.childNodeSize()) {
						//System.out.println("entire string "+ ev);
					
					//System.out.println("Child Node th "+ev.childNodes());
					calculateRelationships(key, ev.childNodes());
					
					//	System.out.println("Child Node td "+ev.childNodes());
					
						
					//}
					//i++;
					//System.out.println();
//					if(ev.getElementsByTag("td").toString().length()>0 &&
//							i < ev.childNodeSize()) {
//						System.out.println("iteration-->" + i);
//						System.out.println("Element is-->"+ ev.childNode(i));
//						//System.out.println("Element is-->"+ ev.select("td").select("a").attr("href").substring(1));
//					//Elements node = ev.select("td");
//						i++;
//					}	
					
					//System.out.println("************" + i);
					
				}
			}
			//System.out.println("_______________________");
		}
		
		
	}
	
	public void calculateRelationships(String key, List<Node> nodeList) {
		//System.out.println("The key is-->" + key);
		
		//System.out.println("The list is-->" + nodeList);
		Relationships relationships = new Relationships();
		relationships.setStartNode(key);
		//System.out.println("The key is-->" + key);
		String startNodeData = (String) getDiagramNode(key).getName();
		//System.out.println("The corresponing diagram node name is-->"+ startNodeData);
		
		relationships.setStartNodeData(startNodeData);
		//System.out.println("end node data " + relationships.getEndNodeData());
		
		int i = 0;
		for(Node node : nodeList) {
			//System.out.println((Element)node);
			Element element = (Element)node;
			if(element.getElementsByTag("td").toString().contains("td")){
				//System.out.println("td "+ element.getElementsByTag("td").text());
				if(element.select("td").toString().contains("href") && i == 2) {
					//System.out.println("Iteration-->" + i);
					//System.out.println("The id is-->"+element.select("td").select("a").attr("href").substring(1));
					String endNode = element.select("td").select("a").attr("href").substring(1);
//					System.out.println("end node id is"+endNode +"startNOde " + key );
//					
//					System.out.println("END NODE LOGIC");
//					System.out.println(getDiagramNode(endNode));
//					
//					System.out.println("START NODE LOGIC");
//					System.out.println(getDiagramNode(key));
					
					relationships.setEndNode(endNode);
					//System.out.println("The data is -->" + element.getElementsByTag("td").text());
					relationships.setEndNodeData(element.getElementsByTag("td").text());
				}
				else {
					//System.out.println("Iteration-->" + i);
					if(i == 0) {
						//System.out.println("goes in role header1 "+"td "+ element.getElementsByTag("td").text());
						relationships.setInRoleHeader1(element.getElementsByTag("td").text());
					}
					
					else if (i == 3) {
						//System.out.println("goes in role header2 "+ element.getElementsByTag("td").text());
						relationships.setInRoleHeader2(element.getElementsByTag("td").text());
					}
					
				}
			}
			i++;
		}
		if(relationships.getEndNode()!= null && relationships.getEndNodeData() != null) {
			relationshipsList.add(relationships);
		}
		
		//String testKey = "3_20262";
		//System.out.println("TEST "+getDiagramNode(testKey));
		//System.out.println(relationships);
		//System.out.println("*************************");
	}
	
	
	//@SuppressWarnings("unchecked")

	private void rowCounterDebugFunc() {
		System.out.println("Rows before filtering-->" + beforeCounter);
		System.out.println("******************");
		System.out.println("Rows after filtering-->"+ afterCounter);
	}
	
	
	private void displayRelatiosnhips() {
//		for(DiagramNode<E> d : diagramNodesList) {
//			System.out.println(d);
//		}
		for(Relationships rel: relationshipsList) {
			//System.out.println("Start Node "+ rel.getStartNode() + "Start Node Name " + getDiagramNode(rel.getStartNode()).getName());
			//System.out.println("End Node "+ rel.getEndNode() + "End Node Name " + getDiagramNode(rel.getEndNode()).getName());
			//System.out.println("-------------------------------------------");
			
			String startNodeID =  rel.getStartNode();
			String endNodeID = rel.getEndNode();
			
			//System.out.println("start id " + startNodeID + "end id " + endNodeID);
			
			//System.out.println(">"+ rel.getStartNodeData() +"-->"+ rel.getEndNodeData());
			System.out.println("Start Node " + getDiagramNode(startNodeID) + "End Node " + getDiagramNode(endNodeID));
			//startNodeID = endNodeID;
			//getDiagramNode(endNodeID);
			//System.out.println( + "End Node id " + endNodeID);
			
			//System.out.println("end Node "+ endNodeID);
			System.out.println("*************");
		}
	}
	
	private DiagramNode<E> getDiagramNode(String nodeID){
		
		//System.out.println("DEBUG 1"+ nodeID);
		DiagramNode<E> diagramNode = null;
		for(DiagramNode<E> d : diagramNodesList) {
			//System.out.println(d);
			if(d.getId().equals(nodeID)) {
				diagramNode = d;
			}
		}
		
		return diagramNode;	
	}
	
//	public void displayNodeList() {
//		for(DiagramNode<E> nodes: diagramNodesList) {
//			System.out.println(nodes);
//		}
//	}
// 	
//	public void displayEdgeList() {
//		for(DiagramEdge<E> edges: diagramEdgesList) {
//			System.out.println(edges);
//		}
//	}
}
