"use client";

import TestSuiteContent from "@test-execution/components/test-suite/TestSuiteContent";
import { useSession } from "next-auth/react";
import { useTestSession } from "@test-execution/context/TestSessionContext";
import { useEffect } from "react";

export default function TestSessionContent() {
  const { data: session } = useSession();
  const { testSessionId, setTestSessionId } = useTestSession();

  useEffect(() => {
    if (!testSessionId && session?.user?.organization) {
      setTestSessionId(`adhoc-${session.user.organization}`);
    }
  }, [testSessionId, session?.user?.organization, setTestSessionId]);

  return <TestSuiteContent />;
}
