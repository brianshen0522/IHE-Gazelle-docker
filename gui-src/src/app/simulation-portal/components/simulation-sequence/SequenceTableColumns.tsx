"use client";

import { createColumnHelper } from "@tanstack/react-table";
import { sequenceIndexNameMapping } from "@simulation-portal/service/constants";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import SequenceActionButton from "@simulation-portal/components/simulation-sequence/SequenceActionButton";
import { TriangleAlert } from "lucide-react";

const columnHelper = createColumnHelper<SimulationSequence>();
export const sequenceColumns = [
  columnHelper.accessor("id", {
    id: "id",
    header: () => <span>{sequenceIndexNameMapping.id}</span>,
    cell: (info) => {
      const simulationSequence = info.row.original;
      const sequenceVersion = simulationSequence.version ? ` (${simulationSequence.version})` : "";
      return (
        <div className="flex items-center gap-1">
          <span>{`${info.getValue() ?? ""}${sequenceVersion}`}</span>
          {!simulationSequence.valid &&
              <>
                  <span className="flex rounded bg-orange opacity-70 text-white font-semibold p-1">
                      <TriangleAlert />
                      Invalid
                  </span>
              </>
          }
          {!simulationSequence.runnable &&
              <span className="rounded bg-blue opacity-70 text-white font-semibold p-1">
                  Manual
              </span>
          }
        </div>
      );
    },
  }),
  columnHelper.accessor("testedRoles", {
    id: "testedRole",
    header: () => <span>{sequenceIndexNameMapping.testedRole}</span>,
    cell: (info) => {
      const roles = info.getValue();
      return roles ? roles.map((role) => role.name).join(", ") : "";
    },
    enableSorting: false,
  }),
  columnHelper.accessor("transactions", {
    id: "transactions",
    header: () => <span>{sequenceIndexNameMapping.transactions}</span>,
    cell: (info) => {
      const transactions = info.getValue();
      return transactions ? transactions.join(", ") : "";
    },
    enableSorting: false,
  }),
  columnHelper.display({
    id: "actions",
    header: () => <span>Actions</span>,
    cell: (info) => {
      const simulationSequence = info.row.original;
      return (
        <div className="flex items-center justify-center w-full">
          <SequenceActionButton simulationSequence={simulationSequence} />
        </div>
      );
    },
    enableColumnFilter: false,
    enableSorting: false,
  }),
];
