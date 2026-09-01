"use client";
import { Badge, type BadgeProps } from "@gazelle/gazelle-component-ui";

export interface StatusBadgeCellProps {
  id: string;
  value: boolean;
  trueVariant: BadgeProps["variant"];
  falseVariant: BadgeProps["variant"];
  trueLabel: string;
  falseLabel: string;
}

// Generic table cell component that displays a Badge based on a boolean value
// Commonly used for status indicators like active/inactive, delegated/local, etc.
export const StatusBadgeCell = ({ id, value, trueVariant, falseVariant, trueLabel, falseLabel }: StatusBadgeCellProps) => {
  return (
    <div className="mx-3 my-1">
      <Badge id={id} variant={value ? trueVariant : falseVariant}>
        {value ? trueLabel : falseLabel}
      </Badge>
    </div>
  );
};
