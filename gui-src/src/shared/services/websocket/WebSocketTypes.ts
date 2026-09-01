export interface MaestroWebSocketError {
  type: "connection" | "authentication" | "server" | "network" | "unknown";
  message: string;
  code?: number;
  reason?: string;
  retryable: boolean;
}

export const NON_RETRYABLE_CLOSE_CODES = [
  1008, // Policy violation
  1011, // Internal server error
  4000, // Invalid request
  4001, // Unauthorized (expired token)
  4003, // Forbidden (permission denied)
  4004, // Execution not found
] as const;

export const CLOSE_CODE_MESSAGES: Record<number, string> = {
  1000: "Normal closure",
  1001: "Going away",
  1002: "Protocol error",
  1003: "Unsupported data",
  1006: "Abnormal closure",
  1008: "Policy violation",
  1011: "Internal server error",
  4000: "Invalid request",
  4001: "Unauthorized - authentication failed or token expired",
  4003: "Forbidden - insufficient permissions",
  4004: "Execution not found",
};

export type WebSocketEventType = "connected" | "disconnected" | "reconnecting" | "connectionFailed" | "message" | "error";

export interface WebSocketEvent {
  type: WebSocketEventType;
  data?: unknown;
  error?: Error;
}

export type EventListener = (event: WebSocketEvent) => void;

// Base config for all WebSocket services — extend to add app-specific fields
export interface BaseWebSocketConfig {
  baseUrl: string;
  accessToken: string; // required for all apps
  enableReconnect?: boolean;
  maxReconnectAttempts?: number;
  reconnectDelay?: number;
}
