/*
 * Copyright 2024 IHE International.
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

package db.migration;

import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.quarkus.GazelleProfiles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestProfile(GazelleProfiles.UnitTest.class)
class GazelleUserRoleWrapperTest {

    @Test
    void testConstructorSettersAndGetters() {
        GazelleUserPreferencesWrapper gazelleUserWrapper = new GazelleUserPreferencesWrapper();

        gazelleUserWrapper.setUserPhoto(new byte[1]);
        gazelleUserWrapper.setUserPhotoThumbnail(new byte[2]);
        gazelleUserWrapper.setSpokenLanguagesString("franchese");
        UserPreference userPref = new UserPreference();
        userPref.setUserId("myTestUserId");
        gazelleUserWrapper.setUserPreference(userPref);

        assertEquals(userPref, gazelleUserWrapper.getUserPreference());
        assertEquals("franchese", gazelleUserWrapper.getSpokenLanguagesString());
        assertEquals(1, gazelleUserWrapper.getUserPicture().length);
        assertEquals(2, gazelleUserWrapper.getUserThumbnail().length);
    }
}