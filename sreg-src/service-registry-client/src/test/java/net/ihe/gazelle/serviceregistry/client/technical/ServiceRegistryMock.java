/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.client.technical;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ServiceRegistryMock {

   public static final String SUCCESS_OUTCOME = "{\"status\":\"SUCCESS\"," +
                                                "\"message\":\"Service registered successfully\"}";
   public static final String FAILURE_OUTCOME = "{\"status\":\"FAILURE\",\"message\":\"Unexpected Error\"}";

   private final LinkedBlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();
   private final MockWebServer mockWebServer;

   public ServiceRegistryMock() {
      this(new QueueDispatcher());
   }

   public ServiceRegistryMock(final Dispatcher dispatcher) {
      mockWebServer = new MockWebServer();
      mockWebServer.setDispatcher(dispatcher);
   }

   public void start() {
      try {
         mockWebServer.start();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public String getUrl() {
      return mockWebServer.url("/service-registry").toString();
   }

   public void enqueueResponse(MockResponse response) {
      mockWebServer.enqueue(response);
   }

   public void setWebSocketDispatcher(Function<String, String> messageDispatcher) {
      mockWebServer.enqueue(
            new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
               @Override
               public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                  receivedMessages.add(text);
                  webSocket.send(messageDispatcher.apply(text));
               }

               @Override
               public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                  receivedMessages.add("CLOSING");
                  webSocket.close(1000, "Closing connection");
               }
            })
      );
   }

   public String pollReceivedMessage(long timeout, TimeUnit timeUnit) throws InterruptedException {
      return receivedMessages.poll(timeout, timeUnit);
   }

   public void shutdown() {
      try {
         mockWebServer.shutdown();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
