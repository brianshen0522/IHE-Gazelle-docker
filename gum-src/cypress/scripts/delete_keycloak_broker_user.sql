DELETE FROM public.federated_user
WHERE id = (
    SELECT user_id
    FROM public.broker_link
    WHERE user_id = id
);
DELETE FROM public.fed_user_role_mapping WHERE user_id = (
    SELECT user_id
    FROM public.broker_link
    WHERE user_id = user_id
);
DELETE FROM public.broker_link
WHERE identity_provider = :idpAlias;
