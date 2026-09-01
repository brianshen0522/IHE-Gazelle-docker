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

package net.ihe.gazelle.simulation.business;

import java.time.Duration;
import java.time.Instant;

/**
 * Simple timer to check if a cache entry has expired.
 */
public class ExpirationTimer {

    private final Duration timeout;
    private Instant lastUpdate;

   /**
    * Constructs an ExpirationTimer with a specified timeout duration.
    * The timer starts with an initial timestamp set to the current time.
    *
    * @param timeout the duration after which the timer is considered expired
    */
    public ExpirationTimer(Duration timeout) {
        this.timeout = timeout;
        this.lastUpdate = Instant.now();
    }

   /**
    * Determines if the timer has expired based on the configured timeout duration.
    *
    * @return true if the current time is after the sum of the last update timestamp and the timeout period, false otherwise.
    */
    public boolean isExpired() {
        return Instant.now().isAfter(lastUpdate.plus(timeout));
    }

   /**
    * Resets the timer by updating the last update timestamp to the current time.
    * This method is typically used to restart the expiration timer when needed.
    */
   public void reset() {
        lastUpdate = Instant.now();
    }
}
