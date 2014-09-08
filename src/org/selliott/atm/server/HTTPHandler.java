package org.selliott.atm.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Handles HTTP requests from Jetty. TODO: It would be nice to make use of
 * Jetty's higher level handlers and annotations.
 */
public class HTTPHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(Main.class);

    @Override
    public void handle(String target, Request request,
            HttpServletRequest httpSRequest, HttpServletResponse httpSResponse)
            throws IOException, ServletException {

        try {
            log.info("Begin handler.");

            System.out.println("authType: " + request.getAuthType());
            System.out.println("pathInfo: " + request.getPathInfo());
            request.getReader();

            String path = request.getPathInfo();
            if (!path.equals("/atm")) {
                log.error("Unknown path \"" + path + "\".");
                httpSResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!request.getMethod().equals("POST")) {
                log.error("Request metho \"" + request.getMethod()
                        + "\" is not supported.");
                httpSResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            httpSResponse.setContentType("application/xml; charset=utf-8");

            MsgHandler msgHandler = new MsgHandler(request.getReader(),
                    httpSResponse.getWriter());
            if (msgHandler.process()) {
                httpSResponse.setStatus(HttpServletResponse.SC_OK);
            } else {
                httpSResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Unable to process message: " + e);
            // TODO: Is it be possible to provide the client with additional
            // information without compromising security?
            httpSResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            request.setHandled(true);
            log.info("End handler.");
        }
    }
}
