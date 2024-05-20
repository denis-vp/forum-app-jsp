package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import utils.JwtUtil;

import java.io.IOException;

@WebFilter(filterName = "authenticationFilter", urlPatterns = {"/pages/*", "/scripts/*"})
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        String path = httpReq.getRequestURI();
        String contextPath = httpReq.getContextPath();

        // Skip authentication for login and register pages
        if (path.equals(contextPath + "/pages/login.jsp") || path.equals(contextPath + "/pages/register.jsp")
                || path.equals(contextPath + "/")) {
            chain.doFilter(req, resp);
            return;
        }

        // Skip authentication for login and register scripts
        if (path.equals(contextPath + "/scripts/login.js") || path.equals(contextPath + "/scripts/register.js")) {
            chain.doFilter(req, resp);
            return;
        }

        // Get token from cookies
        String token = null;
        Cookie[] cookies = httpReq.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Verify token
        if (token == null || JwtUtil.verifyToken(token) == null) {
            httpResp.sendRedirect(contextPath + "/pages/login.jsp");
            return;
        }

        chain.doFilter(req, resp);
    }
}
