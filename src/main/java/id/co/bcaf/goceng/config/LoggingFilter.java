package id.co.bcaf.goceng.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logging filter for incoming HTTP requests.
 * This filter logs the HTTP method and request URI of every incoming request
 * before passing it along the filter chain.
 */

@Component
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        System.out.println("🔍 Incoming Request: " + httpReq.getMethod() + " " + httpReq.getRequestURI());
        chain.doFilter(request, response);
    }
}
