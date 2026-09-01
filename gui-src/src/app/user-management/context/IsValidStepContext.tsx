"use client";
import React, { createContext, useContext, useState, Dispatch, SetStateAction } from "react";

export type IsValidStepContextProps = {
  isValidStep: boolean;
  setIsValidStep: Dispatch<SetStateAction<boolean>>;
};

const IsValidStepContext = createContext<IsValidStepContextProps>({} as IsValidStepContextProps);

export function IsValidStepContextProvider({ children }: Readonly<{ children: React.ReactNode }>) {
  const [isValidStep, setIsValidStep] = useState(false);

  const contextValue = React.useMemo(
    () => ({
      isValidStep,
      setIsValidStep,
    }),
    [isValidStep, setIsValidStep]
  );

  return <IsValidStepContext.Provider value={contextValue}>{children}</IsValidStepContext.Provider>;
}

export function useIsValidStepContext() {
  return useContext(IsValidStepContext);
}
