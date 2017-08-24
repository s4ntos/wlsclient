/**
 * This file is part of Wlsagent.
 *
 * Wlsagent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wlsagent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wlsagent. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package centreon.monitor.jmx.core;

// import java.io.IOException;
// import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

/**
 * Builds a HashMap with the HTTP query parameters,
 * calls WLSStatsManager process method and display
 * the result as plain old text
 * 
 * @author Yann Lambret
 * 
 */
@SuppressWarnings("serial")
public class WLSClient {
    private static Options options = new Options();
    private static StringBuilder output = new StringBuilder();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Map<String,String> params = new HashMap<String,String>();
        WLSStatsManager manager = new WLSStatsManager();
// create the command line parser
	CommandLineParser parser = new DefaultParser();
	String monitor = null;
	String filter = null;
	String vcritical = null;
	String vwarning = null;
// create the Options
	options.addOption( "h", "help", false, "Print the help" );
	options.addOption( "d", "debug", false, "Debug" );
	options.addOption( "H", "host", true, "Host to connect to" );
	options.addOption( "p", "port", true, "Port to connect to" );
	options.addOption( "t", "protocol", true, "Protocol" );
	options.addOption( "U", "username", true, "weblogic username" );
	options.addOption( "P", "password", true, "weblogic password" );
	options.addOption( "m", "monitor", true, "monitor type options are: thread-pool (default), jta, jvm, jdbc, jms-runtime, component, jms-queue");
        options.addOption( OptionBuilder.withLongOpt( "protocol" )
                            .withDescription( "protocol to use: t3 (default), t3s" )
                            .hasArg()
                            .create() );
        options.addOption( "f", "filter", true, "filter of weblogic parameters");
        options.addOption( "w", "warning", true, "warning threshold");
        options.addOption( "c", "critical", true, "critical threshold");
	try { 
        	CommandLine line = parser.parse( options, args );
		if ( line.hasOption("h") )
	    	    help();	
		if ( line.hasOption("H") )
		    params.put("hostname", line.getOptionValue("H"));
		else {
    		    System.out.println("Host is a mandatory parameter and is not set");
		    System.exit(3);
 		}
		if ( line.hasOption("p") )
		    params.put("port", line.getOptionValue("p"));
		if ( line.hasOption("t") )
		    params.put("protocol", line.getOptionValue("t"));
		if ( line.hasOption("U") )
		    params.put("username", line.getOptionValue("U"));
		if ( line.hasOption("m") )
		    params.put("password", line.getOptionValue("P"));
		if ( line.hasOption("h") ) {
		    params.put("debug", "true");
		} else { 
		    params.put("debug", "false");
		}
		if ( line.hasOption("m") )
		    monitor = line.getOptionValue("m");
		if ( line.hasOption("f") )
		    filter = line.getOptionValue("f");
		if ( line.hasOption("w") )
		    vwarning = line.getOptionValue("w");
		if ( line.hasOption("c") )
		    vcritical = line.getOptionValue("c");
	}
	catch( ParseException exp ) {
    		System.out.println( "Unexpected exception:" + exp.getMessage() );
		System.exit(3);
	}
        if ( params.get("username") == null )
        	params.put("username", "weblogic");
        if ( params.get("password") == null )
		params.put("password", "Manager1");
        if ( params.get("port") == null )
		params.put("port", "7001");
        if ( params.get("protocol") == null )
		params.put("protocol", "t3");
        if ( monitor == null )
		monitor = "thread-pool";
        if ( filter == null )
		filter = "*";
        if ( vwarning == null )
		vwarning = "10";
        if ( vcritical == null )
		vcritical = "20";
	//System.out.println(monitor + "=" + filter + "," + vwarning + "," + vcritical);
	params.put(monitor, filter + "," + vwarning + "," + vcritical);
        String[] ret = manager.process(params);
	System.out.println( String.join("|", Arrays.copyOfRange(ret,1,ret.length )));
	System.exit(Integer.valueOf(ret[0]));
    }
    private static void help() {
	  // This prints out some help
	HelpFormatter formater = new HelpFormatter();
	formater.printHelp("WLSClient", options);
	System.exit(0);
    }
}
