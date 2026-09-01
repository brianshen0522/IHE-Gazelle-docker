// TODO: check if duplicate with the one from UI library else move to UI library
import React from "react";

export type CheckboxOptionProps = {
  id: string;
  type: string;
  checked: boolean;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  htmlFor: string;
  children: React.ReactNode;
};

const CheckboxOption = ({ id, type, checked, onChange, htmlFor, children }: CheckboxOptionProps) => {
  return (
    <div className="flex items-center gap-2">
      <input id={id} type={type} checked={checked} onChange={onChange} className="accent-blue cursor-pointer" />
      <label htmlFor={htmlFor}>{children}</label>
    </div>
  );
};

export default CheckboxOption;
