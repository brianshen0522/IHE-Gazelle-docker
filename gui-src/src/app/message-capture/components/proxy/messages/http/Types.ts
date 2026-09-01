import { ReferenceType } from "../../Types";

// Base HTTP message properties shared across all HTTP types
export interface BaseHttpContent {
  method: string;
  uri: string;
  version: string;
  status?: number;
  reasonPhrase?: string;
}

// Extended HTTP content with optional properties
export interface HttpContentData extends BaseHttpContent {
  [key: string]: any;
  type?: string; // For content type identification (e.g., "HTTP_REQUEST")
  content?: string; // For MessageContent compatibility
  body?: string;
  headers?: Record<string, string>;
  unexpectedErrors?: {
    rootType?: string;
  };
  additionalParameters?: {
    [key: string]: string;
  };
}

// Base data structure for HTTP messages
export interface HttpData {
  id: string;
  type: string;
  content: HttpContentData;
  references: ReferenceType[];
}

export interface HttpHeaderProps {
  headers: Record<string, string>;
  content: Partial<BaseHttpContent> & { [key: string]: any };
}

export interface HttpBodyProps {
  data: HttpData;
  contentTypeHeader: string;
  id: string | null;
  searchParams: URLSearchParams;
}
