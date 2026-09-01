import { Route } from "next";
import { sequence } from "@simulation-portal/service/constants";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import Link from "next/link";

type ActionProps = {
  simulationSequence: SimulationSequence;
};

const SequenceActionButton = ({ simulationSequence }: ActionProps) => {
  const href = `${sequence.basePath}/${simulationSequence.id}` as Route;

  return (
    <Link
      href={href}
      title="Run a simulation"
      className="inline-flex items-center gap-2 text-blue hover:text-visited_link transition-colors duration-200 underline"
    >
      {simulationSequence.runnable && simulationSequence.valid ? "Run" : "View"}
    </Link>
  );
};

export default SequenceActionButton;
