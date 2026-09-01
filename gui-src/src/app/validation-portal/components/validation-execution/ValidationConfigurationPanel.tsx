"use client";
import { useState, useMemo } from "react";
import { ValidationProfile, SupportedInput } from "@validation-portal/types/ValidationProfile";
import { Session } from "next-auth";
import { Button, ToggleSwitch, NoticeBanner } from "@gazelle/gazelle-component-ui";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { executeValidation } from "./actions";
import { Route } from "next";
import ConfigurationInput from "./ConfigurationInput";
import { useTranslation } from "react-i18next";

interface ValidationConfigurationPanelProps {
  profile: ValidationProfile;
  serviceName: string;
  profileId: string;
  session: Session | null;
}

export default function ValidationConfigurationPanel({ profile, serviceName, profileId, session }: Readonly<ValidationConfigurationPanelProps>) {
  const { t } = useTranslation();
  const router = useRouter();
  const [reviewEnabled, setReviewEnabled] = useState(false);
  const [fileData, setFileData] = useState<Map<string, { file: File; content: string }>>(new Map());
  const [isExecuting, setIsExecuting] = useState(false);
  const [error, setError] = useState("");
  const [inlineReport, setInlineReport] = useState<Record<string, unknown> | null>(null);

  // Determine inputs to show based on profile inputs
  const inputs: SupportedInput[] = useMemo(() => {
    if (profile.inputs && profile.inputs.length > 0) {
      return profile.inputs;
    }
    // Fallback: single "Content to validate" input if no inputs defined
    return [{ id: "contentToValidate", label: t("gzl.user.interface.validated_content"), required: true }];
  }, [profile.inputs, t]);

  const handleClearAll = () => {
    setFileData(new Map());
  };

  const handleSetFileData = (newFileData: Map<string, { file: File; content: string }>) => {
    setFileData(newFileData);
    if (error) {
      setError("");
    }
  };

  // Check if all required inputs are provided
  const canValidate = useMemo(() => {
    return inputs.filter((input) => input.required !== false).every((input) => fileData.has(input.id));
  }, [inputs, fileData]);

  const handleValidate = async (newFileData?: Map<string, { file: File; content: string }>) => {
    const dataToValidate = newFileData || fileData;
    const requiredInputs = inputs.filter((input) => input.required !== false);
    const allRequiredPresent = requiredInputs.every((input) => dataToValidate.has(input.id));

    if (!allRequiredPresent) {
      toast.error(t("gzl.user.interface.please_upload_all_required_files"));
      return;
    }

    setIsExecuting(true);

    try {
      // Prepare file inputs for Maestro (content already loaded)
      const fileInputs = Array.from(dataToValidate.entries()).map(([id, data]) => ({
        id,
        content: data.content,
        name: data.file.name,
      }));

      const result = await executeValidation({
        profileId,
        serviceName,
        fileInputs,
        session,
      });

      if (result.error) {
        // Server-side error from executeValidation (e.g., auth failure, Maestro API error, JSON parse error)
        setError(result.error);
      } else if (result.reportId) {
        // Redirect to report page
        router.push(`/validation-portal/reports/${result.reportId}` as Route);
      } else if (result.inlineReport) {
        // Standalone mode: report returned inline, display it directly
        setError("");
        setInlineReport(result.inlineReport as Record<string, unknown>);
      }
    } catch (error) {
      // Client-side error during execution (e.g., network failure, unexpected exception)
      const errorMessage = error instanceof Error ? error.message : t("gzl.user.interface.validation_execution_failed");
      setError(errorMessage);
      console.error(error);
    } finally {
      setIsExecuting(false);
    }
  };

  return (
    <div className="space-y-4 p-2">
      {/* Review Toggle */}
      <div className="space-y-2 pb-4">
        <ToggleSwitch
          id="review-toggle"
          label={t("gzl.user.interface.review_uploaded_file_before_validation")}
          checked={reviewEnabled}
          onChange={setReviewEnabled}
          status={reviewEnabled ? t("gzl.user.interface.enabled") : t("gzl.user.interface.disabled")}
          className="font-semibold"
        />

        <ConfigurationInput inputs={inputs} fileData={fileData} setFileData={handleSetFileData} reviewEnabled={reviewEnabled} />
      </div>

      {/* Clear All Button (only show if multiple files) */}
      {inputs.length > 1 && fileData.size > 0 && (
        <div className="flex justify-end">
          <Button id="clear-all" variant="secondary" type="button" onClick={handleClearAll}>
            {t("gzl.user.interface.clear_all")}
          </Button>
        </div>
      )}

      {/* Validation Info */}
      <p className="text-xs text-gray-600 italic">{t("gzl.user.interface.inputs_marked_mandatory")}</p>

      {/* Validate Button */}
      {fileData.size > 0 && (
        <div className="flex justify-start pt-4">
          <Button id="validate-button" variant="primary" type="button" onClick={() => handleValidate()} disabled={!canValidate || isExecuting}>
            {isExecuting ? t("gzl.user.interface.validating") : t("gzl.user.interface.validate")}
          </Button>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <NoticeBanner color="red" className="text-red">
          <span className="font-semibold">{t("gzl.user.interface.error_label")}</span> {error}
        </NoticeBanner>
      )}
      {inlineReport ? <InlineValidationReport report={inlineReport} /> : null}
    </div>
  );
}

function InlineValidationReport({ report }: Readonly<{ report: Record<string, unknown> }>) {
  const overall = String(report.overallResult ?? "UNKNOWN");
  const color = overall === "PASSED" ? "#15803d" : overall === "FAILED" ? "#b91c1c" : "#a16207";
  const reports = (report.reports as Record<string, unknown>[] | undefined) ?? [];
  const renderAssertions = (r: Record<string, unknown>) => {
    const own = (r.assertionReports as Record<string, unknown>[] | undefined) ?? [];
    const subs = (r.subReports as Record<string, unknown>[] | undefined) ?? [];
    const fromSubs = subs.flatMap((sr) => ((sr.assertionReports as Record<string, unknown>[] | undefined) ?? []));
    return [...own, ...fromSubs];
  };
  return (
    <div className="mt-6 rounded-xl border bg-white p-4 shadow-sm" data-testid="inline-validation-report">
      <h3 className="text-lg font-semibold">
        Validation result: <span style={{ color }}>{overall}</span>
      </h3>
      <ul className="mt-3 space-y-3">
        {reports.map((r, i) => {
          const sub = String(r.subReportResult ?? "");
          const subColor = sub === "PASSED" ? "#15803d" : sub === "FAILED" ? "#b91c1c" : "#a16207";
          const failed = renderAssertions(r).filter((a) => a.result !== "PASSED");
          return (
            <li key={i} className="border-t pt-2">
              <span className="font-medium">{String(r.name ?? "Sub-report")}</span>
              {": "}
              <span style={{ color: subColor }}>{sub}</span>
              {failed.length > 0 ? (
                <ul className="mt-1 list-disc pl-6 text-sm">
                  {failed.map((a, j) => (
                    <li key={j}>
                      <span className="font-medium">{String(a.severity ?? "")}</span>
                      {" — "}
                      {String(a.description ?? a.assertionType ?? "assertion failed").trim()}
                    </li>
                  ))}
                </ul>
              ) : null}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
