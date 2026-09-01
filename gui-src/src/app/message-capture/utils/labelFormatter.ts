// Helper function to convert camelCase to input label expected format
export const labelFormatter = (str: string) => {
  if (!str || str === "TLS error") return str;

  str = str.includes("channel_type") ? "standard" : str;

  const ipRegex = str.includes("ip") ? /ip/g : /ip/gi;
  str = str.replace(ipRegex, "$& address");

  return str
    .replaceAll(/([A-Z])/g, " $1") // insert a space before all capital letters
    .replaceAll(/\s(.)/g, function (str) {
      // lowercase the first character after a space
      return str.toLowerCase();
    })
    .replace(/^./, function (str) {
      // uppercase the first character of the string
      return str.toUpperCase();
    })
    .replaceAll('_', " "); // replace all underscores with a space
};

// Helper function to format message like DECODER_ERROR to Decoder error
export const textFormatter = (str: string) => {
  if (!str) return str;

  return str
    .split("_")
    ?.map((word, index) => {
      if (index === 0 && word === "TLS") {
        return word.toUpperCase();
      }
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    })
    .join(" ");
};

export const removeDonePrefix = (status: string) => {
  return status.startsWith("DONE_") ? status.replace("DONE_", "") : status;
};
