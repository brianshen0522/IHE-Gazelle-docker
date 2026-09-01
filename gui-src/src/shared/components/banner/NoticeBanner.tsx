// TODO: This component has been moved to the UI lib
// Delete after all components are migrated to use the new NoticeBanner component from the UI library
import { PropsWithChildren } from "react";

export interface NoticeBannerProps {
  color?: "blue" | "red" | "purple" | "magenta" | "yellow" | "green";
  weight?: "normal" | "semibold" | "bold";
  className?: string;
}

const NoticeBanner = ({ color = "blue", weight = "normal", className, children }: PropsWithChildren<NoticeBannerProps>) => {
  const colorClasses: Record<string, string> = {
    blue: "border-blue text-blue bg-blue/5",
    red: "border-red text-red bg-red/5",
    purple: "border-purple text-purple bg-purple/5",
    magenta: "border-magenta text-magenta bg-magenta/5",
    yellow: "border-yellow text-yellow bg-yellow/5",
    green: "border-green text-green bg-green/5",
  };

  const classes = `whitespace-pre-line rounded border ${colorClasses[color]} font-${weight} p-1 my-1 ${className || ""}`;

  return <div className={classes}>{children}</div>;
};

export default NoticeBanner;
