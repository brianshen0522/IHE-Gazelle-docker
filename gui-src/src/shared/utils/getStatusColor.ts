export const getStatusColor = (result: string | undefined, severity: string | undefined): string => {
  if (result === undefined) {
    return "black"; // Default color if result is undefined
  }

  switch (result) {
    case "PASSED":
      return "#599C35"; // green
    case "FAILED":
      // Handle the case when severity is undefined
      if (severity === undefined) {
        return "#A91D43"; // Default color for FAILED without severity
      }

      switch (severity) {
        case "WARNING":
          return "#EE7601"; // orange
        case "ERROR":
          return "#A91D43"; // red
        case "INFO":
          return "#45327D"; // dark variation
        default:
          return "black";
      }
    case "UNDEFINED":
      return "#BFBFBF"; // grey disabled
    default:
      return "black";
  }
};
