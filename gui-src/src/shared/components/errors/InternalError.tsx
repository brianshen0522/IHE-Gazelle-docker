import Image from "next/image";
import InternalError from "../../assets/internal-error.png";

type InternalErrorsProps = {
  title?: string;
  message: string;
};

const InternalErrors = ({ title, message }: InternalErrorsProps) => {
  return (
    <div className="flex flex-col items-center justify-center">
      <h3>{title}</h3>
      <Image src={InternalError} alt="500" width={400} height={400} />
      <p className="text-md text-red">{message}</p>
    </div>
  );
};

export default InternalErrors;
