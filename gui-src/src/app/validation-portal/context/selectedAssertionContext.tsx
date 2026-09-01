"use client";
import React, { createContext, useContext, useMemo, useState } from "react";
import { AssertionReportDTO } from "@/shared/types/validation/types";

type ReportAssertionsContextType = {
  selectedAssertion: AssertionReportDTO | undefined;
  setSelectedAssertion: React.Dispatch<React.SetStateAction<AssertionReportDTO | undefined>>;
  assertionsWithLocation: AssertionReportDTO[];
  setAssertionsWithLocation: React.Dispatch<React.SetStateAction<AssertionReportDTO[]>>;
};

type ReportAssertionsProviderProps = {
  children: React.ReactNode;
};

const ReportAssertionsContext = createContext<ReportAssertionsContextType | undefined>(undefined);

export const ReportAssertionsProvider: React.FC<ReportAssertionsProviderProps> = ({ children }) => {
  const [selectedAssertion, setSelectedAssertion] = useState<AssertionReportDTO | undefined>(undefined);
  const [assertionsWithLocation, setAssertionsWithLocation] = useState<AssertionReportDTO[]>([]);

  const value = useMemo(
    () => ({
      selectedAssertion,
      setSelectedAssertion,
      assertionsWithLocation,
      setAssertionsWithLocation,
    }),
    [selectedAssertion, assertionsWithLocation],
  );

  return <ReportAssertionsContext.Provider value={value}>{children}</ReportAssertionsContext.Provider>;
};

export const useReportAssertions = (): ReportAssertionsContextType => {
  const context = useContext(ReportAssertionsContext);
  if (!context) {
    throw new Error("useReportAssertions must be used within a ReportAssertionsContextProvider");
  }
  return context;
};
