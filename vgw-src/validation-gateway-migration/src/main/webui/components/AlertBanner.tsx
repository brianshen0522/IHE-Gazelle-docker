type AlertBannerProps = {
  text: string;
  tone?: "danger" | "warning";
};

export function AlertBanner({ text, tone = "danger" }: AlertBannerProps) {
  const toneClass =
    tone === "danger"
      ? "border-rose-300 bg-rose-50/90 text-rose-900"
      : "border-amber-300 bg-amber-50/90 text-amber-900";

  return (
    <section className={`rounded-2xl border px-4 py-3 text-sm font-medium shadow-sm backdrop-blur ${toneClass}`}>
      {text}
    </section>
  );
}
