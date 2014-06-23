package com.peacox.recommender.messages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class Messages {
	
	OntModel ontology;
    String NS = "http://gumo.org/2.0/" + "#";
    String BASE_NS = "http://gumo.org/2.0/";
	
	public Messages(){
		String fileName = "gumo.owl";
		BufferedReader reader = null;		
		reader = new BufferedReader(new InputStreamReader(Messages.class.
		    	getClassLoader().
		    	getResourceAsStream(fileName)));		
        ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontology.read(reader, null, "RDF/XML");
	}
	
	public String getMessageForWalk(){
		String result = "";
		
		String additionalQuery = " { ?resource gumo:typeOf 'http://gumo.org/2.0/Walk'} ";
		
		List<String> messages;
		messages = getMessage(additionalQuery);
        
		result = messages.get(0);
		
		return result;
	}
	
	public String getMessageForCar(){
		String result = "";
		
		String additionalQuery = " { ?resource gumo:typeOf 'http://gumo.org/2.0/Car'} ";
		
		List<String> messages;
		messages = getMessage(additionalQuery);
        
		result = messages.get(0);
		
		return result;
	}
	
	List<String> getMessage(String additionalQuery){
		
		String queryString =        
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
                  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
                  "PREFIX gumo: <http://gumo.org/2.0/>  "+
                  //"PREFIX typeOf: <http://gumo.org/2.0/typeOf>  "+
                  "PREFIX suggestion: <http://gumo.org/2.0/Suggestion> " +
                  "PREFIX messages: <http://gumo.org/2.0/Messages> " +
                  "select DISTINCT ?resource "+
                  "where { "+
                  	//"?resource text:query (ubis:identifier " + "830242" + ")" +
                  	" {?resource rdf:type gumo:MessageTypes }" +
                  	//" ?resource gumo:typeOf 'http://gumo.org/2.0/Suggestion' " +
                  	" . " +
                  	additionalQuery +        
                   //" OPTIONAL { ?interest rdfs:Class gumo:Peacox }" +
                  //" ?interest rdfs:subClassOf ?env " +
                   //?ability  "+
                  //"FILTER (?env = gumo:' Environmental Topics ') "+
                  "} \n ";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        
        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, ontology);
        com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
        // Output query results    
        //ResultSetFormatter.out(System.out, results, query);
        System.out.println("going to display the results: ");
        DatatypeProperty  dataProperty = ontology.getDatatypeProperty(BASE_NS + "messagetext");
        List<String> messages = new ArrayList();
        while(results.hasNext()){

    	 QuerySolution binding = results.nextSolution();                     
    	 Resource message = binding.getResource("resource");
    	 String messageText = message.getProperty(dataProperty).getString();
    	 System.out.println("found result: " + messageText);
    	 messages.add(messageText);
    	 //System.out.println(" is named "+label.getString());
    	}
        
        qe.close();
		return messages;
	}
	
	
	public String getMessage ( 
			String[] userMap, 
			String[] contextMap,
			String[] messagesMap,
			String[] transportationModesMap,
			String[] contextMapSubClass) {
		
		
        
        String additionalQuery = "";
        if (userMap != null){ 
        	int length = userMap.length;
        	int i = 0;
	        for (String entry : userMap){
	        	i++;
	        	System.out.println("userMap: " + entry);
	        	String queryString =        
	                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
	                      "PREFIX gumo: <http://gumo.org/2.0/>  "+  
	                      "PREFIX messages: <http://gumo.org/2.0/Messages> " +
	                      "PREFIX ubis: <http://ubisworld.org/documents/ubis.rdf#> " +
	                      "select ?resource "+
	                      "where { "+
	                      	"{ ?resource ubis:identifier ' " + entry + " ' }" +
	                        " UNION " +
	                        "{ ?resource ubis:identifier '" + entry + "' }" +
	                      "} \n ";
	        	Query query = QueryFactory.create(queryString);
	            
	            // Execute the query and obtain results
	        	QueryExecution qe = QueryExecutionFactory.create(query, ontology);
	        	com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
	            // Output query results    
	            //ResultSetFormatter.out(System.out, results, query);
	            System.out.println("going to display the results: ");        
	            while(results.hasNext()){
	
	        	 QuerySolution binding = results.nextSolution();                     
	        	 Resource message = binding.getResource("resource");    	     	
	        	 System.out.println("userMap: " + message.getURI());
	        	 additionalQuery += " { ?resource gumo:typeOf '" + message.getURI() + "' } ";        	 
	        	 //System.out.println(" is named "+label.getString());
	        	 if (i != length){
		       	 		additionalQuery += "UNION";
		            }
	        	 else{
	        		 additionalQuery += ".";
	        	 }
	        	}
	        }
        }
        
        if (contextMap != null){ 
        	int length = contextMap.length;
        	int i = 0;
	        for (String entry : contextMap){
	        	i++;
	        	System.out.println("contextMap: " + entry);
	        	String queryString =        
	                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
	                      "PREFIX gumo: <http://gumo.org/2.0/>  "+  
	                      "PREFIX messages: <http://gumo.org/2.0/Messages> " +
	                      "PREFIX ubis: <http://ubisworld.org/documents/ubis.rdf#> " +
	                      "select ?resource "+
	                      "where { "+
	                      	"{ ?resource ubis:identifier ' " + entry + " ' }" +
	                        " UNION " +
	                        "{ ?resource ubis:identifier '" + entry + "' }" +
	                      "} \n ";
	        	Query query = QueryFactory.create(queryString);
	            
	            // Execute the query and obtain results
	        	QueryExecution qe = QueryExecutionFactory.create(query, ontology);
	        	com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
	            // Output query results    
	            //ResultSetFormatter.out(System.out, results, query);
	            System.out.println("going to display the results: ");        
	            while(results.hasNext()){
	
	        	 QuerySolution binding = results.nextSolution();                     
	        	 Resource message = binding.getResource("resource");    	     	
	        	 System.out.println("contextMap: " + message.getURI());
	        	 additionalQuery += " { ?resource gumo:typeOf '" + message.getURI() + "' } ";
	        	 //System.out.println(" is named "+label.getString());
	        	 if (i != length){
		       	 		additionalQuery += "UNION";
		            }
	        	 else{
	        		 additionalQuery += ".";
	        	 }
	        	}
	        }
        }
        
        if (contextMapSubClass != null){ 
        	System.out.println("contextMapSubClass is not null");
        	int length = contextMapSubClass.length;
        	int i = 0;
	        for (String entry : contextMapSubClass){
	        	i++;
	        	System.out.println("contextMapSubClass: " + entry);
	        	String queryString =        
	                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
	                      "PREFIX gumo: <http://gumo.org/2.0/>  "+  
	                      "PREFIX messages: <http://gumo.org/2.0/Messages> " +
	                      "PREFIX ubis: <http://ubisworld.org/documents/ubis.rdf#> " +
	                      "select ?resource "+
	                      "where { "+
	                      	"{ ?resource ubis:identifier ' " + entry + " ' }" +
	                        " UNION " +
	                        "{ ?resource ubis:identifier '" + entry + "' }" +
	                      "} \n ";
	        	Query query = QueryFactory.create(queryString);
	            
	            // Execute the query and obtain results
	        	QueryExecution qe = QueryExecutionFactory.create(query, ontology);
	        	com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
	            // Output query results    
	            //ResultSetFormatter.out(System.out, results, query);
	            System.out.println("going to display the results: ");        
	            while(results.hasNext()){
	
	        	 QuerySolution binding = results.nextSolution();                     
	        	 Resource message = binding.getResource("resource");    	     	
	        	 System.out.println("transportationModesMap: " + message.getURI());
	        	 additionalQuery += "{ ?resource gumo:typeOf '" + message.getURI() + "' } ";
	        	 //System.out.println(" is named "+label.getString());
	        	 if (i != length){
		       	 		additionalQuery += "UNION";
		            }
	        	 else{
	        		 additionalQuery += ".";
	        	 }
	        	}
	        }
        }
        
        if (transportationModesMap != null){ 
        	int length = transportationModesMap.length;
        	int i = 0;
	        for (String entry : transportationModesMap){
	        	i++;
	        	System.out.println("transportationModesMap: " + entry);
	        	String queryString =        
	                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
	                      "PREFIX gumo: <http://gumo.org/2.0/>  "+  
	                      "PREFIX messages: <http://gumo.org/2.0/Messages> " +
	                      "PREFIX ubis: <http://ubisworld.org/documents/ubis.rdf#> " +
	                      "select ?resource "+
	                      "where { "+
	                      	"{ ?resource ubis:identifier ' " + entry + " ' }" +
	                        " UNION " +
	                        "{ ?resource ubis:identifier '" + entry + "' }" +
	                      "} \n ";
	        	Query query = QueryFactory.create(queryString);
	            
	            // Execute the query and obtain results
	        	QueryExecution qe = QueryExecutionFactory.create(query, ontology);
	        	com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
	            // Output query results    
	            //ResultSetFormatter.out(System.out, results, query);
	            System.out.println("going to display the results: ");        
	            while(results.hasNext()){
	
	        	 QuerySolution binding = results.nextSolution();                     
	        	 Resource message = binding.getResource("resource");    	     	
	        	 System.out.println("transportationModesMap: " + message.getURI());
	        	 additionalQuery += " { ?resource gumo:typeOf '" + message.getURI() + "'} ";
	        	 //System.out.println(" is named "+label.getString());
	        	 if (i != length){
		       	 		additionalQuery += "UNION";
		            }
	        	 else{
	        		 additionalQuery += ".";
	        	 }
	        	}
	        }
        }
        
        if (messagesMap != null){ 
        	//additionalQuery += " UNION { ";
        	int length = messagesMap.length;
        	int i = 0;
	        for (String entry : messagesMap){
	        	i++;
	        	System.out.println("messagesMap: " + entry);
	        	String queryString =        
	                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
	                      "PREFIX gumo: <http://gumo.org/2.0/>  "+  
	                      "PREFIX messages: <http://gumo.org/2.0/Messages> " +
	                      "PREFIX ubis: <http://ubisworld.org/documents/ubis.rdf#> " +
	                      "select ?resource "+
	                      "where { "+
	                      	"{ ?resource ubis:identifier ' " + entry + " ' }" +
	                        " UNION " +
	                        "{ ?resource ubis:identifier '" + entry + "' }" +
	                      "} \n ";
	        	Query query = QueryFactory.create(queryString);
	            
	            // Execute the query and obtain results
	        	QueryExecution qe = QueryExecutionFactory.create(query, ontology);
	        	com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
	            // Output query results    
	            //ResultSetFormatter.out(System.out, results, query);
	            System.out.println("going to display the results: "); 
	            
	            while(results.hasNext()){
	
	        	 QuerySolution binding = results.nextSolution();                     
	        	 Resource message = binding.getResource("resource");    	     	
	        	 System.out.println("messagesMap: " + message.getURI());
	        	 additionalQuery += " { ?resource gumo:typeOf  '" + message.getURI() + "' } ";
	        	 
	        	 //System.out.println(" is named "+label.getString());
	        	 if (i != length ){
		       	 		additionalQuery += "UNION";
		            }
	        	}
	            
	        }
	        //additionalQuery += "  }";
        }
        //additionalQuery = " UNION {?resource rdf:type 'http://gumo.org/2.0/Rewards' . ?resource rdf:type 'http://gumo.org/2.0/Authority' .} ";
        System.out.println(additionalQuery);
        String dot = "";
        if (additionalQuery.length() > 0){
        	dot = " . " ;
        }
        String queryString =        
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
                  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
                  "PREFIX gumo: <http://gumo.org/2.0/>  "+
                  //"PREFIX typeOf: <http://gumo.org/2.0/typeOf>  "+
                  "PREFIX suggestion: <http://gumo.org/2.0/Suggestion> " +
                  "PREFIX messages: <http://gumo.org/2.0/Messages> " +
                  "select DISTINCT ?resource "+
                  "where { "+
                  	//"?resource text:query (ubis:identifier " + "830242" + ")" +
                  	" {?resource rdf:type gumo:MessageTypes }" +
                  	//" ?resource gumo:typeOf 'http://gumo.org/2.0/Suggestion' " +
                  	dot + 
                  	additionalQuery +
                   //" OPTIONAL { ?interest rdfs:Class gumo:Peacox }" +
                  //" ?interest rdfs:subClassOf ?env " +
                   //?ability  "+
                  //"FILTER (?env = gumo:' Environmental Topics ') "+
                  "} \n ";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        
        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, ontology);
        com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();
        // Output query results    
        //ResultSetFormatter.out(System.out, results, query);
        System.out.println("going to display the results: ");
        DatatypeProperty  dataProperty = ontology.getDatatypeProperty(BASE_NS + "messagetext");
        List<String> messages = new ArrayList();
        while(results.hasNext()){

    	 QuerySolution binding = results.nextSolution();                     
    	 Resource message = binding.getResource("resource");
    	 String messageText = message.getProperty(dataProperty).getString();
    	 System.out.println("found result: " + messageText);
    	 messages.add(messageText);
    	 //System.out.println(" is named "+label.getString());
    	}
        
        qe.close();
        
        return messages.get(0);
		
	}

}
