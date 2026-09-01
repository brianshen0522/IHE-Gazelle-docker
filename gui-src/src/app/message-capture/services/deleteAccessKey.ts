'use server'
import {Session} from "next-auth";
import axios from "axios";
import {AccessControlList} from "@/shared/types/AccessControlListTypes";

type DeleteAccessKeyProps = {
    id: string;
    session: Session | null
}

type DeleteAccessKeyResult = {
    accessControlList?: AccessControlList
    error?: string;
}


export async function deleteAccessKey({id, session}: DeleteAccessKeyProps): Promise<DeleteAccessKeyResult> {
    const accessToken = session?.access_token

    try {
        const url = `${process.env.GZL_DTH_API_URL}/items/${id}/acl/`;
        const response = await axios.delete(url, {
            headers: {
                Authorization: "Bearer " + accessToken
            }
        })
        const data = await response.data as AccessControlList;
        return {accessControlList: data}
    } catch (err: any) {
        console.error(err);
        return {error: err?.message || "Unable to delete access key"}
    }
}
