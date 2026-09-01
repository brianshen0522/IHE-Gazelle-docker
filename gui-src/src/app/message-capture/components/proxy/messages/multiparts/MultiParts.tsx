import { useState } from "react";
import { CollapsibleSection, Pagination, SectionTitle } from "@gazelle/gazelle-component-ui";
import { MultiPartsProps, Part } from "@/app/message-capture/components/proxy/messages/multiparts/Types";
import { useGetParts } from "@message-capture/hooks/swr-requests/useGetParts";
import MultiPartHeaders from "@message-capture/components/proxy/messages/multiparts/MultiPartHeaders";
import MultiPartContent from "@message-capture/components/proxy/messages/multiparts/MultiPartContent";

const MultiParts = ({ hasHTTPParts, id }: MultiPartsProps) => {
  const [openSections, setOpenSections] = useState<{ [key: string]: boolean }>({});
  const [expandAll, setExpandAll] = useState(false);
  const [offset, setOffset] = useState(0);
  const limit = 2;
  const { data: parts, contentRange } = useGetParts(hasHTTPParts, id as string, offset, limit);

  const multipartContent = parts?.map((part: Part) => {
    return { ...JSON.parse(part.content), id: part.id, date: part.date, references: part.references };
  });

  const handleToggle = (id: string, isOpen: boolean) => {
    setOpenSections((prev) => ({ ...prev, [id]: isOpen }));
  };

  const handleExpandAll = () => {
    setExpandAll(!expandAll);
    const newOpenSections = multipartContent.reduce((acc: { [key: string]: boolean }, content: Part) => {
      acc[content.id] = !expandAll;
      return acc;
    }, {} as { [key: string]: boolean });
    setOpenSections(newOpenSections);
  };

  return (
    <>
      {parts && (
        <>
          <SectionTitle id="multipart-headers" title="Multiparts" />
          <button type="button" onClick={handleExpandAll} className="hover:text-purple flex justify-end">
            Expand/Collapse all
          </button>
          {multipartContent?.map((content: Part, index: number) => (
            <CollapsibleSection
              key={content.id}
              id={`multi-part-${content.id}`}
              buttonContent={<MultiPartHeaders content={content} index={index + offset} />}
              expanded={openSections[content.id] || false}
              onToggle={(isOpen) => handleToggle(content.id, isOpen)}
            >
              <MultiPartContent content={content} sectionId={content.id} expandAll={expandAll} />
            </CollapsibleSection>
          ))}
          <Pagination offset={offset} setOffset={setOffset} limit={limit} totalItems={contentRange} />
        </>
      )}
    </>
  );
};

export default MultiParts;
