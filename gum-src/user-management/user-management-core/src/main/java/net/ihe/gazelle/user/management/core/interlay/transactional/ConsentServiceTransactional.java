package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentServiceImpl;

/**
 * Transactional implementation of ConsentService, which delegates the calls to a non-transactional implementation
 */
@RequestScoped
public class ConsentServiceTransactional implements ConsentService {

    private final ConsentService consentService;

    /**
     * Constructor for ConsentServiceTransactional, which initializes the non-transactional ConsentServiceImpl with the provided dependencies.
     * @param consentDAO the DAO for consent operations
     */
    @Inject
    public ConsentServiceTransactional(ConsentDAO consentDAO) {
        consentService = new ConsentServiceImpl(consentDAO);
    }

    @Override
    @Transactional
    public void acceptUserConsent(String userId) {
        consentService.acceptUserConsent(userId);
    }

    @Override
    public boolean needToGiveConsent(String userId) {
        return consentService.needToGiveConsent(userId);
    }
}
