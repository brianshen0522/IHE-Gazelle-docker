/*
 * Copyright 2024 Kereval.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { useCallback } from "react";

const useDateFormat = (useFlexCol: boolean = true) => {
  const formatDate = useCallback(
    (dateString: number | string) => {
      const date = new Date(dateString);

      // Format date as DD-MM-YYYY
      const day = String(date.getDate()).padStart(2, "0");
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const year = date.getFullYear();
      const formattedDate = `${year}-${month}-${day}`;

      // Format time in 24-hour format (HH:MM:SS:mmm)
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");
      const milliseconds = String(date.getMilliseconds()).padStart(3, "0");

      // Get timezone
      const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

      return (
        <div className={useFlexCol ? "flex flex-col" : "flex items-center gap-2"} title={timeZone}>
          <span className="font-semibold">
            {hours}:{minutes}:{seconds}:{milliseconds}
          </span>
          <span>{formattedDate}</span>
        </div>
      );
    },
    [useFlexCol]
  );

  return formatDate;
};

export default useDateFormat;
