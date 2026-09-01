import { Item } from "../Types";

// Type definitions for the validation
export type ItemValidationResult = {
  restReportLocation: string;
  guiReportLocation: string;
  status: string;
};

export type ValidationRequest = {
  itemId?: string;
  validator: ValidatorItem;
  serviceName?: string;
  contentPath?: string;
  syntax?: string;
  selector?: string;
};

export type ValidationPart = {
  value: string;
  label: string;
  path: string;
  syntax: string;
  selector?: string;
  isEnable: boolean; // Not implemented yet
};

export type ModalValidationProps = {
  title: string;
  btnTriggerText: string;
  validationParts: ValidationPart[];
  partId?: string;
  attachmentId?: string;
  onValidationError: (message: string) => void;
};

export type OptionType = {
  value: string;
  label: string;
};

export type logsResults<T> = Readonly<T>;

export interface ValidationResultProps {
  validationError: string | null;
}

export interface ValidationWrapperProps {
  messageItem: Item;
}

export type ValidatorItem = {
  keyword: string;
  name?: string;
  domain?: string | null;

};

export type ProfileItem = {
  validator: ValidatorItem;
  serviceName: string;
};
