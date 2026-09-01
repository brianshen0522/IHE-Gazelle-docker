import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import ValidationPortalContent from "./ValidationPortalContent";

/**
 * Validation Portal Main Page
 * Lists all available validation profiles with filtering and search capabilities
 */
export default async function ValidationPortalPage() {
  await getServerSession(authOptions);

  const breadcrumbs = [
    { label: "Home", url: "/home" },
    { label: "Validation portal", url: "" },
  ];

  return <ValidationPortalContent breadcrumbs={breadcrumbs} />;
}
