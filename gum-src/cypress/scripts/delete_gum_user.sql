DELETE FROM gum.user_consent_history
WHERE consent_id = (
    SELECT id
    FROM gum.user_consent
    WHERE id = consent_id
      AND user_id = (
        SELECT id
        FROM gum.user
        WHERE email = :emailUser
    )
);
DELETE FROM gum.user_consent
WHERE user_id = (
    SELECT id
    FROM gum.user
    WHERE email = :emailUser
);
DELETE FROM gum.user_group
WHERE user_id = (
    SELECT id
    FROM gum.user
    WHERE email = :emailUser
);
DELETE FROM gum.delegated_user
WHERE user_id = (
    SELECT id
    FROM gum.user
    WHERE email = :emailUser
);
DELETE FROM gum.user
WHERE email = :emailUser;