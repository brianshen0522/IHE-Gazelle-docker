import { useDatahouseItem } from "@hooks/useDatahouseItem";
import { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { useEffect, useState } from "react";

export interface UseGetSimulationReportProps {
  testReportItem?: DatahouseItem;
  simulationReportItem?: DatahouseItem;
  isLoading: boolean;
  error?: string;
}

const useGetSimulationReport = (): UseGetSimulationReportProps => {
  const { location, setReportItem } = useSequenceExecutionContext();
  const [simulationReportRef, setSimulationReportRef] = useState<string | undefined>(undefined);
  const { item: testReportItem, isLoading: testReportLoading, error: testReportError } = useDatahouseItem({ location: location });

  useEffect(() => {
    setSimulationReportRef(() => {
      if (testReportItem && testReportItem?.references.length > 0) {
        return testReportItem?.references[0].value;
      }
    });
  }, [testReportItem]);

  const {
    item: simulationReportItem,
    isLoading: simulationReportLoading,
    error: simulationReportError,
  } = useDatahouseItem({ itemId: simulationReportRef });

  useEffect(() => {
    if (simulationReportItem) {
      setReportItem(simulationReportItem);
    }
  }, [simulationReportItem, setReportItem]);

  const isLoading = testReportLoading || simulationReportLoading;
  const error = testReportError?.message ?? simulationReportError?.message;

  return {
    testReportItem,
    simulationReportItem,
    isLoading,
    error,
  };
};

export default useGetSimulationReport;
