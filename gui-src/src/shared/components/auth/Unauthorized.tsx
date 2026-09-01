"use client";
import { CustomAccessDeniedError } from "@gazelle/gazelle-component-ui";

// This component is used to display a message when a user is not authorized to access a page.
// With an image and a message, it is a simple component that can be used in many places.
export default function Unauthorized() {
  return <CustomAccessDeniedError />;
}
