"use client";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import TestsList from "./TestsList";
import InfoBanner from "./InfoBanner";
import { ToastContainer } from "react-toastify";

export default function TestSuiteContent() {
  return (
    <SidePanelProvider>
      <InfoBanner />
      <TestsList />
      <ToastContainer />
    </SidePanelProvider>
  );
}
