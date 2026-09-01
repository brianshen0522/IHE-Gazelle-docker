import { Input, ToggleSwitch, UploadFile } from "@gazelle/gazelle-component-ui";
import { SupportedInput } from "../../../types/TestModel";
import {useTranslation} from "react-i18next";

interface SupportedInputFieldProps {
  input: SupportedInput;
  inputValue: string;
  uploadedFileName?: string;
  persistedFileName?: string;
  isRunning: boolean;
  onFileChange: (file: File | null) => void;
  onInputChange: (content: string) => void;
}

export function SupportedInputField({
  input,
  inputValue,
  uploadedFileName,
  persistedFileName,
  isRunning,
  onFileChange,
  onInputChange,
}: Readonly<SupportedInputFieldProps>) {
  const { t } = useTranslation();
  const isChecked = inputValue === "true";

  // Check if it's a TEXT input with possible values
  const hasSelectOptions = input.type === "TEXT" && input.possibleValues && input.possibleValues.length > 0;

  return (
    <>
      <label className="block font-medium mb-2">
        {input.label}
        {input.required && <span className="text-red ml-1">*</span>}
      </label>
      {input.type === "FILE" && <UploadFile id={input.id} fileName={uploadedFileName ?? persistedFileName ?? ""} onFileChange={onFileChange} disabled={isRunning} />}
      {input.type === "TEXT" && hasSelectOptions && (
        <select
          id={input.id}
          name={input.id}
          value={inputValue || ""}
          onChange={(e) => onInputChange(e.target.value)}
          disabled={isRunning}
          className="w-full px-3 py-2 border border-grey rounded-lg bg-white hover:border-purple focus:outline-none focus:ring-2 focus:ring-purple disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <option key="default" value="" disabled={true}>
            {t('gzl.texec.select_a_value')}
          </option>
          {input.possibleValues?.map((value) => {
            const isString = typeof value === "string";
            const displayValue = isString ? value : value.value;
            const displayLabel = isString ? value : value.label;
            return (
              <option key={displayValue} value={displayValue} disabled={isRunning}>
                {displayLabel}
              </option>
            );
          })}
        </select>
      )}
      {input.type === "TEXT" && !hasSelectOptions && (
        <Input
          id={input.id}
          label={""}
          htmlFor={input.id}
          value={inputValue}
          type="input"
          name={input.id}
          placeholder={input.id}
          setValue={onInputChange}
          disabled={isRunning}
        />
      )}
      {input.type === "BOOLEAN" && (
        <ToggleSwitch
          id={`default-toggle-${input.id}`}
          status={isChecked ? "Enabled" : "Disabled"}
          checked={isChecked}
          disabled={isRunning}
          onChange={() => onInputChange(isChecked ? "false" : "true")}
        />
      )}
    </>
  );
}
