'use client'
import React from 'react';
import RenderSanitizedHTML from "@shared/services/RenderSanitizedHTML";

type HomeContentProps = {
  htmlHomeContent: string;
}

const HomeContent = ({htmlHomeContent}: HomeContentProps) => {
  const renderingContent = () => {
    return htmlHomeContent !== undefined && htmlHomeContent?.length > 13 ?
        <RenderSanitizedHTML untrustedHTML={htmlHomeContent}/>
        : <></>;
  }

  return <div className="flex flex-row">
    {renderingContent()}
  </div>;
};

export default HomeContent;