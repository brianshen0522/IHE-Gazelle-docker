import ContentClient from "../components/validation-report/content/ContentClient";
import { ReportAssertionsProvider } from "../context/selectedAssertionContext";

const ContentPage = () => {
  return (
    <ReportAssertionsProvider>
      <ContentClient />
    </ReportAssertionsProvider>
  );
};

export default ContentPage;
