package hr.fer.zemris.edaf;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Message printer. Prints error, warrning or plain message to outputstream. By
 * flag setting it is possible to exit of the framework.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class MSGPrinter {

	private static PrintWriter writer;

	/**
	 * Error message printer.
	 * 
	 * @param os
	 *            Outputstream
	 * @param msg
	 *            Message
	 * @param exit
	 *            Flag
	 * @param status
	 *            -1 for failure
	 */
	public static void printERROR(OutputStream os, String msg, boolean exit,
			int status) {

		writer = new PrintWriter(os);

		writer.println("ERROR!");
		writer.println(msg);

		if (exit) {
			writer.println("EXIT");
			// to flush before exit
			writer.flush();

			System.exit(status);
		}

		writer.flush();

	}

	/**
	 * Warrning message printer.
	 * 
	 * @param os
	 *            Outputstream
	 * @param msg
	 *            Message
	 * @param exit
	 *            Flag
	 * @param status
	 *            -1 for failure
	 */
	public static void printWARRINIG(OutputStream os, String msg, boolean exit,
			int status) {

		writer = new PrintWriter(os);

		writer.println("WARRINIG!");
		writer.println(msg);

		if (exit) {
			writer.println("EXIT");
			// to flush before exit
			writer.flush();

			System.exit(status);
		}

		writer.flush();

	}

	public static void printMESSAGE(OutputStream os, String msg) {

		writer = new PrintWriter(os);

		writer.println(msg);

		writer.flush();

	}

}
