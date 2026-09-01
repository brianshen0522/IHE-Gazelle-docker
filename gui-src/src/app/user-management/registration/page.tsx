import React from "react";
import RegistrationForm from "@user-management/components/registration/RegistrationForm";

import { getGUMConfigurations } from "../actions";
import { readTestBedConfigurations } from "@home/actions";

export default async function Registration() {
  const { data: GUMConfigurations } = await getGUMConfigurations();
  const testBedConfigurations = await readTestBedConfigurations();

  return <RegistrationForm configs={GUMConfigurations} privacyPolicyUrl={testBedConfigurations.privacyPolicyUrl} />;
}
