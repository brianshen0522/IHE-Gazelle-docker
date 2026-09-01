"use client";
import { useEffect, useRef, useState } from "react";
import {
  alignEndOfDayIfNeeded,
  buildDateRangeParam,
  convertToZonedDateTime,
  DateRangeFilter,
  getPresetDateRange,
  type DateRangePreset,
  type DateRangePresetOption,
  parseDateRangeParam,
  type DateRangeState,
} from "@gazelle/gazelle-component-ui";
import { useSearchParams } from "next/navigation";
import { endOfWeek } from "date-fns";
import { useCreateQuery } from "@shared/hooks/useCreateQuery";
import { useTranslation } from "react-i18next";

const getPresetRange = (preset: DateRangePreset): DateRangeState => {
  if (preset !== "thisWeek") {
    return getPresetDateRange(preset);
  }

  const range = getPresetDateRange(preset);
  return {
    ...range,
    end: convertToZonedDateTime(endOfWeek(new Date(), { weekStartsOn: 1 })),
  };
};

const DateFilterContent = ({
  paramName,
  label,
  persistentParams = {},
}: {
  paramName: string;
  label: string;
  persistentParams?: Record<string, string>;
}) => {
  const { createQuery } = useCreateQuery();
  const searchParams = useSearchParams();
  const { t } = useTranslation();
  const queryValue = searchParams.get(paramName);
  const [value, setValue] = useState<DateRangeState>(() => parseDateRangeParam(queryValue) ?? { start: null, end: null });
  const pendingPreset = useRef<DateRangePreset | null>(null);

  // Sync state when the URL param changes (e.g. "Clear all" button, external navigation)
  useEffect(() => {
    setValue(parseDateRangeParam(queryValue) ?? { start: null, end: null });
  }, [queryValue]);

  const updateDateRange = (nextValue: DateRangeState) => {
    const effectiveValue = pendingPreset.current ? getPresetRange(pendingPreset.current) : nextValue;
    pendingPreset.current = null;
    const nextRange = {
      start: effectiveValue.start,
      end: alignEndOfDayIfNeeded(effectiveValue.end),
    };
    setValue(nextRange);
    createQuery({
      ...persistentParams,
      [paramName]: nextRange.start || nextRange.end ? buildDateRangeParam(nextRange.start, nextRange.end) : "",
    });
  };

  const presets: DateRangePresetOption[] = [
    { id: "thisMonth", label: t("gzl.message.capture.this_month") },
    { id: "thisWeek", label: t("gzl.message.capture.this_week") },
    { id: "yesterday", label: t("gzl.message.capture.yesterday") },
    { id: "today", label: t("gzl.message.capture.today") },
  ];

  return (
    <div
      onClickCapture={(event) => {
        const button = (event.target as HTMLElement).closest("button");
        const preset = presets.find((item) => item.label === button?.textContent?.trim());
        pendingPreset.current = preset?.id ?? null;
      }}
    >
      <DateRangeFilter
        label={label}
        ariaLabel="Date Range Picker"
        value={value}
        onChange={updateDateRange}
        granularity="minute"
        hideTimeZone={false}
        showPresets
        presets={presets}
      />
    </div>
  );
};

export default DateFilterContent;
