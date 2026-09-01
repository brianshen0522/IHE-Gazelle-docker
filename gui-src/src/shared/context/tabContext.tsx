"use client";
import React, { createContext, useState, useContext, useMemo } from "react";
import { Tabs } from "@/app/message-capture/components/proxy/Types";
import { Renderers } from "../../app/message-capture/components/proxy/Types";

type TabContextType = {
  selectedTab: Tabs;
  setSelectedTab: React.Dispatch<React.SetStateAction<Tabs>>;
  selectedRenderer: Renderers;
  setSelectedRenderer: React.Dispatch<React.SetStateAction<Renderers>>;
};

type TabProviderProps = {
  children: React.ReactNode;
};

const TabContext = createContext<TabContextType | undefined>(undefined);

export const TabProvider: React.FC<TabProviderProps> = ({ children }) => {
  const [selectedTab, setSelectedTab] = useState<Tabs>("Content");
  const [selectedRenderer, setSelectedRenderer] = useState<Renderers>("raw");

  const value = useMemo(
    () => ({
      selectedTab,
      setSelectedTab,
      selectedRenderer,
      setSelectedRenderer,
    }),
    [selectedTab, selectedRenderer],
  );

  return <TabContext.Provider value={value}>{children}</TabContext.Provider>;
};

export const useTab = (): TabContextType => {
  const context = useContext(TabContext);
  if (!context) {
    throw new Error("useTab must be used within a TabProvider");
  }
  return context;
};
