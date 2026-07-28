package pe.finanzas.finanzas.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        HttpSession httpSession = request.getSession(false);

        if(httpSession == null || httpSession.getAttribute("idusuario") == null){
            response.sendRedirect(request.getContextPath() + "/");

            return false;
        }
        return true;
    }
}
