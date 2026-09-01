DELETE FROM public.federated_user WHERE id = (SELECT user_id FROM public.broker_link WHERE broker_username =$1);
DELETE FROM public.fed_user_role_mapping WHERE user_id = (SELECT user_id FROM public.broker_link WHERE broker_username =$1);
DELETE FROM public.broker_link WHERE broker_username =$1;