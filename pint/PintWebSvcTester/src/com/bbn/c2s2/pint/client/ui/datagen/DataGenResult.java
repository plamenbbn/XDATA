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

package com.bbn.c2s2.pint.client.ui.datagen;

import java.util.List;

import javax.swing.JFrame;

import com.hp.hpl.jena.rdf.model.Model;

import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

/**
 * Stores the truth model and generated observations
 * 
 * @author tself
 * 
 */
public class DataGenResult {
	Model truthModel;
	List<SimpleObservation> observations;

	public DataGenResult(Model truthModel, List<SimpleObservation> observations) {
		this.truthModel = truthModel;
		this.observations = observations;
	}

	public void displayResults(JFrame frame) {
		DataGenResultsDialog dlg = new DataGenResultsDialog(frame, this);
	}

	public Model getTruthModel() {
		return truthModel;
	}

	public List<SimpleObservation> getObservations() {
		return observations;
	}

}
