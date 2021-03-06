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

package stubs.edu.cornell.mannlib.vitro.webapp.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * TODO
 */
public class SelfEditingConfigurationStub extends SelfEditingConfiguration {

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private Map<String, List<Individual>> associatedIndividuals = new HashMap<String, List<Individual>>();

	public SelfEditingConfigurationStub() {
		super("bogusMatchingProperty");
	}

	public void addAssociatedIndividual(String externalAuthId,
			Individual individual) {
		if (!associatedIndividuals.containsKey(externalAuthId)) {
			associatedIndividuals.put(externalAuthId,
					new ArrayList<Individual>());
		}
		associatedIndividuals.get(externalAuthId).add(individual);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public List<Individual> getAssociatedIndividuals(IndividualDao indDao,
			String externalAuthId) {
		if (associatedIndividuals.containsKey(externalAuthId)) {
			return associatedIndividuals.get(externalAuthId);
		} else {
			return Collections.emptyList();
		}
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.isConfigured() not implemented.");
	}

	@Override
	public String getMatchingPropertyUri() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.getMatchingPropertyUri() not implemented.");
	}

	@Override
	public List<Individual> getAssociatedIndividuals(IndividualDao indDao,
			UserAccount user) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.getAssociatedIndividuals() not implemented.");
	}

	@Override
	public void associateIndividualWithUserAccount(IndividualDao indDao,
			DataPropertyStatementDao dpsDao, UserAccount user,
			String associatedIndividualUri) {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"SelfEditingConfigurationStub.associateIndividualWithUserAccount() not implemented.");
	}

}
