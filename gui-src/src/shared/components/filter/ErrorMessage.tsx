import {CircleAlert} from "lucide-react";

const ErrorMessage = ({ error }: { error?: string }) => {
  return (
    <div className="flex items-center gap-2 text-red font-medium">
      <CircleAlert size={16} className="text-red"/>
      {error}
    </div>
  );
};

export default ErrorMessage;