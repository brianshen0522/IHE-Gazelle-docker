
import React from "react";
import { Trash2, Upload } from "lucide-react";

interface FileUploadProps {
  label?: string | React.ReactNode;
  labelWithFile: string;
  labelWithoutFile: string;
  isValid: boolean;
  value: File | null;
  setValue: (value: File | null) => void;
  required?: boolean;
  error?: string;
}

const FileUpload = ({ label, labelWithFile, labelWithoutFile, isValid, value, setValue, required, error }: FileUploadProps) => {
  const errorClass = error || !isValid ? "border-red text-red outline-none focus:ring-1 focus:ring-red focus:border-red" : "";

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event?.target?.files?.[0] !== undefined) {
      const file = event.target.files[0];
      setValue(file);
    }
  };

  const handleRemove = () => {
    setValue(null);
  };

  return (
    <div>
      <label className={`font-semibold ${error || !isValid ? "text-red" : ""}`} htmlFor="file_input">
        {label}
        {required && <span className="text-red-500 ml-0.5">*</span>}
      </label>
      <div className="flex items-center gap-2">
        {!value && (
          <>
            <input id="file_input" type="file" onChange={handleFileChange} className="hidden" />
            <label
              htmlFor="file_input"
              className={[
                "flex items-center space-x-2 rounded-lg px-4 py-2 cursor-pointer font-medium bg-blue text-white hover:bg-darkblue",
                errorClass,
              ].join(" ")}
            >
              <Upload size={16} />
              <span>{labelWithoutFile}</span>
            </label>
          </>
        )}
        {value && (
          <>
            <a href={URL.createObjectURL(value)} download={labelWithFile} className="text-blue text-sm truncate max-w-xs italic hover:underline">
              {labelWithFile}
            </a>
            <button
              type="button"
              onClick={handleRemove}
              className="flex items-center space-x-1 text-red-500 hover:text-red-600 border border-red-500 hover:bg-red-50 rounded-lg px-1 py-1 text-sm font-medium"
            >
              <Trash2 size={16} />
              <span>Remove</span>
            </button>
          </>
        )}
      </div>
      {error && <p className="text-red">{error}</p>}
    </div>
  );
};

export default FileUpload;
