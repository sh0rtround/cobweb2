package org.cobweb.cobweb2.ui.swing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ui.LoggingExceptionHandler;
import org.cobweb.cobweb2.ui.SimulationRunnerBase;
import org.cobweb.cobweb2.ui.UserInputException;
import org.cobweb.cobweb2.ui.swing.config.GUI;

/**
 * This class contains the main method to drive the application.
 *
 * @author Cobweb Team (Might want to specify)
 *
 */
public class CobwebApplicationRunner {

	/**
	 * The main function is found here for the application version of cobweb.
	 * It initializes the simulation and settings using a settings file optionally
	 * defined by the user.
	 *
	 * <p>Switches:
	 *
	 * <p><p> --help
	 * <br>Prints the various flags that can be used to run the program:
	 * Syntax = "cobweb2 [--help] [-hide] [-autorun finalstep] [-log LogFile.tsv]
	 * [[-open] SettingsFile.xml]"
	 *
	 * <p> -hide
	 * <br>When the hide flag is used, the user interface does not initialize
	 * (visible is set to false).  If visible is set to false, the User Interface
	 * Client will be set to a NullDisplayApplication rather than a
	 * CobwebApplication.  Need to specify an input file to use this switch.
	 *
	 * <p> -open [must specify]
	 * <br>If not used, the default is
	 * CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME  +
	 * CobwebApplication.CONFIG_FILE_EXTENSION otherwise will be set to
	 * whatever the user specifies.  The input file contains the initial conditions
	 * of the simulation (AgentTypeCount, FoodTypeCount, etc.)
	 *
	 * <p> -log [must specify]
	 * <br>Specify the name of the log file.
	 *
	 * <p> -autorun [specify integer >= -1]
	 *
	 * @param args command line arguments
	 * @see CobwebApplication#INITIAL_OR_NEW_INPUT_FILE_NAME
	 * @see CobwebApplication#CONFIG_FILE_EXTENSION
	 */

	public static void main(String[] args) {

		// Process Arguments`

		String inputFileName = "";
		String logFileName = "";
		boolean autostart = false;
		int finalstep = 0;
		boolean visible = true;

		if (args.length > 0) {
			for (int arg_pos = 0; arg_pos < args.length; ++arg_pos){
				if (args[arg_pos].equalsIgnoreCase("--help")){
					System.out.println("Syntax: " + CobwebApplicationRunner.Syntax);
					System.exit(0);
				} else if (args[arg_pos].equalsIgnoreCase("-autorun")){
					autostart = true;
					try{
						finalstep = Integer.parseInt(args[++arg_pos]);
					} catch (NumberFormatException numexception){
						System.out.println("-autorun argument must be integer");
						System.exit(1);
					}
					if (finalstep < -1) {
						System.out.println("-autorun argument must >= -1");
						System.exit(1);
					}
				} else if (args[arg_pos].equalsIgnoreCase("-hide")){
					visible=false;
				} else if (args[arg_pos].equalsIgnoreCase("-open")){
					if (args.length - arg_pos == 1) {
						System.out.println("No value attached to '-open' argument,\n" +
								"Correct Syntax is: " + CobwebApplicationRunner.Syntax);
						System.exit(1);
					} else {
						inputFileName = args[++arg_pos];
					}
				} else if (args[arg_pos].equalsIgnoreCase("-log")){
					if (args.length - arg_pos == 1) {
						System.out.println("No value attached to '-log' argument,\n" +
								"Correct Syntax is: " + CobwebApplicationRunner.Syntax);
						System.exit(1);
					} else {
						logFileName = args[++arg_pos];
					}
				} else {
					inputFileName = args[arg_pos];
				}
			}
		}

		if (!inputFileName.equals("") && ! new File(inputFileName).exists()){
			System.out.println("Invalid settings file value: '" + inputFileName + "' does not exist" );
			System.exit(1);
		}

		main(inputFileName, logFileName, autostart, finalstep, visible);
	}

	public static void main(String inputFileName, String logFileName, boolean autostart, int finalstep, boolean visible) {
		if (!logFileName.isEmpty() && new File(logFileName).exists()){
			System.out.println("WARNING: log '" + logFileName + "' already exists, overwriting it!" );
		}

		//Create CobwebApplication and threads; this is not done earlier so
		// that argument errors will result in quick exits.

		boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

		if (!isDebug) {
			LoggingExceptionHandler handler = visible ? new SwingExceptionHandler() : new LoggingExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(handler);
		}

		//Set up inputFile

		if (inputFileName.equals("")) {
			if (!visible) {
				System.err.println("Please specify an input file name when running with the -hide option");
				return;
			}

			String tempdir = System.getProperty("java.io.tmpdir");
			String sep = System.getProperty("file.separator");
			if (!tempdir.endsWith(sep))
				tempdir = tempdir + sep;

			inputFileName = CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME;
			File testFile = new File(inputFileName);
			if (! (testFile.exists() && testFile.canWrite()))
				inputFileName = tempdir + CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME;

		}

		SimulationConfig defaultconf = null;
		try {
			defaultconf = new SimulationConfig(inputFileName);
		} catch (FileNotFoundException ex) {
			if (!visible) {
				System.err.println("Input file does not exist, creating it with default settings.");
			}
			defaultconf = new SimulationConfig();
			try {
				defaultconf.write(new FileOutputStream(inputFileName));
				defaultconf = new SimulationConfig(inputFileName);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not write default configuration file");
			}
		} catch (Exception e) {
			String message = "Cannot load " + inputFileName + "";
			if (visible) {
				throw new UserInputException(message);
			} else {
				System.err.println(message);
				throw new RuntimeException(e);
			}
		}

		// $$$$$$ Added to check if the new data file is hidden. Feb 22
		File inf = new File(inputFileName);
		if (inf.isHidden() || (inf.exists() && !inf.canWrite())) {
			JOptionPane.showMessageDialog(GUI.frame, "Caution:  The initial data file \"" + inputFileName
					+ "\" is NOT allowed to be modified.\n"
					+ "\n                  Any modification of this data file will be neither implemented nor saved.");
		}

		final SimulationRunnerBase simRunner;
		if (visible) {
			CobwebApplication CA = new CobwebApplication();
			CA.openFile(defaultconf);
			simRunner = CA.simRunner;
		} else {
			Simulation simulation;
			simulation = new Simulation();
			simulation.load(defaultconf);
			simRunner = new SimulationRunnerBase(simulation);
		}
		simRunner.setAutoStopTime(finalstep);

		if (!logFileName.isEmpty()){
			try {
				simRunner.setLog(new FileWriter(logFileName, false));
			} catch (IOException ex) {
				throw new UserInputException("Can't create log file!", ex);
			}
		}

		if (autostart) {
			simRunner.run();
		}
	}

	public static final String Syntax = "cobweb2 [--help] [-hide] [-autorun finalstep] [-log LogFile.tsv] [[-open] SettingsFile.xml]";



}
