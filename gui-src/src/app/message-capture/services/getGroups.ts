'use server'
import axios from "axios";


export type Organization = {
    id: string;
    name: string;
    url: string;
}

type OrganizationByIdResult = {
    organization?: Organization;
    error?: string;
}

/**
 * Get an organization by its id
 * @param id the id of the organization
 * @return the organization if found, otherwise returns an error
 */
export async function getOrganizationById(id: string): Promise<OrganizationByIdResult> {

    try {
        const url = `${process.env.GZL_GUM_API_URL}/organizations/${id}`
        const response = await axios.get(url)
        const responseData = await response.data as Organization;

        return {organization: responseData}
    } catch (err: any) {
        console.error(err)
        return {error: err?.message || "Unable to get groups"}
    }
}
