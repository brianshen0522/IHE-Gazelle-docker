import { LucideIcon } from "lucide-react";

interface AclPolicyDisplayProps {
  icon: LucideIcon;
  label: string;
  description: string;
}

// Displays the current ACL policy with an icon, label, and description.
export const AclPolicyDisplay = ({ icon: Icon, label, description }: AclPolicyDisplayProps) => {
  return (
    <div className="flex items-center gap-2">
      <Icon className="w-10 h-10 text-muted-foreground text-purple" />
      <div className="flex flex-col leading-tight w-full">
        <span className="font-semibold">{label}</span>
        <span className="text-xs text-muted-foreground">{description}</span>
      </div>
    </div>
  );
};
