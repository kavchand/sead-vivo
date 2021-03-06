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
package edu.cornell.mannlib.vitro.webapp.visualization.temporalgraph;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryFieldLabels;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Entity;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.GenericQueryMap;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.GenericQueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.QueryRunner;

public class OrganizationUtilityFunctions {

	public static String getHighestLevelOrganizationURI(ResultSet resultSet,
			Map<String, String> fieldLabelToOutputFieldLabel) {

		GenericQueryMap queryResult = new GenericQueryMap();

		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();

			RDFNode organizationNode = solution
					.get(fieldLabelToOutputFieldLabel.get("organization"));

			if (organizationNode != null) {
				queryResult.addEntry(
						fieldLabelToOutputFieldLabel.get("organization"),
						organizationNode.toString());

				return organizationNode.toString();

			}

			RDFNode organizationLabelNode = solution
					.get(fieldLabelToOutputFieldLabel.get("organizationLabel"));

			if (organizationLabelNode != null) {
				queryResult.addEntry(
						fieldLabelToOutputFieldLabel.get("organizationLabel"),
						organizationLabelNode.toString());
			}

			RDFNode numberOfChildrenNode = solution.getLiteral("numOfChildren");

			if (numberOfChildrenNode != null) {
				queryResult.addEntry("numOfChildren", String
						.valueOf(numberOfChildrenNode.asLiteral().getInt()));
			}
		}

		return "";
	}

	public static String getHighestLevelOrganizationURI(Log log, Dataset dataset)
			throws MalformedQueryParametersException {
		
		Map<String, String> fieldLabelToOutputFieldLabel = new HashMap<String, String>();
		fieldLabelToOutputFieldLabel.put("organization",
				QueryFieldLabels.ORGANIZATION_URL);
		fieldLabelToOutputFieldLabel.put("organizationLabel",
				QueryFieldLabels.ORGANIZATION_LABEL);

		String aggregationRules = "(count(?organization) AS ?numOfChildren)";

		String whereClause = "?organization rdf:type foaf:Organization ;" 
						+ " rdfs:label ?organizationLabel . \n"
				+ "OPTIONAL { ?organization core:hasSubOrganization ?subOrg } . \n"
				+ "OPTIONAL { ?organization core:subOrganizationWithin ?parent } . \n"
				+ "FILTER ( !bound(?parent) ). \n";

		String groupOrderClause = "GROUP BY ?organization ?organizationLabel \n"
				+ "ORDER BY DESC(?numOfChildren)\n" + "LIMIT 1\n";

		QueryRunner<ResultSet> highestLevelOrganizationQueryHandler = new GenericQueryRunner(
				fieldLabelToOutputFieldLabel, aggregationRules, whereClause,
				groupOrderClause, dataset);

		String highestLevelOrgURI = OrganizationUtilityFunctions
				.getHighestLevelOrganizationURI(
						highestLevelOrganizationQueryHandler.getQueryResult(),
						fieldLabelToOutputFieldLabel);
		return highestLevelOrgURI;
	}
	
	public static String getEntityLabelFromDAO(VitroRequest vitroRequest,
			String entityURI) {
		
		IndividualDao iDao = vitroRequest.getWebappDaoFactory().getIndividualDao();
        Individual ind = iDao.getIndividualByURI(entityURI);
        
        String organizationLabel = "Unknown Organization"; 
        
        if (ind != null) {
        	organizationLabel = ind.getName();
        }
		return organizationLabel;
	}
	
	public static String getStaffProvidedOrComputedHighestLevelOrganization(Log log,
			Dataset dataset, VitroRequest vitroRequest)
			throws MalformedQueryParametersException {
		
		String staffProvidedHighestLevelOrganization = ConfigurationProperties.getBean(vitroRequest)
					.getProperty("visualization.topLevelOrg");
		
		/*
		 * First checking if the staff has provided highest level organization in deploy.properties
		 * if so use to temporal graph vis.
		 */
		if (StringUtils.isNotBlank(staffProvidedHighestLevelOrganization)) {
			
			/*
			 * To test for the validity of the URI submitted.
			 */
			IRIFactory iRIFactory = IRIFactory.jenaImplementation();
			IRI iri = iRIFactory.create(staffProvidedHighestLevelOrganization);
		    
			
			if (!iri.hasViolation(false)) {
		    	return staffProvidedHighestLevelOrganization;
		    }
		} 
		
		/*
		 * If the provided value was not proper compute it yourself.
		 * */
		return OrganizationUtilityFunctions.getHighestLevelOrganizationURI(log, dataset);
	}
	
	public static Entity mergeEntityIfShareSameURI(Entity entityA, Entity entityB) {
		
		if (StringUtils.equalsIgnoreCase(entityA.getEntityURI(), entityB.getEntityURI())) {
			
			Entity mergedEntity = new Entity(entityA.getEntityURI());
			
			if (StringUtils.isNotBlank(entityA.getEntityLabel())) {
				
				mergedEntity.setEntityLabel(entityA.getEntityLabel());
				
			} else if (StringUtils.isNotBlank(entityB.getEntityLabel())) {
				
				mergedEntity.setEntityLabel(entityB.getEntityLabel());
			}
			
			mergedEntity.addSubEntitities(entityA.getSubEntities());
			mergedEntity.addSubEntitities(entityB.getSubEntities());
			
			mergedEntity.addParents(entityA.getParents());
			mergedEntity.addParents(entityB.getParents());
			
			return mergedEntity;
			
		} else {
			return null;
		}
		
	}
	
}
