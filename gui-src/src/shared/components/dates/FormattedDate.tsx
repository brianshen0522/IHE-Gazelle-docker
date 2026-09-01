import {DateTime} from "luxon";
import {getCookie} from "cookies-next";

interface Props {
  dateString?: string;
  dateFormat?: string;
}

const FormattedDate = ({dateString, dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"}: Props) => {
  if (!dateString) return null;
  const locale = getCookie('GZL_LOCALE')?.toString() ?? 'en';
  const dt = DateTime.fromISO(dateString, {setZone: true}).setLocale(locale).toLocal();
  const display = dt.toFormat(dateFormat);
  const tooltip = dt.offsetNameLong;

  return (
    <span title={tooltip ?? ''}>
      {display}
    </span>
  );
};

export default FormattedDate;
