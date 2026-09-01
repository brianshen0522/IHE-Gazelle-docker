import React from 'react';
import { readHTMLHomeContent } from "@home/actions";
import HomePageClient from "./HomePageClient";

export default async function Home() {
  const htmlHomeContent = await readHTMLHomeContent();

  return <HomePageClient htmlHomeContent={htmlHomeContent} />;
}
