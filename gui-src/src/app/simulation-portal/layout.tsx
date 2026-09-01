import { ReactNode } from "react";
import { SequenceExecutionContextProvider } from "@simulation-portal/context/SequenceExecutionContext";

export default function SimulationPortalLayout({ children }: Readonly<{ children: ReactNode }>) {
  return <SequenceExecutionContextProvider>{children}</SequenceExecutionContextProvider>;
}
