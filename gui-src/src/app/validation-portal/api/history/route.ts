import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import { ParsedValidationContent, ValidationHistory, ValidationReportObject } from "@/app/validation-portal/types/ValidationProfile";

export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);

    // Return empty array if not authenticated (per VAL-051)
    if (!session) {
      return NextResponse.json({ data: [] }, { status: 200 });
    }

    const accessToken = session?.access_token;

    // Get query parameters
    const searchParams = request.nextUrl.searchParams;
    const profileId = searchParams.get("profileId");
    const owner = searchParams.get("owner") ?? "gazelle";
    const limit = searchParams.get("limit") ?? "10";
    const sortBy = searchParams.get("sortBy") ?? "date";
    const sortOrder = searchParams.get("sortOrder") ?? "DESC";

    if (!profileId) {
      return NextResponse.json({ error: "profileId query parameter is required" }, { status: 400 });
    }

    // Query Datahouse for validation reports that used this profile
    const sortPrefix = sortOrder === "DESC" ? "-" : "";
    const datahouseSearchParams = new URLSearchParams({
      profileId,
      owner,
      _limit: limit,
      _sort: `${sortPrefix}${sortBy}`,
    });
    const datahouseUrl = `${process.env.GZL_DTH_API_URL}/items?${datahouseSearchParams.toString()}`;

    const response = await fetch(datahouseUrl, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: `Backend error: ${response.status}` }));
      console.error("Backend returned error:", response.status, errorData);
      return NextResponse.json(errorData, { status: response.status });
    }

    const data = await response.json();

    const transformedItems: ValidationHistory[] = (data ?? []).map((item: ValidationReportObject): ValidationHistory => {
      try {
        const content: ParsedValidationContent = typeof item.content === "string" ? JSON.parse(item.content) : item.content;

        return {
          reportId: item.id ?? "",
          executionDate: item.date ?? new Date().toISOString(),
          result: content.overallResult ?? "UNKNOWN",
        };
      } catch (error) {
        console.error("Error parsing item content:", error);
        return {
          reportId: item.id ?? "",
          executionDate: item.date ?? new Date().toISOString(),
          result: "UNKNOWN",
        };
      }
    });

    return NextResponse.json({ data: transformedItems }, { status: 200 });
  } catch (error) {
    console.error("Error fetching profile history:", error);
    return NextResponse.json({ error: "Failed to fetch profile history" }, { status: 500 });
  }
}
