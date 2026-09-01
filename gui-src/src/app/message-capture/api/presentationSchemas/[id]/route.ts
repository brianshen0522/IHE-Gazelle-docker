import fs from "node:fs";
import { NextRequest, NextResponse } from "next/server";
import {PresentationSchema} from "@/app/message-capture/components/proxy/Types";

export async function GET(req: NextRequest) {
  const schemaName = req.nextUrl.pathname.split("/").pop();
  const schemaFilePath = process.env.GDH_PRESENTATION_SCHEMA_PATH ?? "/opt/datahouse-ui/presentationSchemas.json";

  try {
    const schemas = JSON.parse(fs.readFileSync(schemaFilePath, "utf-8")) as PresentationSchema[];
    const schema = schemas.find((s) => s.name === schemaName);

    if (!schema) {
      return NextResponse.json(
        {
          message: "Schema not found",
        },
        { status: 404 }
      );
    }
    return NextResponse.json(schema);
  } catch (err: unknown) {
    return NextResponse.json({ error: (err as Error)?.message || "Unable to get presentation schemas" }, { status: 500 });
  }
}
