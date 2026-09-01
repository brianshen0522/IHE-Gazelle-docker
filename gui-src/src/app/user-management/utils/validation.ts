import XRegExp from "xregexp";

const emailPattern = XRegExp("^[a-zA-Z0-9.!#$%&'*+-/=?^_`{|}~]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$");
const namePattern = /^[^_!¡?÷¿/\\+=@#$%ˆ&*(){}|~<>;:[\]]*$/i;
const passwordPattern = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\d)(?=.*?[#?!@$%^&*-]).{8,}$/;
const urlPattern = /^(http:\/\/|https:\/\/)((www\.)?(ftp\.)?)[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+([?].*)?$/;

export const validateEmail = (email: string) => {
  if (!email) return true;
  return emailPattern.test(email);
};

export const validateUserNames = (name: string) => {
  if (name.length > 255) return false;
  return namePattern.test(name);
};

export const validateOrganizationName = (name: string) => {
  if (!name) return true;
  return !(name.length == 0 || name.length > 255);

};

export const validatePassword = (password: string) => {
  if (!password) return true;
  return passwordPattern.test(password);
};

export const validatePasswordConfirmation = (passwordConfirmation: string, password: string) => {
  if (!passwordConfirmation) return true;
  return passwordConfirmation === password;
};

export const validateShortName = (name: string) => name.length <= 32;

export const validateUrl = (url: string) => {
  if (!url) return true;
  return urlPattern.test(url.trim());
};

export function isValidInput<T>(input: T, validator: (input: T) => boolean | null): boolean {
  const result = validator(input);
  return result ?? true;
}

export function isStepValid<T extends Record<string, any>>(values: T, validators: { [K in keyof T]: (value: T[K]) => boolean | null }): boolean {
  return Object.keys(validators).every((key) => {
    const value = values[key as keyof T];
    const validator = validators[key as keyof T];
    return value !== "" && validator(value) !== false;
  });
}
