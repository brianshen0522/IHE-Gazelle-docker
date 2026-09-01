"use client";

import { useEffect, useState } from "react";
import { MigrationError } from "@/types/migration";
import { fetchFailedReports } from "@/lib/api";
import { ErrorTypeBadge } from "./ErrorTypeBadge";

type RetryModalProps = {
  isOpen: boolean;
  onClose: () => void;
  failedCount: number;
  onConfirm: (specificIgnoredOids: string[]) => void;
};

export function RetryModal({ isOpen, onClose, failedCount, onConfirm }: RetryModalProps) {
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [errors, setErrors] = useState<MigrationError[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [ignoredOids, setIgnoredOids] = useState<Set<string>>(new Set());

  // Fetch errors when modal opens or page/pageSize changes
  useEffect(() => {
    if (!isOpen) return;

    const loadErrors = async () => {
      setLoading(true);
      try {
        const offset = currentPage * pageSize;
        const response = await fetchFailedReports(offset, pageSize);
        setErrors(response.errors);
        setTotal(response.total);
      } catch (error) {
        console.error("Failed to fetch failed reports:", error);
        setErrors([]);
        setTotal(0);
      } finally {
        setLoading(false);
      }
    };

    loadErrors();
  }, [isOpen, currentPage, pageSize]);

  // Reset state when modal closes
  useEffect(() => {
    if (!isOpen) {
      setCurrentPage(0);
      setIgnoredOids(new Set());
    }
  }, [isOpen]);

  const handleToggleIgnore = (oid: string) => {
    const newIgnored = new Set(ignoredOids);
    if (newIgnored.has(oid)) {
      newIgnored.delete(oid);
    } else {
      newIgnored.add(oid);
    }
    setIgnoredOids(newIgnored);
  };

  const handleSelectAllOnPage = () => {
    const missingInputErrors = errors.filter((error) => error.type === "MISSING_INPUT");
    const allSelected = missingInputErrors.every((error) => ignoredOids.has(error.evsOid));

    const newIgnored = new Set(ignoredOids);
    if (allSelected) {
      // Deselect all on this page
      missingInputErrors.forEach((error) => newIgnored.delete(error.evsOid));
    } else {
      // Select all on this page
      missingInputErrors.forEach((error) => newIgnored.add(error.evsOid));
    }
    setIgnoredOids(newIgnored);
  };

  const handleConfirm = () => {
    onConfirm(Array.from(ignoredOids));
  };

  const totalPages = Math.ceil(total / pageSize);
  const canGoNext = currentPage < totalPages - 1;
  const canGoPrev = currentPage > 0;

  const ignoredCount = ignoredOids.size;

  const missingInputErrorsOnPage = errors.filter((error) => error.type === "MISSING_INPUT");
  const allMissingInputSelected =
    missingInputErrorsOnPage.length > 0 &&
    missingInputErrorsOnPage.every((error) => ignoredOids.has(error.evsOid));

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl max-w-5xl w-full max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="px-6 py-4 border-b">
          <h2 className="text-xl font-semibold text-gray-900">Retry Failed Reports</h2>
          <p className="text-sm text-gray-600 mt-1">
            All {failedCount} failed reports will be retried. Check &quot;Ignore Input&quot; for reports you want to create without
            input attachments (for MISSING_INPUT errors only).
          </p>
        </div>

        {/* Body - Scrollable */}
        <div className="flex-1 overflow-y-auto px-6 py-4">
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="text-gray-500">Loading failed reports...</div>
            </div>
          ) : errors.length === 0 ? (
            <div className="flex items-center justify-center py-8">
              <div className="text-gray-500">No failed reports found</div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-28">
                      <div className="flex items-center gap-2">
                        <span>Ignore Input</span>
                        {missingInputErrorsOnPage.length > 0 && (
                          <input
                            type="checkbox"
                            checked={allMissingInputSelected}
                            onChange={handleSelectAllOnPage}
                            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            title="Toggle all MISSING_INPUT on this page"
                          />
                        )}
                      </div>
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      EVS OID
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Error Type
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Error Message
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Occurred At
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {errors.map((error) => {
                    const canIgnore = error.type === "MISSING_INPUT";
                    const isIgnored = ignoredOids.has(error.evsOid);
                    const occurredDate = new Date(error.occurredAt);

                    return (
                      <tr key={error.evsOid} className={isIgnored ? "bg-gray-50" : ""}>
                        <td className="px-3 py-3 whitespace-nowrap">
                          {canIgnore && (
                            <input
                              type="checkbox"
                              checked={isIgnored}
                              onChange={() => handleToggleIgnore(error.evsOid)}
                              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                              title="Ignore this error"
                            />
                          )}
                        </td>
                        <td className="px-3 py-3 whitespace-nowrap text-sm font-mono text-gray-900">
                          {error.evsOid}
                        </td>
                        <td className="px-3 py-3 whitespace-nowrap">
                          <ErrorTypeBadge type={error.type} />
                        </td>
                        <td className="px-3 py-3 text-sm text-gray-700 max-w-md truncate" title={error.message}>
                          {error.message}
                        </td>
                        <td className="px-3 py-3 whitespace-nowrap text-sm text-gray-500">
                          {occurredDate.toLocaleString()}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t bg-gray-50">
          {/* Pagination Controls */}
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-4">
              <div className="text-sm text-gray-700">
                Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, total)} of {total}{" "}
                errors
              </div>
              <div className="flex items-center gap-2">
                <label htmlFor="pageSize" className="text-sm text-gray-700">
                  Per page:
                </label>
                <select
                  id="pageSize"
                  value={pageSize}
                  onChange={(e) => {
                    setPageSize(Number(e.target.value));
                    setCurrentPage(0);
                  }}
                  className="border border-gray-300 rounded px-2 py-1 text-sm"
                >
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={50}>50</option>
                </select>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={!canGoPrev}
                className="px-3 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="text-sm text-gray-700">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={!canGoNext}
                className="px-3 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Retrying all <span className="font-semibold">{failedCount}</span> failed report{failedCount !== 1 ? "s" : ""}
              {ignoredCount > 0 && (
                <>
                  {" "}
                  (<span className="font-semibold">{ignoredCount}</span> will ignore input{ignoredCount !== 1 ? "s" : ""})
                </>
              )}
            </div>
            <div className="flex gap-3">
              <button
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirm}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded hover:bg-blue-700"
              >
                Retry All {failedCount} Report{failedCount !== 1 ? "s" : ""}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
