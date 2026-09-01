package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentServiceImpl implements ConsentService {
    private final Logger log = LoggerFactory.getLogger(ConsentServiceImpl.class.getName());

    private final ConsentDAO consentDAO;

    public ConsentServiceImpl(ConsentDAO consentDAO) {
        this.consentDAO = consentDAO;
    }

    @Override
    public void acceptUserConsent(String userId) {
        log.debug("acceptUserConsent for user: {}", userId);
        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());

        consentDAO.acceptUserConsent(userId);
    }

    @Override
    public boolean needToGiveConsent(String userId) {
        log.debug("needToConsent for user: {}", userId);
        if (userId == null)
            return false;

        return consentDAO.needToGiveConsent(userId);
    }
}
