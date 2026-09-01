import { useState, useMemo, useEffect } from "react";

export interface InputItem {
  id: string;
  value: string;
}

export interface InputConfig {
  id: string;
  required?: boolean;
  defaultValue?: string;
  possibleValues?: string[];
}

export interface PersistedInput {
  name: string;
  value: string;
  type: string;
}

/**
 * Generic hook for managing form/file inputs with validation
 * @param inputs - Array of input configurations (with id and required flag)
 * @param onUnsavedChange - Optional callback when input state changes
 * @param persistedInputs - Optional array of persisted inputs from server (for validation)
 * @returns Input management utilities and validation state
 */
export function useInputManagement<T extends InputConfig>(
  inputs?: T[],
  onUnsavedChange?: (hasChanges: boolean) => void,
  persistedInputs?: PersistedInput[],
) {
  const [uploadedInputs, setUploadedInputs] = useState<InputItem[]>([]);
  const [isInitialized, setIsInitialized] = useState(false);

  // Initialize inputs with default values and persisted values
  useEffect(() => {
    if (!inputs || isInitialized) return;

    const initialInputs: InputItem[] = [];

    inputs.forEach((input) => {
      // Priority: persisted value > default value
      const persistedInput = persistedInputs?.find((p) => p.name === input.id);

      if (persistedInput?.value) {
        initialInputs.push({ id: input.id, value: persistedInput.value });
      } else if (input.defaultValue) {
        initialInputs.push({ id: input.id, value: input.defaultValue });
      }
    });

    if (initialInputs.length > 0) {
      setUploadedInputs(initialInputs);
    }

    setIsInitialized(true);
  }, [inputs, persistedInputs, isInitialized]);

  const updateInput = (inputId: string, value: string | null) => {
    setUploadedInputs((prev) => {
      // Remove input only if value is null (not empty string)
      if (value === null) {
        return prev.filter((input) => input.id !== inputId);
      }

      // Update existing or add new input
      const existingIndex = prev.findIndex((input) => input.id === inputId);
      if (existingIndex >= 0) {
        const updated = [...prev];
        updated[existingIndex] = { id: inputId, value };
        return updated;
      }

      return [...prev, { id: inputId, value }];
    });

    onUnsavedChange?.(value !== null && value !== "");
  };

  const requiredInputs = useMemo(() => inputs?.filter((input) => input.required) ?? [], [inputs]);

  const areRequiredInputsFilled = useMemo(() => {
    return requiredInputs.every((requiredInput) => {
      // Check session state first
      const sessionInput = uploadedInputs.find((uploaded) => uploaded.id === requiredInput.id);
      // If exists in session, check it has a non-empty value
      if (sessionInput !== undefined) {
        return sessionInput.value !== "";
      }

      // Check persisted inputs - name field contains the input ID
      const inPersisted = persistedInputs?.some((persisted) => persisted.name === requiredInput.id && persisted.value);

      return !!inPersisted;
    });
  }, [requiredInputs, uploadedInputs, persistedInputs]);

  const clearInputs = () => {
    setUploadedInputs([]);
    onUnsavedChange?.(false);
  };

  const getInputValue = (inputId: string): string | undefined => {
    return uploadedInputs.find((input) => input.id === inputId)?.value;
  };

  return {
    uploadedInputs,
    updateInput,
    areRequiredInputsFilled,
    clearInputs,
    getInputValue,
  };
}
