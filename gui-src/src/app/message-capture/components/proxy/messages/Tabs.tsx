import { useEffect } from "react";
import { Tabs } from "@gazelle/gazelle-component-ui";
import { useTab } from "@/shared/context/tabContext";

interface TabsProps {
  isConnectionError: boolean;
}

const CTabs = ({ isConnectionError }: TabsProps) => {
  const { selectedTab, setSelectedTab } = useTab();

  useEffect(() => {
    setSelectedTab("Content");
  }, [isConnectionError, setSelectedTab]);

  return (
    <div className="flex items-center sm:gap-4 pl-4">
      {!isConnectionError && (
        <Tabs tabNames={["Content", "Validation"]} selectedTab={selectedTab} onTabSelect={(tabName) => setSelectedTab(tabName as any)} />
      )}
    </div>
  );
};

export default CTabs;
