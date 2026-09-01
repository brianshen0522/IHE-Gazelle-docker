import { useEffect, useState, memo } from "react";
import NoticeBanner from "@shared/components/banner/NoticeBanner";
import { useTranslation } from "react-i18next";

interface CountdownTimerProps {
  initialTimeout: number;
  onTimeout: () => void;
}

const formatCountdown = (ms: number) => {
  if (ms <= 0) return "00:00";

  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
};

const CountdownTimer = memo(({ initialTimeout, onTimeout }: CountdownTimerProps) => {
  const { t } = useTranslation();
  const [timeLeft, setTimeLeft] = useState(initialTimeout);

  useEffect(() => {
    setTimeLeft(initialTimeout);
  }, [initialTimeout]);

  useEffect(() => {
    if (timeLeft <= 0) {
      onTimeout();
      return;
    }

    const interval = setInterval(() => {
      setTimeLeft((prev) => Math.max(prev - 1000, 0));
    }, 1000);

    return () => clearInterval(interval);
  }, [timeLeft, onTimeout]);

  return (
    <NoticeBanner color="blue" weight="semibold">
      {t("gzl.user.interface.time_left")} {formatCountdown(timeLeft)}
    </NoticeBanner>
  );
});

CountdownTimer.displayName = "CountdownTimer";

export default CountdownTimer;
