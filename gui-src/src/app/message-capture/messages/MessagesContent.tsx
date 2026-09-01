"use client";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import MessagesList from "@/app/message-capture/components/proxy/messages/MessagesList";
import { ToastContainer } from "react-toastify";

export default function MessagesContent() {
  return (
    <SidePanelProvider>
      <MessagesList />
      <ToastContainer />
    </SidePanelProvider>
  );
}
