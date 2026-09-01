package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.user.management.api.application.user.registration.ConsentException;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ConsentServiceTest {

    @Mock
    private ConsentDAO consentDAOMock;

    private ConsentService consentService;

    @BeforeEach
    void init() {
        consentService = new ConsentServiceImpl(consentDAOMock);
    }

    @Test
    void needToGiveConsentTest() {
        when(consentDAOMock.needToGiveConsent("userWithGivenConsent")).thenReturn(false);
        when(consentDAOMock.needToGiveConsent("userWithoutGivenConsent")).thenReturn(true);

        assertFalse(consentService.needToGiveConsent(null));
        assertFalse(consentService.needToGiveConsent("userWithGivenConsent"));
        assertTrue(consentService.needToGiveConsent("userWithoutGivenConsent"));
    }

    @Test
    void testUserAcceptConsent() {
        // No user id
        assertThrows(IllegalArgumentException.class,() -> consentService.acceptUserConsent(null));

        // Bad user id
        doThrow(new ConsentException("")).when(consentDAOMock).acceptUserConsent("userId2");
        assertThrows(ConsentException.class,() -> consentService.acceptUserConsent("userId2"));

        // Correct user id
        assertDoesNotThrow(() -> consentService.acceptUserConsent("userId"));
    }
}
