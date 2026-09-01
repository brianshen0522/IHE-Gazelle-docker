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

import net.ihe.gazelle.oidc.common.technical.OIDCIdentity;
import net.ihe.gazelle.security.business.Authenticator;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.mocks.MockedJsonWebToken;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class MockAuthenticator implements Authenticator {

   @Override
   public int getWeight() {
      return 3000;
   }

   @Override
   public GazelleIdentity authenticate() {
      JsonWebToken jwt = new MockedJsonWebToken();
      return new OIDCIdentity(jwt)
            .setId(jwt.getName())
            .setName(jwt.getSubject());
   }
}
