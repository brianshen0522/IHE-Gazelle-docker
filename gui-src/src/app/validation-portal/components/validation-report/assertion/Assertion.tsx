import React, { useEffect, useState } from "react";
import { AssertionReportDTO } from "@/shared/types/validation/types";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import { useTranslation } from "react-i18next";
import UnexpectedErrorsDisplay from "@validation-portal/components/validation-report/UnexpectedErrorDisplay";
import { getStatusColor } from "@/shared/utils/getStatusColor";
import { CopyURL, InfoRow } from "@gazelle/gazelle-component-ui";
import { isEqualAssertion } from "@/app/validation-portal/utils/isEqualAssertion";
import { toast } from "react-toastify";

type AssertionProps = {
  assertion: AssertionReportDTO;
};

const formatStatusText = (str: string): string => {
  return str ? str.charAt(0) + str.slice(1).toLowerCase() : str;
};

const Assertion = React.memo(function ({ assertion }: Readonly<AssertionProps>) {
  const { t } = useTranslation();
  const { selectedAssertion, setSelectedAssertion } = useReportAssertions();
  const [isOpen, setIsOpen] = useState(false);
  const isCurrentlySelected = isEqualAssertion(selectedAssertion, assertion);

  const statusColor = getStatusColor(assertion.result, assertion.severity);

  useEffect(() => {
    setIsOpen(isCurrentlySelected);
  }, [selectedAssertion, isCurrentlySelected]);

  useEffect(() => {
    if (assertion.result === "UNDEFINED" && typeof globalThis !== "undefined" && globalThis.location.hash === "#undefined-assertions") {
      setIsOpen(true);
    }
  }, [assertion.result]);

  const handleViewMore = () => setIsOpen(!isOpen);

  const selectAssertion = () => {
    if (!isCurrentlySelected) {
      setSelectedAssertion(assertion);
    }
  };

  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === "Enter" || event.key === " ") {
      selectAssertion();
      event.preventDefault();
    }
  };

  const statusText = formatStatusText(
    assertion.result !== "PASSED" && assertion.result !== "UNDEFINED" ? assertion.severity || "" : assertion.result || "",
  );

  const hasSubjectLocations = assertion.subjectLocations && assertion.subjectLocations.length > 0;

  return (
    <div
      className={`rounded-md border border-l-8 select-text ${isCurrentlySelected ? "bg-grey-100" : "bg-white"}`}
      style={{ borderColor: statusColor }}
    >
      <div className="flex flex-row w-full">
        <div className="flex flex-col p-2 w-full">
          {/* Header section */}
          <div className="flex flex-row justify-between">
            <p className="font-bold" style={{ color: statusColor }}>
              {statusText}
            </p>
            <button onClick={handleViewMore} className="text-blue hover:text-visited_link">
              {isOpen ? t("gzl.texec.show_less") : t("gzl.texec.show_more")}
            </button>
          </div>

          {/* Main content section */}
          <button className="text-left mt-1 w-full cursor-pointer" onClick={selectAssertion} onKeyDown={handleKeyDown}>
            {/* Description section */}
            <div className="break-words">{assertion.description}</div>

            {/* Priority for undefined assertions */}
            {assertion.result === "UNDEFINED" && assertion.priority && <InfoRow label={t("gzl.texec.priority")} value={assertion.priority} />}

            {/* Unexpected errors */}
            {assertion.unexpectedErrors && assertion.unexpectedErrors.length > 0 && <UnexpectedErrorsDisplay errors={assertion.unexpectedErrors} />}

            {/* Expanded details section */}
            {isOpen && (
              <div>
                {assertion.source && <InfoRow label={t("gzl.texec.source")} value={assertion.source} />}
                {assertion.assertionID && <InfoRow label={t("gzl.texec.test_name")} value={assertion.assertionID} />}
                {assertion.priority && assertion.result !== "UNDEFINED" && <InfoRow label={t("gzl.texec.priority")} value={assertion.priority} />}
                {!!assertion.requirementIDs?.length && <InfoRow label={t("gzl.texec.requirements")} value={assertion.requirementIDs} />}
                {assertion.assertionType && <InfoRow label={t("gzl.texec.type")} value={assertion.assertionType} />}

                {hasSubjectLocations && (
                  <div className="mt-1.5">
                    <span className="font-semibold">{t("gzl.texec.subject_location")}:</span>
                    <ul>
                      {assertion?.subjectLocations?.map((location: Record<string, string>, locationIndex: number) => (
                        <li
                          key={`${locationIndex}-${location.inputId ?? ""}-${location.type ?? ""}-${location.value ?? ""}`}
                          className="p-1 rounded my-1"
                        >
                          {location.inputId && <InfoRow label={`${t("gzl.texec.input_id")}`} value={location.inputId} />}
                          {location.type && <InfoRow label={`${t("gzl.texec.type")}`} value={location.type} />}
                          {location.value && (
                            <div className="flex-col items-center gap-4">
                              <InfoRow label={`${t("gzl.texec.value")}`} value={location.value} />
                              {/* TODO: this component should renamed for generic purpose */}
                              <CopyURL
                                linkText={t("gzl.user.interface.copy_subject_location_value")}
                                currentURL={location.value}
                                title={t("gzl.user.interface.copy_subject_location_value")}
                                onCopySuccess={() => {
                                  toast.success(t("gzl.user.interface.value_copied_to_clipboard"));
                                }}
                                onCopyError={() => {
                                  toast.error(t("gzl.user.interface.failed_to_copy_value"));
                                }}
                              />
                            </div>
                          )}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
                {assertion.formalExpression && <InfoRow label={t("gzl.texec.test")} value={assertion.formalExpression} />}
                {assertion.subjectValue && <InfoRow label={t("gzl.texec.subject_value")} value={assertion.subjectValue} />}
              </div>
            )}
          </button>
        </div>
      </div>
    </div>
  );
});

export default Assertion;
