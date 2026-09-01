'use server'

import {Session} from "next-auth";
import axios from "axios";
import {AccessControlList} from "@/shared/types/AccessControlListTypes";

type ParamProps = {
    id: string,
    session?: Session | null
}

type AccessControlListResult = {
    accessControlList?: AccessControlList;
    error?: string;
}

/**
 * Generate the read access key of access control list of a protected resource
 * @param id the id of the protected resource
 * @param session the session of the current logged in entity
 * @return the updated access control list
 */
export async function generateReadAccessKey({id, session}: ParamProps): Promise<AccessControlListResult> {
    const accessToken = session?.access_token

    try {
        const response = await axios.post(`${process.env.GZL_DTH_API_URL}/items/${id}/acl/privateKey$generate`, {}, {
            headers: {Authorization: "Bearer " + accessToken},
        });

        const newVar = await response.data as AccessControlList;
        return {accessControlList: newVar}
    } catch (err: any) {
        console.error(err);
        return {error: err?.message || "Unable to generate read access key"};
    }

}
