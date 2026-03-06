package com.school.bulletin.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        
        // Si l'utilisateur n'est pas en session, on le renvoie au login
        if (session.getAttribute("utilisateurConnecte") == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}