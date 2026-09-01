DELETE FROM public.usr_delegated_organization WHERE organization_id = (SELECT id FROM public.usr_institution WHERE name =$1);
