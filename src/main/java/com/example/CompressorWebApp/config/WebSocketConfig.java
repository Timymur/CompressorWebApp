package com.example.CompressorWebApp.config;



import com.example.CompressorWebApp.services.CompressorEventService;
import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.ModelParamRangeService;
import com.example.CompressorWebApp.websocket.CompressorWebSocketHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CompressorService compressorService;
    private final ModelParamRangeService modelParamRangeService;
    private final CompressorEventService compressorEventService;

    public WebSocketConfig(CompressorService compressorService, ModelParamRangeService modelParamRangeService, CompressorEventService compressorEventService){
        this.compressorService = compressorService;
        this.modelParamRangeService = modelParamRangeService;
        this.compressorEventService = compressorEventService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(compressorHandler(), "/ws/compressor")
                .setAllowedOriginPatterns("*");
    }

    @Bean
    public WebSocketHandler compressorHandler() {
        return new CompressorWebSocketHandler( compressorService, modelParamRangeService, compressorEventService);
    }
}
