"use client";

import { createContext, useCallback, useContext, useMemo } from "react";
import { FormProvider, useForm, UseFormReturn } from "react-hook-form";

type SimulationFormContextType = {
  form: UseFormReturn<Record<string, unknown>> | null;
  validateForm: () => Promise<boolean>;
  resetForm: () => void;
};

export const SimulationFormContext = createContext<SimulationFormContextType>({
  form: null,
  validateForm: async () => false,
  resetForm: () => {},
});

export const SimulationFormContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const form = useForm();

  const validateForm = useCallback(async () => {
    const isValid = await form.trigger();
    if (!isValid) {
      const firstErrorField = Object.keys(form.formState.errors)[0];
      if (firstErrorField) {
        form.setFocus(firstErrorField as keyof typeof form.getValues);
        const el = document.querySelector(`[id="${firstErrorField}"]`);
        el?.scrollIntoView({ behavior: "smooth", block: "center" });
      }
    }
    return isValid;
  }, [form]);

  const resetForm = useCallback(() => {
    form.reset();
  }, [form]);

  const value = useMemo(
    () => ({
      form,
      validateForm,
      resetForm,
    }),
    [form, validateForm, resetForm],
  );

  return (
    <SimulationFormContext.Provider value={value}>
      <FormProvider {...form}>
        <form onSubmit={(e) => e.preventDefault()}>{children}</form>
      </FormProvider>
    </SimulationFormContext.Provider>
  );
};

export const useSimulationForm = () => {
  const context = useContext(SimulationFormContext);
  if (!context) throw new Error("useSimulationForm must be used within a SimulationFormProvider");
  return context;
};
