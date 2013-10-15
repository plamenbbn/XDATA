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
package com.bbn.c2s2.pint.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.bbn.c2s2.ConfigLoader;
import com.bbn.c2s2.KMLGenerator;
import com.bbn.c2s2.pint.client.JhuToWsConversionFactory;
import com.bbn.c2s2.pint.client.PintToWsConversionFactory;
import com.bbn.c2s2.pint.client.WsToJhuConversionFactory;
import com.bbn.c2s2.pint.client.ui.datagen.DataGenDialog;
import com.bbn.c2s2.pint.client.ui.datagen.DataGenResult;
import com.bbn.c2s2.pint.configuration.Constants;
import com.bbn.c2s2.pint.dataGen.DataGenConfig;
import com.bbn.c2s2.pint.dataGen.DataSetGenerator;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.bbn.c2s2.util.ObservationLoader;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import edu.jhuapl.c2s2.pp.observation.Observation;
import edu.jhuapl.c2s2.processfinderenterprisebus.DetectedProcess;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class MainFrame implements ActionListener {
	private static final long serialVersionUID = 2820253495656257353L;
	private JFrame _frame;
	private JFileChooser _fileChooser;
	private ObservationInputPanel _obsPanel;
	private ResultsPanel _resPanel;
	private StatusBar _statusBar;
	private ObservationIndex _obsIndex;
	private Properties _pintParams;
	private JMenuItem _menuItemGenData;
	private RnrmWrapper rnrmWrapper;
	private DataGenConfig dataGenConfig;
	private String selectedProcessUri;

	// config property names
	public static final String KEY_DATA_DIR = "ui.default.data-dir";
	public static final String KEY_RNRM_URL = "ui.default.rnrm-url";

	private void initializeComponents() {
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (Exception e) {

		}
		dataGenConfig = new DataGenConfig();
		_pintParams = ConfigLoader.getInstance().getPintParameters();
		_frame = new JFrame();
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setTitle("PINT Test Client");
		_frame.setSize(1024, 768);
		_frame.setLayout(new BorderLayout());

		addMenus();

		// input view
		_obsPanel = new ObservationInputPanel(this);
		_frame.getContentPane().add(_obsPanel, BorderLayout.NORTH);

		// results view
		_resPanel = new ResultsPanel();
		_frame.getContentPane().add(_resPanel, BorderLayout.CENTER);

		// status bar
		_statusBar = new StatusBar();

		_frame.getContentPane().add(_statusBar, BorderLayout.SOUTH);

		// initialize file chooser
		String sCwd = _pintParams.getProperty(KEY_DATA_DIR);
		if (null == sCwd || sCwd.length() < 1) {
			sCwd = ".";
		}
		_fileChooser = new JFileChooser();
		File cwd = null;
		try {
			cwd = new File(sCwd).getCanonicalFile();
		} catch (IOException ioe) {
		}
		if (null != cwd) {
			_fileChooser.setCurrentDirectory(cwd);
		}
		_fileChooser.setFileFilter(new DataFileFilter());
	}

	private void addMenus() {
		JMenuBar menus = new JMenuBar();
		// file menu
		JMenu fileMenu = new JMenu("File");

		// Load RNRM
		JMenuItem menuItem = new JMenuItem("Load RNRM");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				performLoadRnrm();
			}
		});
		fileMenu.add(menuItem);

		// Load data
		menuItem = new JMenuItem("Load Observations File");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				performLoadDataFile();
			}
		});
		fileMenu.add(menuItem);

		// Generate data
		_menuItemGenData = new JMenuItem("Generate Observations");
		_menuItemGenData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				performGenerateObservations();
			}
		});
		_menuItemGenData.setEnabled(false);
		fileMenu.add(_menuItemGenData);

		// Save data file
		menuItem = new JMenuItem("Export Observations to Data File");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				performExportObservations();
			}
		});
		fileMenu.add(menuItem);

		// Save KML file
		menuItem = new JMenuItem("Export Observations to KML File");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				performExportKMLObservations();
			}
		});
		fileMenu.add(menuItem);

		menus.add(fileMenu);
		_frame.setJMenuBar(menus);
	}

	private void show() {
		if (null != _frame) {
			_frame.setVisible(true);
		}
	}

	private void performLoadRnrm() {
		boolean success = false;
		String sUrl = _pintParams.getProperty(KEY_RNRM_URL);
		String response = (String) JOptionPane.showInputDialog(_frame,
				"Enter the URL of the Red Nodal Reference Model",
				"RNRM Loader", JOptionPane.QUESTION_MESSAGE, null, null, sUrl);
		if (null != response) {
			success = _obsPanel.loadRnrm(response);
			rnrmWrapper = new RnrmWrapper(_obsPanel.getRnrmModel());
			// the following just forces it to load
			rnrmWrapper.getRdfProcesses();
		}
		if (success) {
			_statusBar.setRnrmUrl(response);
			_resPanel.setModel(_obsPanel.getRnrmModel());
			_menuItemGenData.setEnabled(true);
		}
	}

	public void setStatus(String msg) {
		_statusBar.setMessage(msg);
	}

	private void performGenerateObservations() {
		DataGenDialog dialog = new DataGenDialog(_frame, rnrmWrapper, dataGenConfig, selectedProcessUri);
		if (dialog.doGenerate()) {
			setStatus("Generating observations...");
			selectedProcessUri = dialog.getProcessUri();
			final String procUri = selectedProcessUri;
			dataGenConfig = dialog.getDataGenConfig();
			final DataGenConfig dConfig = dataGenConfig;
			SwingWorker<DataGenResult, Void> worker = new SwingWorker<DataGenResult, Void>() {

				@Override
				protected DataGenResult doInBackground()
						throws Exception {
					DataGenResult rv = null;
					List<SimpleObservation> observations = new ArrayList<SimpleObservation>();

					DataSetGenerator gen = new DataSetGenerator(rnrmWrapper.getModel(), procUri, dConfig);
					List<com.bbn.c2s2.pint.Observation> pintObsList = gen.generate(); 
					observations = new ArrayList<SimpleObservation>(PintToWsConversionFactory.convertObservations(pintObsList));
					_obsIndex = new ObservationIndex(observations);
					rv = new DataGenResult(gen.getTruthModel(), observations);
					return rv;
				}

				@Override
				public void done() {
					DataGenResult result = null;
					List<SimpleObservation> observations = new ArrayList<SimpleObservation>();
					try {
						result = get();
					} catch (InterruptedException ie) {
					} catch (ExecutionException ee) {
						showError(
								_frame,
								String
										.format(
												"Error occurred while generating test data.%n%1$s",
												ee.getMessage()));
					}
					observations = result.getObservations();
					_obsPanel.setObservations(observations);
					_resPanel.setObservationIndex(_obsIndex);
					_statusBar.setInputDataSize(observations.size());
					setStatus(null);
					result.displayResults(_frame);
				}

			};
			worker.execute();
		}
	}
	
	private void performExportKMLObservations() {
		// set the filechooser to use .kml
		_fileChooser.setFileFilter(new KmlFileFilter());
		int response = _fileChooser.showSaveDialog(_frame);
		if (response == JFileChooser.APPROVE_OPTION) {
			setStatus("Exporting to KML file...");
			File kmlFile = _fileChooser.getSelectedFile();
			if (!kmlFile.getName().toLowerCase().endsWith(".kml")) {
				kmlFile = new File(String.format("%1$s.kml", kmlFile
						.getAbsolutePath()));
			}
			final File toSave = kmlFile;
			List<SimpleObservation> sObsList = _obsPanel.getObservations();
			final List<Observation> obsList = new ArrayList<Observation>(
					WsToJhuConversionFactory.convertObservations(sObsList));
			SwingWorker<Boolean, List<SimpleObservation>> worker = new SwingWorker<Boolean, List<SimpleObservation>>() {

				@Override
				protected Boolean doInBackground() throws Exception {
					FileWriter kmlWriter = new FileWriter(toSave);
					boolean rv = KMLGenerator.writeKML(obsList, kmlWriter);
					return rv;
				}

				@Override
				public void done() {
					boolean result = false;
					try {
						result = get().booleanValue();
					} catch (InterruptedException ie) {
					} catch (ExecutionException ee) {
						showError(
								_frame,
								String
										.format(
												"Error occurred while saving observation data to KML file.%n%1$s",
												ee.getMessage()));
					}
					if (!result) {
						showError(_frame,
								"Error occurred while saving observation KML file.");
					}
					setStatus(null);
				}
			};
			worker.execute();
		}
		// set the filechooser back to the data filter
		_fileChooser.setFileFilter(new DataFileFilter());

	}
	
	private void performExportObservations() {
		int response = _fileChooser.showSaveDialog(_frame);
		if (response == JFileChooser.APPROVE_OPTION) {
			setStatus("Exporting data...");
			File obsFile = _fileChooser.getSelectedFile();
			if (!obsFile.getName().toLowerCase().endsWith(".data")) {
				obsFile = new File(String.format("%1$s.data", obsFile
						.getAbsolutePath()));
			}
			final File toSave = obsFile;
			List<SimpleObservation> sObsList = _obsPanel.getObservations();
			final List<Observation> obsList = new ArrayList<Observation>(
					WsToJhuConversionFactory.convertObservations(sObsList));
			SwingWorker<Boolean, List<SimpleObservation>> worker = new SwingWorker<Boolean, List<SimpleObservation>>() {

				@Override
				protected Boolean doInBackground() throws Exception {
					boolean rv = ObservationLoader.saveObservations(toSave,
							obsList);
					return rv;
				}

				@Override
				public void done() {
					boolean result = false;
					try {
						result = get().booleanValue();
					} catch (InterruptedException ie) {
					} catch (ExecutionException ee) {
						showError(
								_frame,
								String
										.format(
												"Error occurred while saving observation data to file.%n%1$s",
												ee.getMessage()));
					}
					if (!result) {
						showError(_frame,
								"Error occurred while saving observation data file.");
					}
					setStatus(null);
				}
			};
			worker.execute();
		}
	}

	private void performLoadDataFile() {
		int response = _fileChooser.showOpenDialog(_frame);
		if (response == JFileChooser.APPROVE_OPTION) {
			setStatus("Loading data...");
			final File obsFile = _fileChooser.getSelectedFile();
			SwingWorker<List<SimpleObservation>, Void> worker = new SwingWorker<List<SimpleObservation>, Void>() {
				@Override
				public List<SimpleObservation> doInBackground() {
					List<SimpleObservation> observations = new ArrayList<SimpleObservation>();
					System.out.println(String.format(
							"Loading Observations from %1$s...", obsFile
									.getAbsolutePath()));
					try {
						List<edu.jhuapl.c2s2.pp.observation.Observation> fileObservations = ObservationLoader
								.loadObservations(obsFile);
						observations = new ArrayList<SimpleObservation>(
								JhuToWsConversionFactory
										.convertObservations(fileObservations));
					} catch (Exception e) {
						showError(_frame, String.format(
								"Error occurred while loading %1$s.%n%2$s",
								obsFile.getAbsolutePath(), e.getMessage()));
					}
					_obsIndex = new ObservationIndex(observations);
					return observations;
				}

				@Override
				public void done() {
					List<SimpleObservation> observations = null;
					try {
						observations = get();
					} catch (InterruptedException ie) {
					} catch (ExecutionException ee) {
						showError(
								_frame,
								String
										.format(
												"Error occurred while loading observation data.%n%1$s",
												ee.getMessage()));
					}
					_obsPanel.setObservations(observations);
					_resPanel.setObservationIndex(_obsIndex);
					_statusBar.setInputDataSize(observations.size());
					setStatus(null);
				}
			};
			worker.execute();
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equalsIgnoreCase("execute")) {
			executeJob();
		}
	}

	private void executeJob() {
		_obsPanel.setBusyState(true);
		_statusBar.setMessage("Computing...");
		final List<SimpleObservation> observations = _obsPanel
				.getObservations();
		final Properties params = _obsPanel.getParameters();
		params.list(System.out);
		params
				.setProperty(Constants.KEY_PROCESS_MAX_DISTANCE_KM,
						(Double.toString((double) (_obsPanel
								.getMaxDistanceMeters() / 1000.0f))));
		params.setProperty(Constants.KEY_PROCESS_MAX_TIMESPAN_MS, Double
				.toString(_obsPanel.getMaxTimespanSeconds()));
		SwingWorker<List<DetectedProcess>, Void> worker = new SwingWorker<List<DetectedProcess>, Void>() {
			@Override
			public List<DetectedProcess> doInBackground() {
				IProcessFinderClient client = _obsPanel
						.getProcessFinderClient();
				String obsGroupUri = "http://samplegroup"
						+ System.currentTimeMillis();
				List<DetectedProcess> results = client.solicitDetection(
						observations, params, obsGroupUri);
				return results;
			}

			@Override
			public void done() {
				List<DetectedProcess> results = null;
				try {
					results = get();
				} catch (InterruptedException ie) {
				} catch (ExecutionException ee) {
					showError(
							_frame,
							String
									.format(
											"Error occurred while loading data on background thread.%n%1$s",
											ee.getMessage()));
				}
				System.out.println(String.format("Results (%1$d). Booyah!",
						(null != results) ? results.size() : 0));

				if (null != results) {
					_resPanel.setResults(results);
				} else {
					showError(_frame, "Returned results were null");
				}
				_obsPanel.setBusyState(false);
				_statusBar.setMessage(null);
			}
		};
		worker.execute();
	}

	private static void showGui() {
		MainFrame mf = new MainFrame();

		mf.initializeComponents();
		mf.show();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showGui();
			}
		});
	}

	public static void showError(Component comp, String error) {
		JOptionPane.showMessageDialog(comp, error, "Error Message",
				JOptionPane.ERROR_MESSAGE);
	}
}
