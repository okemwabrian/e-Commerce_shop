package com.example.shop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !("/api/auth/login".equals(path)
                || "/api/auth/register".equals(path)
                || "/api/auth/google".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        String key = request.getRemoteAddr() + ":" + request.getServletPath();
        Deque<Long> attempts = hits.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        boolean blocked;
        synchronized (attempts) {
            while (!attempts.isEmpty() && now - attempts.peekFirst() >= WINDOW_MS) {
                attempts.removeFirst();
            }
            blocked = attempts.size() >= MAX_ATTEMPTS;
            if (!blocked) {
                attempts.addLast(now);
            }
        }

        if (requestCount.incrementAndGet() % 1000 == 0) {
            hits.entrySet().removeIf(entry -> {
                Deque<Long> queue = entry.getValue();
                synchronized (queue) {
                    while (!queue.isEmpty() && now - queue.peekFirst() >= WINDOW_MS) {
                        queue.removeFirst();
                    }
                    return queue.isEmpty();
                }
            });
        }

        if (blocked) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many attempts. Try again in a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
