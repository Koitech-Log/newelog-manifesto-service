package br.com.newelog.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 401/403 no mesmo formato de erro dos demais serviços: { timestamp, mensagem }. */
public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        escrever(response, HttpServletResponse.SC_UNAUTHORIZED, "Sessão ausente ou expirada. Faça login novamente.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        escrever(response, HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para esta ação.");
    }

    private void escrever(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        // Mensagens fixas, sem aspas nem barras: dispensa serializador JSON.
        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"mensagem\":\"" + mensagem + "\"}");
    }
}
