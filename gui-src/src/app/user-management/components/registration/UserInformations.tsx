"use client";
import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
  validatePasswordConfirmation,
  validateEmail,
  validateUserNames,
  validatePassword,
  isValidInput,
  isStepValid,
} from "@user-management/utils/validation";
import { Input } from "@gazelle/gazelle-component-ui";
import { useIsValidStepContext } from "../../context/IsValidStepContext";
import { UserInformationsProps } from "./types";

const UserInformations = ({ userInfos, setUserInfos }: UserInformationsProps) => {
  const { t } = useTranslation();
  const { firstName, lastName, email, password, passwordConfirmation } = userInfos;
  const { setIsValidStep } = useIsValidStepContext();

  const handleChange = (field: string) => (value: string) => {
    setUserInfos((prev) => ({ ...prev, [field]: value }));
  };

  useEffect(() => {
    const isValid = isStepValid(
      { firstName, lastName, email, password, passwordConfirmation },
      {
        firstName: validateUserNames,
        lastName: validateUserNames,
        email: validateEmail,
        password: validatePassword,
        passwordConfirmation: (input: string) => validatePasswordConfirmation(input, password),
      },
    );
    setIsValidStep(isValid);
  }, [email, firstName, lastName, password, passwordConfirmation, setIsValidStep]);

  return (
    <div className="flex flex-col gap-4 w-full">
      <Input
        id="firstName"
        label={t("gzl.gum.first_name")}
        type="text"
        value={firstName}
        setValue={handleChange("firstName")}
        placeholder={t("gzl.gum.first_name")}
        data-testid={`input-${firstName}`}
        error={isValidInput(firstName, validateUserNames) ? undefined : t("gzl.gum.invalid_characters")}
      />
      <Input
        id="lastName"
        label={t("gzl.gum.last_name")}
        type="text"
        value={lastName}
        setValue={handleChange("lastName")}
        placeholder={t("gzl.gum.last_name")}
        data-testid={`input-${lastName}`}
        isValidInput={isValidInput(lastName, validateUserNames)}
        error={isValidInput(lastName, validateUserNames) ? undefined : t("gzl.gum.invalid_characters")}
      />
      <Input
        id="email"
        label={t("gzl.gum.email")}
        placeholder={t("gzl.gum.email")}
        type="email"
        value={email}
        setValue={handleChange("email")}
        isValidInput={isValidInput(email, validateEmail)}
        error={isValidInput(email, validateEmail) ? undefined : t("gzl.gum.invalid_email")}
        data-testid="email"
      />
      <Input
        id="password"
        label={t("gzl.gum.password")}
        placeholder={t("gzl.gum.password")}
        type="password"
        value={password}
        setValue={handleChange("password")}
        isValidInput={isValidInput(password, validatePassword)}
        error={isValidInput(password, validatePassword) ? undefined : t("gzl.gum.invalid_password")}
        data-testid="password"
        showPasswordToggle
      />
      <Input
        id="passwordConfirmation"
        label={t("gzl.gum.password_confirmation")}
        placeholder={t("gzl.gum.password_confirmation")}
        type="password"
        value={passwordConfirmation}
        setValue={handleChange("passwordConfirmation")}
        isValidInput={isValidInput(passwordConfirmation, (input: string) => validatePasswordConfirmation(input, password))}
        error={
          isValidInput(passwordConfirmation, (input: string) => validatePasswordConfirmation(input, password))
            ? undefined
            : t("gzl.gum.passwords_dont_match")
        }
        data-testid="passwordConfirmation"
        showPasswordToggle
      />
      <div className="w-full justify-start">{t("gzl.gum.password_requirements")}</div>
      <div className="w-full justify-start font-semibold">{t("gzl.gum.required_fields")}</div>
    </div>
  );
};

export default UserInformations;
