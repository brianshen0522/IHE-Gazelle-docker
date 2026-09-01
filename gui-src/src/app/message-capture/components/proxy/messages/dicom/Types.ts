import { Messages, DataMessageProps } from "../../Types";

export type DicomMessage = Messages & {
  messageId: string;
  channelType: "DICOM";
  commandField: string;
  affectedSopClassUid: string;
  commandSet: string;
  transferSyntax: string;
  id: string;
  status: string;
  requestedSopClassName: string;
  requestedSopClassUid: string;
  affectedSopClassName: string;
  commandDataSetType: string;
  captureDate: string;
  dumpDataSet?: string | null;
  dumpCommandSet?: string | null;
};

export type DicomCommandSetProps = {
  data: DataMessageProps['data'];
  showRenderer: string;
};
