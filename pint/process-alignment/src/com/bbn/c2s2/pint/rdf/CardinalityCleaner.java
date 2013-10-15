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

package com.bbn.c2s2.pint.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.rdf.ProcessValidator.CardinalityViolation;
import com.bbn.c2s2.pint.rdf.ProcessValidator.CardinalityViolationType;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Identifies and removes cardinality violations within an RNRM Process
 * 
 * <pre>
 * Current rules for elements [ ElementType(in, out) ]:
 * ActivityElement(1,1)
 * Fork(1,2+)
 * Decision(1,2+)
 * Join(2+,1)
 * Merge(2+,1)
 * StartElement(0,1)
 * EndElement(1,0)
 * </pre>
 * 
 * @author tself
 * 
 */
public class CardinalityCleaner {
	private RdfProcessEditor editor;
	private RdfProcess process;
	protected static Logger logger = LoggerFactory
			.getLogger(CardinalityCleaner.class);


	private CardinalityCleaner(RdfProcess process) {
		this.process = process;
		editor = new RdfProcessEditor(process);
	}

	/**
	 * Identifies and attempts to remove cardinality violations within the RNRM
	 * process. The result is a process with 0 cardinality violations, even if
	 * it means removing all partial order from the process.
	 */
	public static void repairCardinality(RdfProcess process) {
		CardinalityCleaner cleaner = new CardinalityCleaner(process);

		CardinalityViolation cardViolation = ProcessValidator
		.getNextCardinalityViolation(process);
		while (null != cardViolation) {
			cleaner.fixViolation(cardViolation);
			cardViolation = ProcessValidator.getNextCardinalityViolation(process);
		}
		cleaner.getProcessEditor().finalizeProcess();
	}

	protected RdfProcessEditor getProcessEditor() {
		return editor;
	}
	
	protected void fixViolation(CardinalityViolation v) {
		switch (v.violation) {
		case UNNECESSARY_ELEMENT:
			editor.deleteFromPath(v.offendingProcessElement);
			break;
		case OVERPOPULATED_ACTIVITY:
			trimActivityArcs(v.offendingProcessElement);
			break;
		case OVERPOPULATED_CLOSING:
			trimBranchingElement(v.offendingProcessElement);
			break;
		case OVERPOPULATED_OPENING:
			trimBranchingElement(v.offendingProcessElement);
			break;
		default:
			editor.isolateElement(v.offendingProcessElement);
		}
	}

	protected void trimBranchingElement(Resource procElement) {
		List<Statement> incoming = process.getIncomingEdges(procElement);
		List<Statement> outgoing = process.getOutgoingEdges(procElement);
		Model model = process.getModel();
		if (logger.isWarnEnabled()) {
			logger.warn(String.format(
					"Trimming Branch Element Arcs for %1$s (%2$d,%3$d)...",
					RDFHelper.getLabel(model, procElement.getURI()), incoming
							.size(), outgoing.size()));
		}
		boolean madeChange = true;
		int arcCount = -1;
		if (RDFHelper.hasRdfType(model, procElement, RNRM.ForkElement)
				|| RDFHelper.hasRdfType(model, procElement,
						RNRM.DecisionElement)) {
			arcCount = incoming.size();
			while (arcCount > 1 && madeChange) {
				madeChange = editor
						.removeNonBreakingIncomingArc(procElement);
				if (madeChange) {
					incoming = process.getIncomingEdges(procElement);
					arcCount = incoming.size();
				}
			}
			if (arcCount > 1) {
				// could not fix. Isolate.
				editor.isolateElement(procElement);
			}
		} else if (RDFHelper.hasRdfType(model, procElement, RNRM.JoinElement)
				|| RDFHelper.hasRdfType(model, procElement, RNRM.MergeElement)) {
			arcCount = outgoing.size();
			while (arcCount > 1 && madeChange) {
				madeChange = editor
						.removeNonBreakingOutgoingArc(procElement);
				if (madeChange) {
					outgoing = process.getOutgoingEdges(procElement);
					arcCount = outgoing.size();
				}
			}
			if (arcCount > 1) {
				// could not fix. Isolate.
				editor.isolateElement(procElement);
			}
		}
		if (logger.isWarnEnabled()) {
			incoming = process.getIncomingEdges(procElement);
			outgoing = process.getOutgoingEdges(procElement);
			logger.warn(String.format("...trimmed to (%1$d,%2$d)", incoming
					.size(), outgoing.size()));
		}

	}

	protected void trimActivityArcs(Resource activity) {
		List<Statement> incoming = process.getIncomingEdges(activity);
		List<Statement> outgoing = process.getOutgoingEdges(activity);

		if (logger.isWarnEnabled()) {
			logger.warn(String.format(
					"Trimming Activity Arcs for %1$s (%2$d,%3$d)...", RDFHelper
							.getLabel(process.getModel(), activity
									.getURI()), incoming.size(), outgoing
							.size()));
		}
		boolean madeChange = true;
		int arcCount = incoming.size();
		while (arcCount > 1 && madeChange) {
			madeChange = editor.removeNonBreakingIncomingArc(activity);
			if (madeChange) {
				incoming = process.getIncomingEdges(activity);
				arcCount = incoming.size();
			}
		}
		if (arcCount > 1) {
			// could not fix it. Isolate
			editor.isolateElement(activity);
		} else {
			arcCount = outgoing.size();
			madeChange = true;
			while (arcCount > 1 && madeChange) {
				madeChange = editor
						.removeNonBreakingOutgoingArc(activity);
				if (madeChange) {
					outgoing = process.getOutgoingEdges(activity);
					arcCount = outgoing.size();
				}
			}
			if (arcCount > 1) {
				// could not fix it. Isolate.
				editor.isolateElement(activity);
			}
		}
		if (logger.isWarnEnabled()) {
			incoming = process.getIncomingEdges(activity);
			outgoing = process.getOutgoingEdges(activity);
			logger.warn(String.format("...trimmed to (%1$d,%2$d)", incoming
					.size(), outgoing.size()));
		}
	}
}
