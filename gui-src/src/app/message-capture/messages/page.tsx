import ContentHeaderWrapper from "@/shared/components/layout/ContentHeaderWrapper";
import MessagesContent from "./MessagesContent";

export default function Messages() {
  return (
    <div className="flex flex-col w-full">
      <ContentHeaderWrapper id="messages-header" title="Messages" />
      <MessagesContent />
    </div>
  );
}
