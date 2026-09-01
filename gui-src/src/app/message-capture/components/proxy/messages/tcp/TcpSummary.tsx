import { InfoRow } from "@gazelle/gazelle-component-ui";
import { MsgOverviewProps, TcpMessage } from "@/app/message-capture/components/proxy/Types";
import useDateFormat from "@/shared/hooks/useDateFormat";

const TcpSummary = ({ selectedRow }: MsgOverviewProps) => {
  const { content = {} } = selectedRow || {};
  const formatDate = useDateFormat(false);
  type MapContent = TcpMessage["content"];

  const dataMap = [
    {
      label: "Capture date",
      value: (content: MapContent) => formatDate(content.captureDate),
      show: selectedRow.type !== "CONNECTION_ERROR",
    },
    {
      label: "Message size",
      value: (content: MapContent) => content.sizeOfMessage + " bytes",
      show: content.sizeOfMessage && selectedRow.type !== "CONNECTION_ERROR",
    },
  ];

  return (
    <article className="flex flex-col w-full gap-4">
      <section className="flex justify-between">
        <div>
          {dataMap
            .filter((item) => item.show)
            .map((item) => (
              <InfoRow key={item.label} label={item.label} value={item.value(content)} />
            ))}
        </div>
      </section>
    </article>
  );
};

export default TcpSummary;
