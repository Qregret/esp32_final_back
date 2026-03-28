package com.example.smartlab.config;

import com.example.smartlab.websocket.StreamWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final StreamWebSocketHandler streamWebSocketHandler;
    private final CorsProperties corsProperties;

    public WebSocketConfig(StreamWebSocketHandler streamWebSocketHandler, CorsProperties corsProperties) {
        this.streamWebSocketHandler = streamWebSocketHandler;
        this.corsProperties = corsProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(streamWebSocketHandler, "/ws/stream/events")
                .setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(new String[0]));
    }
}
