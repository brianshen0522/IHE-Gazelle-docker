import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";
import { getServerSession } from "next-auth";
import { authOptions } from "@auth/authOptions";
import { canAccessTestExecution } from "@home/utils/permissions";
import UnauthorizedPage from "@/app/unauthorized/page";
import TestSessionContent from "@test-execution/components/test-suite/TestSessionContent";

const Tests = async () => {
  const breadcrumbs = [
    { label: "Home", url: "/home" },
    { label: "Test execution", url: "" },
    { label: "Ad-hoc testing session", url: "Ad-hoc testing session" },
  ];

  const session = await getServerSession(authOptions);

  return (
    <>
      {canAccessTestExecution(session) ? (
        <div className="flex flex-col w-full p-2">
          <ContentHeaderWrapper id="test-execution-header" title="Test suite" breadcrumbs={breadcrumbs} />
          <TestSessionContent />
        </div>
      ) : (
        <UnauthorizedPage />
      )}
    </>
  );
};

export default Tests;
