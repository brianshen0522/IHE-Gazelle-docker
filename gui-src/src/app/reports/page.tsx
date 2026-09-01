import React, { Suspense } from "react";
import { getServerSession } from "next-auth";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import { ReportExplorer } from "@shared/components/report-explorer/ReportExplorer";
import Unauthorized from "@shared/components/auth/Unauthorized";
import { authOptions } from "@shared/components/auth/authOptions";

export default async function ReportsPage() {
  const session = await getServerSession(authOptions);

  if (!session?.user) {
    return <Unauthorized />;
  }

  return (
    <SidePanelProvider>
      <Suspense fallback={<div>Loading...</div>}>
        <ReportExplorer />
      </Suspense>
    </SidePanelProvider>
  );
}
