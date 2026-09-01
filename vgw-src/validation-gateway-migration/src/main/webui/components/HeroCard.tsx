export function HeroCard() {
  return (
    <section className="relative overflow-hidden rounded-3xl border border-slate-200 bg-white/90 px-7 py-8 text-slate-900 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.35)] backdrop-blur">
      <p className="text-[11px] uppercase tracking-[0.28em] text-slate-500">Validation Gateway</p>
      <h1 className="mt-3 text-3xl font-semibold tracking-tight text-slate-900 md:text-4xl">EVS Migration Console</h1>
      <p className="mt-3 max-w-3xl text-sm leading-relaxed text-slate-600 md:text-base">
        Move historical EVSClient validations into Datahouse with controlled execution, health-aware safeguards, and
        migration traceability.
      </p>
    </section>
  );
}
