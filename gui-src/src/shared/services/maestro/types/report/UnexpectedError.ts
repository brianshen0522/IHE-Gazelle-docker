export interface UnexpectedError {
  name: string;
  message: string;
  cause?: UnexpectedError;
}
