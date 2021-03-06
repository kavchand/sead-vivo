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
package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JsonObject is used for creating data in JSON format, 
 * by just using the fields that are required to be included.
 * @author bkoniden
 * Deepak Konidena
 */
public class JsonObject {
	
	private String label;
	private String lastCachedAtDateTime;
	private List<List<Integer>> data = new ArrayList<List<Integer>>();
	private String entityURI;
	private String visMode;
	private List<String> organizationType = new ArrayList<String>();
	
	public List<String> getOrganizationTypes() {
		return organizationType;
	}

	public void setOrganizationTypes(List<String> organizationType) {
		this.organizationType = organizationType;
	}
	
	public void setOrganizationTypes(Set<String> givenOrganizationType) {
		for (String type : givenOrganizationType) {
			this.organizationType.add(type);
		}
	}

	public String getEntityURI() {
		return entityURI;
	}

	public void setEntityURI(String entityURI) {
		this.entityURI = entityURI;
	}

	public String getVisMode() {
		return visMode;
	}

	public void setVisMode(String visMode) {
		this.visMode = visMode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<List<Integer>> getYearToActivityCount() {
		return data;
	}

	public JsonObject(String label) {
		this.label = label;
	}
	
	public void setYearToActivityCount(List<List<Integer>> yearToPublicationCount) {
		this.data = yearToPublicationCount;
	}

	public void setLastCachedAtDateTime(String lastCachedAtDateTime) {
		this.lastCachedAtDateTime = lastCachedAtDateTime;
	}

	public String getLastCachedAtDateTime() {
		return lastCachedAtDateTime;
	}
}
