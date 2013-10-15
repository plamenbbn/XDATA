/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp. All Rights Reserved.
 ******************************************************************************/
/* =============================================================================
 *
 *                  COPYRIGHT 2010 BBN Technologies Corp.
 *                  1300 North 17th Street, Suite 600
 *                       Arlington, VA  22209
 *                          (703) 284-1200
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * ==============================================================================
 */
package com.bbn.c2s2.pint;

import java.util.ArrayList;
import java.util.List;

import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class RnrmProcessElement {

	private Resource resource;
	private int id = -1;


	public RnrmProcessElement(Resource resource) {
		if (null == resource) {
			throw new IllegalArgumentException(
					"Cannot create an RnrmProcessElement with a null Resource");
		}
		this.resource = resource;
	}
	
	public RnrmProcessElement(Resource resource, int id) {
		if (null == resource) {
			throw new IllegalArgumentException(
					"Cannot create an RnrmProcessElement with a null Resource");
		}
		this.id = id;
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public int getId() {
		return id;
	}

	public boolean hasRdfType(Resource type) {
		return RDFHelper.hasRdfType(this.resource.getModel(), this.resource,
				type);
	}

	public String getLabel() {
		return RDFHelper.getLabel(this.resource.getModel(), this.resource
				.getURI());
	}

	public List<Resource> getObservables() {
		List<Resource> out = null;
		Activity a = this.getActivity();
		if (a == null)
			return out;
		Resource activity = this.resource.getModel().createResource(
				a.getActivityURI());
		StmtIterator i = this.resource.getModel().listStatements(activity,
				RNRM.hasObservable, (Resource) null);
		out = new ArrayList<Resource>();
		while (i.hasNext()) {
			out.add((Resource) i.nextStatement().getObject());
		}
		return out;
	}

	public Activity getActivity() {
		Activity rv = null;
		if (!this.hasRdfType(RNRM.ActivityElement))
			return rv;

		StmtIterator it = this.resource.getModel().listStatements(
				this.resource, RNRM.representsActivity, (RDFNode) null);

		Resource actResource = null;
		if (it.hasNext()) {
			actResource = it.nextStatement().getResource();

			rv = new Activity(this.id, actResource.getURI());
			rv.setLabel(RDFHelper.getLabel(this.resource.getModel(),
					actResource.getURI()));
		}
		return rv;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RnrmProcessElement other = (RnrmProcessElement) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}

}
