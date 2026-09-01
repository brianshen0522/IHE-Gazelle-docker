import { Metadata } from "next";
import { TestExecutionReport } from "@test-execution/components/test-report/TestExecutionReport";

export async function generateMetadata({ params }: Readonly<{ params: Promise<{ reportId: string }> }>): Promise<Metadata> {
  const { reportId } = await params;
  return { title: `Report | ${reportId}` };
}

export default async function ReportPage({ params }: Readonly<{ params: Promise<{ reportId: string }> }>) {
  const { reportId } = await params;

  return <TestExecutionReport reportId={reportId} />;
}
