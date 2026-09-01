ALTER TABLE gum.user ALTER COLUMN last_login_timestamp TYPE timestamp(6) with time zone;
ALTER TABLE gum.user ALTER COLUMN last_update_timestamp TYPE timestamp(6) with time zone;
ALTER TABLE gum.user ALTER COLUMN registration_timestamp TYPE timestamp(6) with time zone;
ALTER TABLE gum.user_consent_history ALTER COLUMN timestamp TYPE timestamp(6) with time zone;