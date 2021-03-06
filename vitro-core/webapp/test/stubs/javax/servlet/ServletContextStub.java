/*
Copyright (c) 2013, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package stubs.javax.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple stand-in for the {@link ServletContext}, for use in unit tests.
 */
public class ServletContextStub implements ServletContext {
	private static final Log log = LogFactory.getLog(ServletContextStub.class);

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private String contextPath = ""; // root context returns ""
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	private final Map<String, String> mockResources = new HashMap<String, String>();
	private final Map<String, String> realPaths = new HashMap<String, String>();

	public void setContextPath(String contextPath) {
		if (contextPath == null) {
			throw new NullPointerException("contextPath may not be null.");
		}
	}

	public void setMockResource(String path, String contents) {
		if (path == null) {
			throw new NullPointerException("path may not be null.");
		}
		if (contents == null) {
			mockResources.remove(path);
		} else {
			mockResources.put(path, contents);
		}
	}

	public void setRealPath(String path, String filepath) {
		if (path == null) {
			throw new NullPointerException("path may not be null.");
		}
		if (filepath == null) {
			log.debug("removing real path for '" + path + "'");
			realPaths.remove(path);
		} else {
			log.debug("adding real path for '" + path + "' = '" + filepath
					+ "'");
			realPaths.put(path, filepath);
		}
	}

	/**
	 * Call setRealPath for each of the files in this directory (non-recursive).
	 * The prefix is the "pretend" location that we're mapping these files to,
	 * e.g. "/config/". Use the prefix and the filename as the path.
	 */
	public void setRealPaths(String pathPrefix, File dir) {
		for (File file : dir.listFiles()) {
			setRealPath(pathPrefix + file.getName(), file.getPath());
		}
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		if (object == null) {
			removeAttribute(name);
		} else {
			attributes.put(name, object);
		}
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		if (mockResources.containsKey(path)) {
			return new ByteArrayInputStream(mockResources.get(path).getBytes());
		} else {
			return null;
		}
	}

	@Override
	public String getRealPath(String path) {
		String real = realPaths.get(path);
		log.debug("Real path for '" + path + "' is '" + real + "'");
		return real;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public ServletContext getContext(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getContext() not implemented.");
	}

	@Override
	public String getInitParameter(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getInitParameter() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		throw new RuntimeException(
				"ServletContextStub.getInitParameterNames() not implemented.");
	}

	@Override
	public int getMajorVersion() {
		throw new RuntimeException(
				"ServletContextStub.getMajorVersion() not implemented.");
	}

	@Override
	public String getMimeType(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getMimeType() not implemented.");
	}

	@Override
	public int getMinorVersion() {
		throw new RuntimeException(
				"ServletContextStub.getMinorVersion() not implemented.");
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getNamedDispatcher() not implemented.");
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getRequestDispatcher() not implemented.");
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		throw new RuntimeException(
				"ServletContextStub.getResource() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Set getResourcePaths(String arg0) {
		throw new RuntimeException(
				"ServletContextStub.getResourcePaths() not implemented.");
	}

	@Override
	public String getServerInfo() {
		throw new RuntimeException(
				"ServletContextStub.getServerInfo() not implemented.");
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		throw new RuntimeException(
				"ServletContextStub.getServlet() not implemented.");
	}

	@Override
	public String getServletContextName() {
		throw new RuntimeException(
				"ServletContextStub.getServletContextName() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getServletNames() {
		throw new RuntimeException(
				"ServletContextStub.getServletNames() not implemented.");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getServlets() {
		throw new RuntimeException(
				"ServletContextStub.getServlets() not implemented.");
	}

	@Override
	public void log(String arg0) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

	@Override
	public void log(Exception arg0, String arg1) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		throw new RuntimeException("ServletContextStub.log() not implemented.");
	}

}
