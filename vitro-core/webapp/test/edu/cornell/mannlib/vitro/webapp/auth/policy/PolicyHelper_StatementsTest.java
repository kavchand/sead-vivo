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

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyStatementAction;

/**
 * Test the function of PolicyHelper in authorizing statements and models.
 */
public class PolicyHelper_StatementsTest extends AbstractTestClass {
	private static final String APPROVED_SUBJECT_URI = "test://approvedSubjectUri";
	private static final String APPROVED_PREDICATE_URI = "test://approvedPredicateUri";
	private static final String UNAPPROVED_PREDICATE_URI = "test://bogusPredicateUri";
	private static final String APPROVED_OBJECT_URI = "test://approvedObjectUri";

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;
	private OntModel ontModel;

	@Before
	public void setup() {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		setLoggerLevel(ServletPolicyList.class, Level.WARN);
		ServletPolicyList.addPolicy(ctx, new MySimplePolicy());
	}

	// ----------------------------------------------------------------------
	// The tests.
	// ----------------------------------------------------------------------

	@Test
	public void addNullStatement() {
		assertEquals("null statement", false,
				PolicyHelper.isAuthorizedToAdd(req, null, ontModel));
	}

	@Test
	public void addStatementWithNullRequest() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("null request", false,
				PolicyHelper.isAuthorizedToAdd(null, stmt, ontModel));
	}

	@Test
	public void addStatementToNullModel() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt, null));
	}

	@Test
	public void addAuthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void addAuthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void addUnauthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void addUnauthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void dropNullStatement() {
		assertEquals("null statement", false, PolicyHelper.isAuthorizedToDrop(
				req, (Statement) null, ontModel));
	}

	@Test
	public void dropStatementWithNullRequest() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("null request", false,
				PolicyHelper.isAuthorizedToDrop(null, stmt, ontModel));
	}

	@Test
	public void dropStatementFromNullModel() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt, null));
	}

	@Test
	public void dropAuthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	@Test
	public void dropAuthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	@Test
	public void dropUnauthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	@Test
	public void dropUnauthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/** Build a data statement. */
	private Statement dataStatement(String subjectUri, String predicateUri) {
		Resource subject = ontModel.createResource(subjectUri);
		Property predicate = ontModel.createProperty(predicateUri);
		return ontModel.createStatement(subject, predicate, "whoCares?");
	}

	/** Build a object statement. */
	private Statement objectStatement(String subjectUri, String predicateUri,
			String objectUri) {
		Resource subject = ontModel.createResource(subjectUri);
		Resource object = ontModel.createResource(objectUri);
		Property predicate = ontModel.createProperty(predicateUri);
		return ontModel.createStatement(subject, predicate, object);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class MySimplePolicy implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			if (whatToAuth instanceof AbstractDataPropertyStatementAction) {
				return isAuthorized((AbstractDataPropertyStatementAction) whatToAuth);
			} else if (whatToAuth instanceof AbstractObjectPropertyStatementAction) {
				return isAuthorized((AbstractObjectPropertyStatementAction) whatToAuth);
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision isAuthorized(
				AbstractDataPropertyStatementAction whatToAuth) {
			if ((APPROVED_SUBJECT_URI.equals(whatToAuth.getSubjectUri()))
					&& (APPROVED_PREDICATE_URI.equals(whatToAuth
							.getPredicateUri()))) {
				return authorized();
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision isAuthorized(
				AbstractObjectPropertyStatementAction whatToAuth) {
			if ((APPROVED_SUBJECT_URI.equals(whatToAuth.getSubjectUri()))
					&& (APPROVED_PREDICATE_URI.equals(whatToAuth
							.getPredicateUri()))
					&& (APPROVED_OBJECT_URI.equals(whatToAuth.getObjectUri()))) {
				return authorized();
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision authorized() {
			return new BasicPolicyDecision(Authorization.AUTHORIZED, "");
		}

		private PolicyDecision inconclusive() {
			return new BasicPolicyDecision(Authorization.INCONCLUSIVE, "");
		}
	}

}
