import { ValidationReportMain } from "@/app/validation-portal/components/validation-report/ValidationReportMain";
import { ToastContainer } from "react-toastify";

export default async function ValidationReportPage(props: Readonly<{ params: Promise<{ reportId: string }> }>) {
  const params = await props.params;

  return (
    <>
      <ValidationReportMain reportId={params.reportId} />
      <ToastContainer />
    </>
  );
}
