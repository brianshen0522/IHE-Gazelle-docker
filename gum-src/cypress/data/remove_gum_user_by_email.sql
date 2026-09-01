DELETE FROM gum.credentials  WHERE user_id=(SELECT id from gum.user WHERE email=$1);
DELETE FROM gum.user_consent_history  WHERE consent_id=(SELECT id FROM gum.user_consent  WHERE user_id=(SELECT id from gum.user WHERE email=$1));
DELETE FROM gum.user_consent  WHERE user_id=(SELECT id from gum.user WHERE email=$1);
DELETE FROM gum.delegated_user WHERE user_id=(SELECT id from gum.user WHERE email=$1);
DELETE FROM gum.user WHERE email=$1;
