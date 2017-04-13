package com.dbms.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.service.AuthenticationService;

public class AuthenticationFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

	private List<String> resourceUris;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("Initialization done.");
		this.resourceUris = new ArrayList<>();
		this.resourceUris.add("/javax.faces.resource/");
		this.resourceUris.add("/skins/");
		this.resourceUris.add("/image/");
		this.resourceUris.add("/ui/");
		this.resourceUris.add("/temp/ldap.xhtml");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		boolean loginRequest = httpRequest.getRequestURI().equals(httpRequest.getContextPath() + "/login.xhtml");
		boolean resourceRequest = this.isResourceRequest(httpRequest);
		boolean facesAjaxRequest = "partial/ajax".equals(httpRequest.getHeader("Faces-Request"));
		LOG.info("Request uri " + httpRequest.getRequestURI() + " [loginRequest:" + loginRequest + ",  resourceRequest:"
				+ resourceRequest + ", facesAjaxRequest:" + facesAjaxRequest + "]");
		if (!loginRequest && !resourceRequest && !facesAjaxRequest) {
			HttpSession session = httpRequest.getSession(false);
			if (null == session) {
				httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
			} else {
				AuthenticationService authenticationService = (AuthenticationService) session
						.getAttribute("AuthenticationService");
				if (null == authenticationService) {
					httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
				} else {
					authenticationService.validateUser(httpRequest);
					if(authenticationService.hasAccess(httpRequest)) {
						chain.doFilter(request, response);
					} else {
						//redirect to index page
						httpResponse.sendRedirect(httpRequest.getContextPath() + "/");
					}
				}
			}
		} else {
			chain.doFilter(request, response);
		}

	}

	private boolean isResourceRequest(HttpServletRequest httpRequest) {
		boolean retVal = false;
		String uri = httpRequest.getRequestURI();
		String ctx = httpRequest.getContextPath();
		for (String resourceUri : resourceUris) {
			String ctxResourceUri = ctx + resourceUri;
			if (uri.startsWith(ctxResourceUri)) {
				retVal = true;
				break;
			}
		}

		if (retVal == false) {
			// check if the uri is a static resource mime ending
			retVal = (uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".jpg") || uri.endsWith(".png")
					|| uri.endsWith(".xcf") || uri.endsWith(".gif"));
		}
		return retVal;
	}
	
	@Override
	public void destroy() {

	}
}
