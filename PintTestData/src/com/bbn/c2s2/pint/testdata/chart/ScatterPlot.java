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

package com.bbn.c2s2.pint.testdata.chart;

import java.awt.RenderingHints;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ScatterPlot extends ApplicationFrame {

	private static final int DOT_SIZE = 5;
	
	private static final long serialVersionUID = 3837293727083887744L;
	private double[][] data;
	private JFreeChart chart;

	public ScatterPlot(String chartTitle, String domainAxisTitle,
			String rangeAxisTitle, double[][] data) {
		super(chartTitle);
		this.data = data;
		final NumberAxis domainAxis = new NumberAxis(domainAxisTitle);
		domainAxis.setAutoRangeIncludesZero(false);
		final NumberAxis rangeAxis = new NumberAxis(rangeAxisTitle);
		rangeAxis.setAutoRangeIncludesZero(false);
		XYDataset dataSet = getDataSet(data);
		XYItemRenderer renderer = getRenderer();
		final XYPlot plot = new XYPlot(dataSet, domainAxis, rangeAxis, renderer);
//		plot.
//		final FastScatterPlot plot = new FastScatterPlot(data, domainAxis,
//				rangeAxis);
//		DrawingSupplier supplier = new ModifiedDrawingSupplier(5.0);
//		plot.setDrawingSupplier(supplier);
		chart = new JFreeChart(chartTitle, plot);
		chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		chart.removeLegend();

		
		final ChartPanel panel = new ChartPanel(chart, true);
		panel.setPreferredSize(new java.awt.Dimension(500, 270));
		panel.setMinimumDrawHeight(10);
		panel.setMaximumDrawHeight(2000);
		panel.setMinimumDrawWidth(20);
		panel.setMaximumDrawWidth(2000);

		setContentPane(panel);
	}

	/**
	 * @return
	 */
	private XYItemRenderer getRenderer() {
		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setDotHeight(DOT_SIZE);
		renderer.setDotWidth(DOT_SIZE);	
		return renderer;
	}

	/**
	 * @param data2
	 * @return
	 */
	private XYDataset getDataSet(double[][] data) {
		DefaultXYDataset retSet = new DefaultXYDataset();
		Comparable first = new Integer(1);
		retSet.addSeries(first, data);
		return retSet;
	}

	public void toScreen() {
		this.pack();
		RefineryUtilities.centerFrameOnScreen(this);
		this.setVisible(true);
	}

	public void toJpgFile(String fileName, int xSize, int ySize)
			throws IOException {
		ChartUtilities.saveChartAsJPEG(new File(fileName), chart, xSize, ySize);
	}

	public void toCsvFile(String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		for (int i = 0; i < data[0].length; i++) {
			String out = data[0][i] + "," + data[1][i];
			fw.write(out + '\n');
		}
		fw.close();
	}


	/**
	 * Populates the data array with random values.
	 */
	static double[][] populateData(int size) {

		double[][] data = new double[2][size];

		for (int i = 0; i < data[0].length; i++) {
			final float x = (float) i;
			data[0][i] = x;
			data[1][i] = (float) Math.random();
		}

		return data;

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		double[][] testData = populateData(50);
		ScatterPlot demo = new ScatterPlot("Fast Scatter Plot Demo", "X Label", "Y Label", testData);
		try {
			demo.toJpgFile("test-plot.jpg", 1000, 800);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		demo.toScreen();

	}

}
