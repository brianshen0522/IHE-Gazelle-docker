"use server";
import axios from "axios";

export async function getGUMConfigurations() {
  try {
    const { data } = await axios.get(`${process.env.GZL_GUM_API_URL}/configurations`);
    return { data };
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.data) {
      return { data: err.response.data, error: err.response.data.error };
    } else {
      return { data: null, error: "Failed to fetch configs" };
    }
  }
}
