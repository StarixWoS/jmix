
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


// TODO swap out MINA, replace with something else (new version or w/e)
public class JMIX {
	public static Server server;
	private static Logger logger;
	
	public static void main (String[] args) throws IOException {
		System.out.println(System.getenv());
		System.getProperties().list(System.out);
		
		initLogger();
		
		// Create command line options
		Options options = new Options();
		options.addOption("t", false, "display current time");
		
		// Create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// Parse command line args
			CommandLine cmd = parser.parse(options, args);
			
			// Do stuff with the parsed arguments
		} catch (ParseException e) {
			e.printStackTrace();
			// Log and use default values
		}
		
		server = new Server(new ServerConfiguration());
	}
	
	private static void initLogger() {
		logger = Logger.getLogger("JMIX");
		//ConsoleHandler ch = new ConsoleHandler();
		//logger.addHandler(ch);
		logger.setLevel(Level.ALL);
		//ch.setFormatter(new SimpleFormatter());
		logger.log(Level.INFO, "LOGGER STARTED");
	}
	
	public static Logger getLogger() {
		return logger;
	}
	
	public static void setLoggerLevel(Level level) {
		logger.setLevel(level);
	}	
}
