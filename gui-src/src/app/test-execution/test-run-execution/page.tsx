import { ToastContainer } from "react-toastify";
import TestRunWrapper from "../components/test-run/TestRunWrapper";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import { canAccessTestExecution } from "@home/utils/permissions";
import UnauthorizedPage from "@/app/unauthorized/page";

interface TestRunPageProps {
  searchParams: Promise<{ executionId?: string }>;
}

const TestRunPage = async ({ searchParams }: TestRunPageProps) => {
  const session = await getServerSession(authOptions);
  const params = await searchParams;

  return (
    <>
      {canAccessTestExecution(session) ? (
        <div className="flex flex-col w-full p-2">
          <TestRunWrapper params={params} session={session} />
          <ToastContainer />
        </div>
      ) : (
        <UnauthorizedPage />
      )}
    </>
  );
};

export default TestRunPage;
