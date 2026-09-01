export type MaestroStepReport = {
  stepId: string;
  type: string;
  result: string;
  outputs: OutputDTO[];
};

export type OutputDTO = {
  name: string;
  reference: string;
  content: string;
};

export type MaestroRunReport = {
  dateTime: string;
  result: string;
  test: {
    id: string;
    name: string;
  };
  stepRunReports: MaestroStepReport[];
};

export type MaestroReport = {
  uuid: string;
  date: string;
  status: string;
  testCounters: {
    total: number;
    passed: number;
    failed: number;
    undefined: number;
    unexpectedErrors: number;
  };
  unexpectedErrors: [
    {
      name: string;
      message: string;
    },
  ];
  testRunReports: MaestroRunReport[];
};
