import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import ValidatePageClient from "./ValidatePageClient";
import MissingParamsClient from "./MissingParamsClient";

export default async function ValidatePage({ searchParams }: Readonly<{ searchParams: Promise<{ profileId?: string; serviceName?: string }> }>) {
  const session = await getServerSession(authOptions);
  const params = await searchParams;
  const { profileId, serviceName } = params;

  if (!profileId || !serviceName) {
    return <MissingParamsClient />;
  }

  return <ValidatePageClient profileId={profileId} serviceName={serviceName} session={session} />;
}
