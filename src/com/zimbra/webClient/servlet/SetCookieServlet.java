/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Web Client
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.webClient.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.io.IOException;

public class SetCookieServlet extends ZCServlet
{
    
    private static final String PARAM_AUTH_TOKEN = "authToken";
    private static final String PARAM_PUBLIC_COMPUTER = "publicComputer";
    private static final String PARAM_QUERY_STRING_TO_CARRY = "qs";
    private static final String PARAM_AUTH_TOKEN_LIFETIME = "atl";
    private static final String DEFAULT_MAIL_URL = "/zimbra/mail";
    
    private static final String HEADER_HOST = "host";
    private static final String HEADER_REFERER = "referer";

    private static String redirectLocation;
    
    public void init(ServletConfig servletConfig) {
        redirectLocation = servletConfig.getInitParameter("mailUrl");
        if (redirectLocation == null) {
            redirectLocation = DEFAULT_MAIL_URL;
        }
    }

    public void doGet (HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException
    {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    public void doPost (HttpServletRequest req, HttpServletResponse resp) {
        
        try {
            String authToken = getReqParameter(req, PARAM_AUTH_TOKEN);
            if (authToken == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } else {
                String atl = getReqParameter(req, PARAM_AUTH_TOKEN_LIFETIME);
                String publicComputer = getReqParameter(req,
                                                        PARAM_PUBLIC_COMPUTER);
                boolean isPublic = false;
                if (publicComputer != null) {
                    isPublic = new Boolean(publicComputer).booleanValue();
                }
                

                int lifetime = -1;
                if (!isPublic){
                    try {
                        int lifetimeMs = Integer.parseInt(atl);
                        lifetime = lifetimeMs / 1000;
                    } catch (NumberFormatException ne){
                        lifetime = -1;
                    }
                }

                String authCookieVal = getCookieValue(req, "ZM_AUTH_TOKEN");
                if (authCookieVal == null) {
                    Cookie c = new Cookie("ZM_AUTH_TOKEN", authToken);
                    c.setPath("/");
                    c.setMaxAge(lifetime);                
                    resp.addCookie(c);
                }
            }
            
            String host = req.getHeader(HEADER_HOST);
            String referer = req.getHeader(HEADER_REFERER);
            //System.out.println("Host == " + host + " referer = " + referer);
            boolean abs = true;
            if (!shouldRedirectUrl(req) && referer != null && (referer.matches("[^/]*//" + host + "/.*")) ) {
                abs = false;
            } else {
                abs = true;
            }
            String redirectTo = getRedirectUrl(req, redirectLocation, null, 
                                               abs, true);
            //System.out.println("RedirectTo = " + redirectTo);
            
            resp.sendRedirect(redirectTo);
        } catch (IOException ie) {
	    // do nothing
        } catch (IllegalStateException is){
	    // do nothing
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ex.printStackTrace ();
        }
    }    

    private String getCookieValue (HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        String value = null;
        if (cookies != null) {
            for (int idx = 0; idx < cookies.length; ++idx) {
                if (cookies[idx].getName().equals(name)){
                    value = cookies[idx].getValue();
                }
            }
        }
        return value;
    }
    
	
}
