/*
 * Copyright 2024 Kereval.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { useState } from "react";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import TabRenderers from "@message-capture/components/proxy/renderers/TabRenderers";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";

const TcpContent = ({ data }: DataMessageProps) => {
  const [showRenderer, setShowRenderer] = useState<string>("raw");
  const handleRendererChange = (renderer: string) => {
    setShowRenderer(renderer);
  };

  return (
    <>
      <TabRenderers specificRenderers={["raw"]} showRenderer={showRenderer} onRendererChange={handleRendererChange} />
      {showRenderer === "raw" && <RawRenderer base64Data={data.content.content} dataType="TCP" />}
    </>
  );
};
export default TcpContent;
