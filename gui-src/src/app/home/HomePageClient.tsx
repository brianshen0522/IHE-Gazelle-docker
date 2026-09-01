"use client";
import React from 'react';
import HomeContent from '@home/components/homeContent/HomeContent';
import ContentHeaderWrapper from "@shared/components/layout/ContentHeader";
import { useTranslation } from "react-i18next";

interface HomePageClientProps {
  htmlHomeContent: string;
}

export default function HomePageClient({ htmlHomeContent }: Readonly<HomePageClientProps>) {
  const { t } = useTranslation();
  const breadcrumbs = [{ label: t("gzl.user.interface.home"), url: '/home' }];

  return (
    <ContentHeaderWrapper breadcrumbsItems={breadcrumbs} title="" secured={false}>
      <div className='flex flex-col gap-4'>
        <HomeContent htmlHomeContent={htmlHomeContent}/>
      </div>
    </ContentHeaderWrapper>
  );
}

