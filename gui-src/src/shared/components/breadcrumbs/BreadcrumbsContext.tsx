"use client";

import {BreadcrumbItem, BreadcrumbsProps} from "@gazelle/gazelle-component-ui";
import {createContext, useContext, useMemo, useState} from "react";

type BreadcrumbsContextType = {
    breadCrumbsProps: BreadcrumbsProps | null;
    setBreadCrumbsProps: React.Dispatch<React.SetStateAction<BreadcrumbsProps | null>>;
    breadCrumbsItems?: BreadcrumbItem[];
    setBreadCrumbsItems: React.Dispatch<React.SetStateAction<BreadcrumbItem[] | undefined>>;
};

export const BreadCrumbsContext = createContext<BreadcrumbsContextType>({
    breadCrumbsProps: null,
    setBreadCrumbsProps: () => {},
    breadCrumbsItems: undefined,
    setBreadCrumbsItems: () => {},
});

export const BreadCrumbsContextProvider = ({ children }: { children: React.ReactNode }) => {
    const [breadCrumbsProps, setBreadCrumbsProps] = useState<BreadcrumbsProps | null>(null);
    const [breadCrumbsItems, setBreadCrumbsItems] = useState<BreadcrumbItem[] | undefined>(undefined);

    const value = useMemo(() => ({
        breadCrumbsProps,
        setBreadCrumbsProps,
        breadCrumbsItems,
        setBreadCrumbsItems
    }), [breadCrumbsProps, breadCrumbsItems]);

    return (
        <BreadCrumbsContext.Provider value={value}>
            {children}
        </BreadCrumbsContext.Provider>
    );
};

export function useBreadCrumbsContext(): BreadcrumbsContextType {
    const context = useContext(BreadCrumbsContext);
    if (!context) {
        throw new Error("useBreadCrumbsContext must be used within a BreadCrumbsContextProvider");
    }
    return context;
}