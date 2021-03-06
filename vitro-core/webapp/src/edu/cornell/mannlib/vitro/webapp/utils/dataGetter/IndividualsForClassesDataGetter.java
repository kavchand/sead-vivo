/*
Copyright (c) 2013, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;

/**
 * This will pass these variables to the template:
 * classGroupUri: uri of the classgroup associated with this page.
 * vClassGroup: a data structure that is the classgroup associated with this page.     
 */
public class IndividualsForClassesDataGetter extends DataGetterBase implements DataGetter{
    private static final Log log = LogFactory.getLog(IndividualsForClassesDataGetter.class);
    protected static String restrictClassesTemplateName = null;
    VitroRequest vreq;
    ServletContext context;
    String dataGetterURI;
    String classGroupURI;
    Map<String, Object> classIntersectionsMap;
    private final static String defaultTemplate =  "page-classgroup.ftl";

    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public IndividualsForClassesDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI){
        this.configure(vreq,displayModel,dataGetterURI);
    }   
    
    /**
     * Configure this instance based on the URI and display model.
     */
    protected void configure(VitroRequest vreq, Model displayModel, String dataGetterURI) {
    	if( vreq == null ) 
    		throw new IllegalArgumentException("VitroRequest  may not be null.");
        if( displayModel == null ) 
            throw new IllegalArgumentException("Display Model may not be null.");
        if( dataGetterURI == null )
            throw new IllegalArgumentException("PageUri may not be null.");
                
        this.vreq = vreq;
        this.context = vreq.getSession().getServletContext();
        this.dataGetterURI = dataGetterURI;   
        this.classGroupURI = DataGetterUtils.getClassGroupForDataGetter(displayModel, dataGetterURI);
        this.classIntersectionsMap = getClassIntersectionsMap(displayModel);
    }
    
    /**
     * Get the classes and classes to restrict by - if any
     */

    protected Map<String, Object> getClassIntersectionsMap(Model displayModel) {
    	  QuerySolutionMap initBindings = new QuerySolutionMap();
          initBindings.add("dataGetterUri", ResourceFactory.createResource(this.dataGetterURI));
          try {
	         QueryExecution qexec = QueryExecutionFactory.create( dataGetterQuery, displayModel , initBindings);
	     	 Map<String, Object> classesAndRestrictions = new HashMap<String, Object>();
	         List<String> classes = new ArrayList<String>();
	         displayModel.enterCriticalSection(Lock.READ);

	          try{
	              List<String>  restrictClasses = new ArrayList<String>();
	              HashMap<String, String> restrictClassesPresentMap = new HashMap<String, String>();
	              ResultSet resultSet = qexec.execSelect();        
	              while(resultSet.hasNext()){
	                  QuerySolution soln = resultSet.next();
	                  classes.add(DataGetterUtils.nodeToString(soln.get("class")));
	                  String restrictClass = DataGetterUtils.nodeToString(soln.get("restrictClass"));
	                  if(!restrictClass.isEmpty() && !restrictClassesPresentMap.containsKey(restrictClass)) {
	                  	restrictClasses.add(restrictClass);
	                  	restrictClassesPresentMap.put(restrictClass, "true");
	                  }
	              }
	              
	              if( classes.size() == 0 ){
	                  log.debug("No classes  defined in display model for "+ this.dataGetterURI);
	                  this.classIntersectionsMap =  null;
	              }
	              classesAndRestrictions.put("classes", classes);  
	              classesAndRestrictions.put("restrictClasses", restrictClasses);
	              return classesAndRestrictions;

	          }finally{
	              qexec.close();
	          }
	      }finally{
	          displayModel.leaveCriticalSection();
	      }
               
    }
    
    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) { 
        this.setTemplateName();
    	HashMap<String, Object> data = new HashMap<String,Object>();
        
        try{
        	List<String> classes = retrieveClasses(context, classIntersectionsMap);
        	List<String> restrictClasses = retrieveRestrictClasses(context, classIntersectionsMap);
        	log.debug("Retrieving classes for " + classes.toString() + " and restricting by " + restrictClasses.toString());
        	processClassesAndRestrictions(vreq, context, data, classes, restrictClasses);
        	 //Also add data service url
            //Hardcoding for now, need a more dynamic way of doing this
            data.put("dataServiceUrlIndividualsByVClass", this.getDataServiceUrl());
            //this is the class group associated with the data getter utilized for display on menu editing, not the custom one created
            data.put("classGroupUri",this.classGroupURI);
            //default template, overridden at page level if specified in display model
            data.put("bodyTemplate", defaultTemplate);
        } catch(Exception ex) {
        	log.error("An error occurred retrieving Vclass Intersection individuals", ex);
        }
             
        return data;
    }        
   
    protected void setTemplateName() {
    	this.restrictClassesTemplateName = "restricted";
    }
    
    
    protected List<String> retrieveClasses(
			ServletContext context, Map<String, Object> classIntersectionsMap) {
    	List<String> restrictClasses = (List<String>) classIntersectionsMap.get("classes");
		return restrictClasses;
	}    
    
    protected List<String> retrieveRestrictClasses(
			ServletContext context, Map<String, Object> classIntersectionsMap) {
    	List<String> restrictClasses = (List<String>) classIntersectionsMap.get("restrictClasses");
		return restrictClasses;
	}        
    
    protected void processClassesAndRestrictions(VitroRequest vreq, ServletContext context, 
    		HashMap<String, Object> data, List<String> classes, List<String> restrictClasses ) {
    	processClassesForDisplay(vreq, context, data, classes);
    	processRestrictionClasses(vreq, context, data, restrictClasses);
    	processIntersections(vreq, context, data);
    	
    }
    


	//At this point, data specifices whether or not intersections included
    private void processIntersections(VitroRequest vreq,
			ServletContext context, HashMap<String, Object> data) {
    	VClassGroup classesGroup = (VClassGroup) data.get("vClassGroup");
    	List<VClass> vclassList = classesGroup.getVitroClassList();
    	List<VClass> restrictClasses = (List<VClass>) data.get("restrictVClasses");
    	//if there are restrict classes, then update counts
    	if(restrictClasses.size() > 0) {
    		log.debug("Restriction classes exist");
    		List<VClass> newVClassList = new ArrayList<VClass>();
    		//Iterate through vclasses and get updated counts, iterated and saved in same order as initially included
    		for(VClass v: vclassList) {
    			int oldCount = v.getEntityCount();
    			//Making a copy so as to ensure we don't touch the values in the cache
    			VClass copyVClass = makeCopyVClass(v);
    			int count = retrieveCount(vreq, context, v, restrictClasses);
    			if(oldCount != count) {
    				log.debug("Old count was " + v.getEntityCount() + " and New count for " + v.getURI() + " is " + count);
    				copyVClass.setEntityCount(count);
    			} 
    			newVClassList.add(copyVClass);
    		}
    		classesGroup.setVitroClassList(newVClassList);
    		//TODO: Do we need to do this again or will this already be reset?
    		data.put("vClassGroup", classesGroup);
    	}
	}

    private VClass makeCopyVClass(VClass v) {
    	VClass copyVClass = new VClass(v.getURI());
		copyVClass.setLocalName(copyVClass.getLocalName());
		copyVClass.setDisplayRank(v.getDisplayRank());
		copyVClass.setName(v.getName());
		copyVClass.setNamespace(v.getNamespace());
		copyVClass.setEntityCount(v.getEntityCount());
		return copyVClass;
    }
    
    //update class count based on restrict classes
	private int retrieveCount(VitroRequest vreq, ServletContext context, VClass v, List<VClass> restrictClasses) {
		//Execute solr query that returns only count of individuals
		log.debug("Entity count is " + v.getEntityCount());
		List<String> classUris = new ArrayList<String>();
		classUris.add(v.getURI());
		for(VClass r: restrictClasses) {
			classUris.add(r.getURI());
		}
		long count =  DataGetterUtils.getIndividualCountForIntersection(vreq, context, classUris);
		return new Long(count).intValue();

	}

	private void processClassesForDisplay(VitroRequest vreq, ServletContext context, HashMap<String, Object> data, List<String> classes) {
    	VClassGroup classesGroup = new VClassGroup();
    	classesGroup.setURI("displayClasses");
    	log.debug("Processing classes that will be displayed");
    	List<VClass> vClasses = new ArrayList<VClass>();
  
    	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
    	for(String classUri: classes) {
    		//Retrieve vclass from cache to get the count
    		VClass vclass = vcgc.getCachedVClass(classUri);
    		//if not found in cache, possibly due to not being in any class group
			if(vclass == null) {
				vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(classUri);
			}
    		if(vclass != null) {
    			log.debug("VClass does exist for " + classUri + " and entity count is " + vclass.getEntityCount());
    			vClasses.add(vclass);
    		} else {
    			log.debug("Vclass " + classUri + " does not exist in the cache");
    			log.error("Error occurred, vclass does not exist for this uri " + classUri);
    			//Throw exception here
    		}
    	}
    	//Sort these classes
    	Collections.sort(vClasses);
    	log.debug("Sorting complete for V Classes");
    	classesGroup.setVitroClassList(vClasses);
    	log.debug("Returning vitro class list in data for template");
    	//Set vclass group
    	data.put("vClassGroup", classesGroup);
    }	
    
    private void processRestrictionClasses(VitroRequest vreq, ServletContext context, 
    		HashMap<String, Object> data, List<String> restrictClasses) {
    	try {
	    	VClassGroup restrictClassesGroup = new VClassGroup();
	    	restrictClassesGroup.setURI("restrictClasses");
	    	
	    	List<VClass> restrictVClasses = new ArrayList<VClass>();
	    	
	    	List<String> urlEncodedRestrictClasses = new ArrayList<String>();
	    	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);

	    	if(restrictClasses.size() > 0) {
	    		//classes for restriction are not displayed so don't need to include their class individual counts
	    		for(String restrictClassUri: restrictClasses) {
	    			//Also uses cache to remain consistent with process classes and also allow
	    			//vclasses to be returned even if switched to display model, although
	    			//uris used within display model editing and not vclass objects
	    			VClass vclass = vcgc.getCachedVClass(restrictClassUri);
	    			//if not found in cache, possibly due to not being in any class group
	    			if(vclass == null) {
	    				vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(restrictClassUri);
	    			}
	        		if(vclass != null) {
	        			log.debug("Found restrict class and adding to list " + restrictClassUri);
	        			restrictVClasses.add(vclass);
	        		} else {
	        			log.error("Error occurred, vclass does not exist for this uri " + restrictClassUri);
	        		}
	        		//Assuming utf-8?
	        		urlEncodedRestrictClasses.add(URLEncoder.encode(restrictClassUri, "UTF-8"));
	    		}
	    	
	    		restrictClassesGroup.setVitroClassList(restrictVClasses);
	    		restrictClassesGroup.setIndividualCount(restrictVClasses.size());
	    	} else {
	    		
	    	}
	    	String[] restrictClassesArray = new String[urlEncodedRestrictClasses.size()];
	    	restrictClassesArray = urlEncodedRestrictClasses.toArray(restrictClassesArray);
	    	
	    	//In case just want uris
	    	log.debug("Variable name for including restriction classes " +  getRestrictClassesTemplateName());
	    	data.put(getRestrictClassesTemplateName(), StringUtils.join(restrictClassesArray, ","));
	    	data.put("restrictVClasses", restrictVClasses);
	    	//not sure if this is useful
	    	data.put("restrictVClassGroup", restrictClassesGroup);
    	} catch(Exception ex) {
    		log.error("An error occurred in processing restriction classes ", ex);
    	}
    }
    
    public static VClassGroupTemplateModel getClassGroup(String classGroupUri, ServletContext context, VitroRequest vreq){
        
        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(context);
        List<VClassGroup> vcgList = vcgc.getGroups();
        VClassGroup group = null;
        for( VClassGroup vcg : vcgList){
            if( vcg.getURI() != null && vcg.getURI().equals(classGroupUri)){
                group = vcg;
                break;
            }
        }
        
        if( classGroupUri != null && !classGroupUri.isEmpty() && group == null ){ 
            /*This could be for two reasons: one is that the classgroup doesn't exist
             * The other is that there are no individuals in any of the classgroup's classes */
            group = vreq.getWebappDaoFactory().getVClassGroupDao().getGroupByURI(classGroupUri);
            if( group != null ){
                List<VClassGroup> vcgFullList = vreq.getWebappDaoFactory().getVClassGroupDao()
                    .getPublicGroupsWithVClasses(false, true, false);
                for( VClassGroup vcg : vcgFullList ){
                    if( classGroupUri.equals(vcg.getURI()) ){
                        group = vcg;
                        break;
                    }                                
                }
                if( group == null ){
                    log.error("Cannot get classgroup '" + classGroupUri + "'");
                    return null;
                }else{
                    setAllClassCountsToZero(group);
                }
            }else{
                log.error("classgroup " + classGroupUri + " does not exist in the system");
                return null;
            }            
        }
        
        return new VClassGroupTemplateModel(group);
    }
    
    public String getType(){
        return DataGetterUtils.generateDataGetterTypeURI(IndividualsForClassesDataGetter.class.getName());
    } 
    
    //Get data servuice
    public String getDataServiceUrl() {
    	return UrlBuilder.getUrl("/dataservice?getRenderedSolrIndividualsByVClass=1&vclassId=");
    }
    /**
     * For processig of JSONObject
     */
    public JSONObject convertToJSON(Map<String, Object> map, VitroRequest vreq) {
    	JSONObject rObj = DataGetterUtils.processVclassResultsJSON(map, vreq, true);
    	return rObj;
    }
    
    protected static void setAllClassCountsToZero(VClassGroup vcg){
        for(VClass vc : vcg){
            vc.setEntityCount(0);
        }
    }
    
    protected static String getAlphaParameter(VitroRequest request){
        return request.getParameter("alpha");
    }
    
    protected static int getPageParameter(VitroRequest request) {
        String pageStr = request.getParameter("page");
        if( pageStr != null ){
            try{
                return Integer.parseInt(pageStr);                
            }catch(NumberFormatException nfe){
                log.debug("could not parse page parameter");
                return 1;
            }                
        }else{                   
            return 1;
        }
    }
    
    //Get template parameter
    private static String getRestrictClassesTemplateName() {
    	return restrictClassesTemplateName;

    }
    
    private static final String prefixes = 
        "PREFIX rdf:   <" + VitroVocabulary.RDF +"> \n" +
        "PREFIX rdfs:  <" + VitroVocabulary.RDFS +"> \n" + 
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n";
    
    private static final String dataGetterQuery = 
    	prefixes + "\n" + 
   	 "SELECT ?class ?restrictClass WHERE {\n" +
        " ?dataGetterUri <" + DisplayVocabulary.GETINDIVIDUALS_FOR_CLASS + "> ?class . \n" +
        "    OPTIONAL {?dg <"+ DisplayVocabulary.RESTRICT_RESULTS_BY + "> ?restrictClass } .\n" +    
        "} \n" ;
}