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

package net.ihe.gazelle.serviceregistry.technical.job;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MockLogger implements Logger {

   public static final String TRACE = "TRACE";
   public static final String DEBUG = "DEBUG";
   public static final String INFO = "INFO";
   public static final String WARN = "WARN";
   public static final String ERROR = "ERROR";

   public record LogEntry(String level, String message, Object[] args, Throwable throwable) {
      LogEntry(String level, String message) {
         this(level, message, null, null);
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof LogEntry(String level1, String message1, Object[] args1, Throwable throwable1))) {
            return false;
         }
         return Objects.equals(level, level1) && Objects.deepEquals(args,
               args1) && Objects.equals(message, message1) && Objects.equals(throwable,
               throwable1);
      }

      @Override
      public int hashCode() {
         return Objects.hash(level, message, Arrays.hashCode(args), throwable);
      }
   }

   private final List<LogEntry> logEntries = new ArrayList<>();

   public List<LogEntry> getLogEntries() {
      return new ArrayList<>(logEntries);
   }

   @Override
   public String getName() {
      return "";
   }

   @Override
   public boolean isTraceEnabled() {
      return false;
   }

   @Override
   public void trace(String s) {
      logEntries.add(new LogEntry(TRACE, s));
   }

   @Override
   public void trace(String s, Object o) {
      logEntries.add(new LogEntry(TRACE, s, new Object[]{o}, null));
   }

   @Override
   public void trace(String s, Object o, Object o1) {
      logEntries.add(new LogEntry(TRACE, s, new Object[]{o, o1}, null));
   }

   @Override
   public void trace(String s, Object... objects) {
      logEntries.add(new LogEntry(TRACE, s, objects, null));
   }

   @Override
   public void trace(String s, Throwable throwable) {
      logEntries.add(new LogEntry(TRACE, s, null, throwable));
   }

   @Override
   public boolean isTraceEnabled(Marker marker) {
      return false;
   }

   @Override
   public void trace(Marker marker, String s) {
      logEntries.add(new LogEntry(TRACE, s));
   }

   @Override
   public void trace(Marker marker, String s, Object o) {
      logEntries.add(new LogEntry(TRACE, s, new Object[]{o}, null));
   }

   @Override
   public void trace(Marker marker, String s, Object o, Object o1) {
      logEntries.add(new LogEntry(TRACE, s, new Object[]{o, o1}, null));
   }

   @Override
   public void trace(Marker marker, String s, Object... objects) {
      logEntries.add(new LogEntry(TRACE, s, objects, null));
   }

   @Override
   public void trace(Marker marker, String s, Throwable throwable) {
      logEntries.add(new LogEntry(TRACE, s, null, throwable));
   }

   @Override
   public boolean isDebugEnabled() {
      return false;
   }

   @Override
   public void debug(String s) {
      logEntries.add(new LogEntry(DEBUG, s));
   }

   @Override
   public void debug(String s, Object o) {
      logEntries.add(new LogEntry(DEBUG, s, new Object[]{o}, null));
   }

   @Override
   public void debug(String s, Object o, Object o1) {
      logEntries.add(new LogEntry(DEBUG, s, new Object[]{o, o1}, null));
   }

   @Override
   public void debug(String s, Object... objects) {
      logEntries.add(new LogEntry(DEBUG, s, objects, null));
   }

   @Override
   public void debug(String s, Throwable throwable) {
      logEntries.add(new LogEntry(DEBUG, s, null, throwable));
   }

   @Override
   public boolean isDebugEnabled(Marker marker) {
      return false;
   }

   @Override
   public void debug(Marker marker, String s) {
      logEntries.add(new LogEntry(DEBUG, s));
   }

   @Override
   public void debug(Marker marker, String s, Object o) {
      logEntries.add(new LogEntry(DEBUG, s, new Object[]{o}, null));
   }

   @Override
   public void debug(Marker marker, String s, Object o, Object o1) {
      logEntries.add(new LogEntry(DEBUG, s, new Object[]{o, o1}, null));
   }

   @Override
   public void debug(Marker marker, String s, Object... objects) {
      logEntries.add(new LogEntry(DEBUG, s, objects, null));
   }

   @Override
   public void debug(Marker marker, String s, Throwable throwable) {
      logEntries.add(new LogEntry(DEBUG, s, null, throwable));
   }

   @Override
   public boolean isInfoEnabled() {
      return false;
   }

   @Override
   public void info(String s) {
      logEntries.add(new LogEntry(INFO, s));
   }

   @Override
   public void info(String s, Object o) {
      logEntries.add(new LogEntry(INFO, s, new Object[]{o}, null));
   }

   @Override
   public void info(String s, Object o, Object o1) {
      logEntries.add(new LogEntry(INFO, s, new Object[]{o, o1}, null));
   }

   @Override
   public void info(String s, Object... objects) {
      logEntries.add(new LogEntry(INFO, s, objects, null));
   }

   @Override
   public void info(String s, Throwable throwable) {
      logEntries.add(new LogEntry(INFO, s, null, throwable));
   }

   @Override
   public boolean isInfoEnabled(Marker marker) {
      return false;
   }

   @Override
   public void info(Marker marker, String s) {
      logEntries.add(new LogEntry(INFO, s));
   }

   @Override
   public void info(Marker marker, String s, Object o) {
      logEntries.add(new LogEntry(INFO, s, new Object[]{o}, null));
   }

   @Override
   public void info(Marker marker, String s, Object o, Object o1) {
      logEntries.add(new LogEntry(INFO, s, new Object[]{o, o1}, null));
   }

   @Override
   public void info(Marker marker, String s, Object... objects) {
      logEntries.add(new LogEntry(INFO, s, objects, null));
   }

   @Override
   public void info(Marker marker, String s, Throwable throwable) {
      logEntries.add(new LogEntry(INFO, s, null, throwable));
   }

   @Override
   public boolean isWarnEnabled() {
      return false;
   }

   @Override
   public void warn(String s) {
      logEntries.add(new LogEntry(WARN, s));
   }

   @Override
   public void warn(String s, Object o) {
      logEntries.add(new LogEntry(WARN, s, new Object[]{o}, null));
   }

   @Override
   public void warn(String s, Object... objects) {
      logEntries.add(new LogEntry(WARN, s, objects, null));
   }

   @Override
   public void warn(String s, Object o, Object o1) {
      logEntries.add(new LogEntry(WARN, s, new Object[]{o, o1}, null));
   }

   @Override
   public void warn(String s, Throwable throwable) {
      logEntries.add(new LogEntry(WARN, s, null, throwable));
   }

   @Override
   public boolean isWarnEnabled(Marker marker) {
      return false;
   }

   @Override
   public void warn(Marker marker, String s) {
      logEntries.add(new LogEntry(WARN, s));
   }

   @Override
   public void warn(Marker marker, String s, Object o) {
      logEntries.add(new LogEntry(WARN, s, new Object[]{o}, null));
   }

   @Override
   public void warn(Marker marker, String s, Object o, Object o1) {
      logEntries.add(new LogEntry(WARN, s, new Object[]{o, o1}, null));
   }

   @Override
   public void warn(Marker marker, String s, Object... objects) {
      logEntries.add(new LogEntry(WARN, s, objects, null));
   }

   @Override
   public void warn(Marker marker, String s, Throwable throwable) {
      logEntries.add(new LogEntry(WARN, s, null, throwable));
   }

   @Override
   public boolean isErrorEnabled() {
      return false;
   }

   @Override
   public void error(String s) {
      logEntries.add(new LogEntry(ERROR, s));
   }

   @Override
   public void error(String s, Object o) {
      logEntries.add(new LogEntry(ERROR, s, new Object[]{o}, null));
   }

   @Override
   public void error(String s, Object o, Object o1) {
      logEntries.add(new LogEntry(ERROR, s, new Object[]{o, o1}, null));
   }

   @Override
   public void error(String s, Object... objects) {
      logEntries.add(new LogEntry(ERROR, s, objects, null));
   }

   @Override
   public void error(String s, Throwable throwable) {
      logEntries.add(new LogEntry(ERROR, s, null, throwable));
   }

   @Override
   public boolean isErrorEnabled(Marker marker) {
      return false;
   }

   @Override
   public void error(Marker marker, String s) {
      logEntries.add(new LogEntry(ERROR, s));
   }

   @Override
   public void error(Marker marker, String s, Object o) {
      logEntries.add(new LogEntry(ERROR, s, new Object[]{o}, null));
   }

   @Override
   public void error(Marker marker, String s, Object o, Object o1) {
      logEntries.add(new LogEntry(ERROR, s, new Object[]{o, o1}, null));
   }

   @Override
   public void error(Marker marker, String s, Object... objects) {
      logEntries.add(new LogEntry(ERROR, s, objects, null));
   }

   @Override
   public void error(Marker marker, String s, Throwable throwable) {
      logEntries.add(new LogEntry(ERROR, s, null, throwable));
   }
}
