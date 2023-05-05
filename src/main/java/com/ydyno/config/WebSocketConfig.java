/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ydyno.config;

import com.ydyno.service.WebSocketServer;
import com.ydyno.utils.SpringContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;


/**
 * @author Zheng Jie
 * @description 开启WebSocket
 **/
@Configuration
@EnableScheduling
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/api/ws/{sid}")
                .setAllowedOrigins("*")
                .addInterceptors(handshakeInterceptor());
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new WebSocketServer();
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new CustomHandshakeInterceptor();
    }

    @Scheduled(fixedRate = 15 * 1000)
    public void configureTasks() {
        //  每15秒发送一次心跳
        //  避免 org.apache.tomcat.util.net.NioEndpoint$NioSocketWrapper.fillReadBuffer 错误
        SpringContextHolder.getBean(WebSocketServer.class).groupMessage("this is the heartbeat message");
    }
}
