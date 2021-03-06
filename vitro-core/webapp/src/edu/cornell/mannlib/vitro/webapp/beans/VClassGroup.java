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

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.impl.URIImpl;

public class VClassGroup extends LinkedList <VClass> implements Comparable<VClassGroup> {
	
	private static final Log log = LogFactory.getLog(VClassGroup.class.getName());
	
    private String URI          = null;
    private String namespace    = null;
    private String localName    = null;
    private String publicName   = null;
    private int    displayRank  = -1;
    private int individualCount = -1;
    
    public boolean isIndividualCountSet(){
        return individualCount >= 0;
    }
    
    public int getIndividualCount() {
        return individualCount;
    }

    public void setIndividualCount(int individualCount) {
        this.individualCount = individualCount;
    }

    public int getDisplayRank() {
        return displayRank;
    }

    public void setDisplayRank(int displayRank) {
        this.displayRank = displayRank;
    }

    public VClassGroup(){
        super();
    }

    public VClassGroup(String uri, String name) {
        this(uri, name, 0);
    }

    public VClassGroup(String uri, String name, int rank) {
        super();
        this.URI = uri;
        URIImpl theURI = new URIImpl(uri);
        this.namespace = theURI.getNamespace();
        this.localName = theURI.getLocalName();
        this.displayRank = rank;
        this.publicName = name;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String ns) {
        this.namespace = ns;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String ln) {
        this.localName = ln;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String name) {
        this.publicName = name;
    }

    public void setVitroClassList(List<VClass> list)   {
        this.clear();
        this.addAll(list);
    }

    public List<VClass> getVitroClassList() {
        return this;
    }

    public boolean equals(Object obj) {
        if( obj instanceof VClassGroup ){
            VClassGroup other = (VClassGroup)obj;
            return other.getURI().equals(this.getURI());
        } else {
          return super.equals(obj);
        }
    }

    public int hashCode() {
        return this.getURI().hashCode();
    }

    public String toString() {
        return getPublicName() + " URI(" + getURI() + ") with " + size();
    }

    public static void removeEmptyClassGroups(Map groups){
        if( groups == null) return;

        List keysToRemove = new LinkedList();

        Iterator it = groups.keySet().iterator();
        while(it.hasNext()){
            Object key = it.next();
            Object grp = groups.get(key);
            if(grp != null && grp instanceof AbstractCollection){
                if( ((AbstractCollection)grp).isEmpty())
                    keysToRemove.add(key);
            }
        }

        it = keysToRemove.iterator();
        while(it.hasNext()){
            groups.remove(it.next());
        }
    }
    
    /**
     * Sorts VClassGroup objects by group rank, then alphanumeric.
     * @author bdc34 modified by jc55, bjl23
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second. 
     */
    public int compareTo(VClassGroup o2) {
    	Collator collator = Collator.getInstance();
        if (o2 == null) {
            log.error("object NULL in DisplayComparator()");
            return 0;
        }
        int diff = (this.getDisplayRank() - o2.getDisplayRank());
        if (diff == 0 ) {
            
            //put null public name classgrups at end of list
            if( this.getPublicName() == null ){
                if( o2.getPublicName() == null )
                    return 0; //or maybe collator.compare(this.getURI(),o2.getURI()) ???
                else
                    return 1;
            }else if ( o2.getPublicName() == null ){
                return -1;
            }else{
                return collator.compare(this.getPublicName(),o2.getPublicName());
            }
        }
        return diff;
    }
    
}
