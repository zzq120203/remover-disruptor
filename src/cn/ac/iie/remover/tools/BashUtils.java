package cn.ac.iie.remover.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BashUtils {

	private static Logger log = LoggerFactory.getLogger(BashUtils.class);

	
	public static boolean runRemoteCmd(String cmd) throws IOException {
		Process p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", cmd });
		try {
			InputStream err = p.getErrorStream();
			InputStreamReader isr = new InputStreamReader(err);
			BufferedReader br = new BufferedReader(isr);

			String line = null;
			StringBuilder str = null;

			while ((line = br.readLine()) != null)
				str.append(line);
			if (str != null) {
				log.error("cmd:{}; err:{}", cmd, str.toString());
			}
			int exitVal = p.waitFor();
			br.close();
			isr.close();
			err.close();
			if (exitVal > 0)
				return false;
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	public static String runRemoteCmdWithResult(String cmd) throws IOException {
		return runRemoteCmdWithResultVerbose(cmd, true);
	}

	public static String runRemoteCmdWithResultVerbose(String cmd, boolean verbose) throws IOException {
		Process p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", cmd });
		StringBuilder result = new StringBuilder();

		try {
			InputStream err = p.getErrorStream();
			InputStreamReader isr = new InputStreamReader(err);
			BufferedReader br = new BufferedReader(isr);

			String line = null;

			if (verbose)
				System.out.println("<ERROR>");

			while ((line = br.readLine()) != null) {
				if (verbose)
					System.out.println(line);
			}
			if (verbose)
				System.out.println("</ERROR>");
			br.close();
			isr.close();
			err.close();

			InputStream out = p.getInputStream();
			isr = new InputStreamReader(out);
			br = new BufferedReader(isr);

			if (verbose)
				System.out.println("<OUTPUT>");

			while ((line = br.readLine()) != null) {
				result.append(line + "\n");
				if (verbose)
					System.out.println(line);
			}
			if (verbose)
				System.out.println("</OUTPUT>");

			int exitVal = p.waitFor();
			if (verbose)
				System.out.println(" -> exit w/ " + exitVal);
			br.close();
			isr.close();
			out.close();
			if (exitVal > 0)
				return result.toString();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		return result.toString();
	}

}
