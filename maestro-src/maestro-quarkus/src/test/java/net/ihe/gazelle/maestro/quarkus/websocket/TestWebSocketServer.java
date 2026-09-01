/*
 * Copyright 2025-2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestro.quarkus.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/test-server")
public class TestWebSocketServer {

    public static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

    @OnMessage
    public void onMessage(String msg) {
        MESSAGES.add(msg);
    }

    @OnOpen
    public void onOpen(Session session) {
        // nothing
    }

    @OnClose
    public void onClose(Session session) {
        // nothing
    }

    public static String pollLastMessage() throws InterruptedException {
        return MESSAGES.poll(2000, TimeUnit.MILLISECONDS);
    }

    public static void clear() {
        MESSAGES.clear();
    }

}
