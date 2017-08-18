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

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Builds a HashMap with the HTTP query parameters,
 * calls WLSStatsManager process method and display
 * the result as plain old text
 * 
 * @author Yann Lambret
 * 
 */
@SuppressWarnings("serial")
public class WLSServlet extends HttpServlet {

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,String> params = new HashMap<String,String>();
        WLSStatsManager manager = new WLSStatsManager();

        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            String param = e.nextElement();
            params.put(param, request.getParameter(param));
        }

        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        out.print(manager.process(params));
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
