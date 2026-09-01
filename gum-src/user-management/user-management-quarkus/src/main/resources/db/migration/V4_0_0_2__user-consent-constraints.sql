ALTER TABLE gum.user_consent_history DROP CONSTRAINT fk_user_consent;

alter table if exists gum.user_consent
    add constraint fk_user_consent
    foreign key (user_id)
    references gum.user ON DELETE CASCADE;

alter table if exists gum.user_consent_history
    add constraint fk_user_consent_history
    foreign key (consent_id)
    references gum.user_consent ON DELETE CASCADE;