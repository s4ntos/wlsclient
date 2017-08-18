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

package centreon.monitor.jmx.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;

import centreon.monitor.jmx.core.Result;
import centreon.monitor.jmx.core.Status;
import centreon.monitor.jmx.core.WLSProxy;

/**
 * Gets the current HTTP sessions count
 * for a web application.
 *
 * @author Yann Lambret
 * @author Kiril Dunn
 * 
 */
public class ComponentTest extends TestUtils implements Test {

    // No statistics for WLS internal components
    private static final List<String> EXCLUSIONS = Collections.unmodifiableList(Arrays.asList(
            "_async",
            "bea_wls_deployment_internal",
            "bea_wls_cluster_internal",
            "bea_wls_diagnostics",
            "bea_wls_internal",
            "console",
            "consolehelp",
            "uddi",
            "uddiexplorer"));

    /**
     * WebLogic applications stats.
     * 
     * @param proxy   an applicative proxy for the target WLS instance
     * @param params  a pipe separated list of web application names, or
     *                a wildcard character (*) for all web applications
     * @return result collected data and test status
     */
    public Result run(WLSProxy proxy, String params) {
        Result result = new Result();
        List<String> output = new ArrayList<String>();
        List<String> message = new ArrayList<String>();
        int code = 0;

        // Test thresholds
        long warning;
        long critical;
        String thresholds = "";
        Map<String,String> components = new HashMap<String,String>();

        // Test code for a specific application
        int testCode = 0;

        // Message prefix
        String prefix = "HTTP session count: ";

        // Performance data
        int openSessions;

        // Parses HTTP query params
        for (String s : Arrays.asList(params.split("\\|"))) {
            components.put(s.split(",", 2)[0], s.split(",", 2)[1]);
        }

        try {
            ObjectName[] applicationRuntimeMbeans = proxy.getMBeans("ApplicationRuntimes");
            for (ObjectName applicationRuntime : applicationRuntimeMbeans) {
                ObjectName[] componentRuntimeMbeans = proxy.getMBeans(applicationRuntime, "ComponentRuntimes");
                for (ObjectName componentRuntime : componentRuntimeMbeans) {
                    String contextRoot;
                    try {
                        contextRoot = (String)proxy.getAttribute(componentRuntime, "ContextRoot");
                        // The context root may be an empty string or a single character
                        if (contextRoot.length() > 1) {
                            contextRoot = contextRoot.substring(1);
                        }
                    } catch (AttributeNotFoundException ignored) {
                        // Our component is not an instance of WebAppComponentRuntimeMBean
                        continue;
                    }
                    if (EXCLUSIONS.contains(contextRoot)) {
                        continue;
                    }
                    if (components.containsKey("*") || components.containsKey(contextRoot)) {
                        openSessions = (Integer)proxy.getAttribute(componentRuntime, "OpenSessionsCurrentCount");
                        output.add("app-" + contextRoot + "=" + openSessions);
                        thresholds = components.get("*") != null ? components.get("*") : components.get(contextRoot);
                        warning = Long.parseLong(thresholds.split(",")[0]);
                        critical = Long.parseLong(thresholds.split(",")[1]);
                        testCode = checkResult(openSessions, critical, warning);
                        if (testCode == Status.WARNING.getCode() || testCode == Status.CRITICAL.getCode()) {
                            message.add(contextRoot + " (" + openSessions + ")");
                            code = (testCode > code) ? testCode : code;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(Status.UNKNOWN);
            result.setMessage(e.toString());
            return result;
        }

        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                result.setStatus(status);           
                break;
            }
        }

        result.setOutput(formatOut(output));
        result.setMessage(formatMsg(prefix, message));

        return result;
    }

}
