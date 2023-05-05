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
package com.ydyno.service;

import cn.hutool.json.JSONUtil;
import com.ydyno.service.dto.OpenAiRequest;
import com.ydyno.utils.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketServer
 */
@Component
public class WebSocketServer extends TextWebSocketHandler {

    private final static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sid = (String) session.getAttributes().get("sid");
        sessions.put(sid, session);
        log.info("有新窗口开始监听:" + sid + ",当前在线人数为:" + sessions.size());
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sid = (String) session.getAttributes().get("sid");
        sessions.remove(sid);
        log.info("有一连接关闭:" + sid + ",当前在线人数为:" + sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sid = (String) session.getAttributes().get("sid");
        log.info("收到客户端(" + sid + ")消息: " + message.getPayload());
        OpenAiRequest openAiRequest = JSONUtil.toBean(message.getPayload(), OpenAiRequest.class);
        OpenAiService openAiService = SpringContextHolder.getBean(OpenAiService.class);
        try {
            openAiService.communicate(openAiRequest, sid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("发生错误");
        exception.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String sid, String message) throws IOException {
        WebSocketSession session = sessions.get(sid);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    /**
     * 群发自定义消息
     */
    public void groupMessage(String message) {
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
