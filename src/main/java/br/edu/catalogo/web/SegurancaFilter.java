package br.edu.catalogo.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Aplica UTF-8, cabeçalhos e proteção CSRF antes de chegar ao controlador. */
public final class SegurancaFilter implements Filter {
    private static final SecureRandom ALEATORIO = new SecureRandom();
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        var res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "same-origin");
        res.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; form-action 'self'; frame-ancestors 'none'; base-uri 'self'");
        res.setHeader("Cache-Control", "no-store");
        var sessao = req.getSession();
        String token;
        synchronized (sessao) {
            token = (String) sessao.getAttribute("csrfToken");
            if (token == null) {
                byte[] bytes = new byte[32];
                ALEATORIO.nextBytes(bytes);
                token = HexFormat.of().formatHex(bytes);
                sessao.setAttribute("csrfToken", token);
            }
        }
        if ("POST".equals(req.getMethod())) {
            String recebido = req.getParameter("csrfToken");
            if (recebido == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), recebido.getBytes(StandardCharsets.UTF_8))) {
                res.setStatus(403);
                req.setAttribute("mensagemErro", "O formulário expirou ou não é válido. Reabra a página e tente novamente.");
                req.getRequestDispatcher("/WEB-INF/views/erro.jsp").forward(req, res);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
