import {
  NON_RETRYABLE_CLOSE_CODES,
  CLOSE_CODE_MESSAGES,
  MaestroWebSocketError,
  BaseWebSocketConfig,
  WebSocketEvent,
  EventListener,
} from "@/shared/services/websocket/WebSocketTypes";

export abstract class WebSocketService<TConfig extends BaseWebSocketConfig> {
  private ws: WebSocket | null = null;
  protected config: TConfig | null = null;
  private readonly listeners: Set<EventListener> = new Set();
  private reconnectTimeout: NodeJS.Timeout | null = null;
  private reconnectAttempts: number = 0;
  private intentionalDisconnect: boolean = false;

  private readonly defaultMaxReconnectAttempts: number;
  private readonly defaultBaseReconnectDelay: number;
  private readonly defaultMaxReconnectDelay: number;

  private readonly nonRetryableCloseCodes: Set<number> = new Set(NON_RETRYABLE_CLOSE_CODES);

  constructor(maxReconnectAttempts: number = 5, baseReconnectDelay: number = 1000, maxReconnectDelay: number = 30000) {
    this.defaultMaxReconnectAttempts = maxReconnectAttempts;
    this.defaultBaseReconnectDelay = baseReconnectDelay;
    this.defaultMaxReconnectDelay = maxReconnectDelay;
  }

  // Each app defines how to build its WebSocket URL from its config
  protected abstract buildUrl(config: TConfig): string;

  // Validate app-specific required fields — override to add extra checks
  protected validateConfig(config: TConfig): boolean {
    if (!config.baseUrl || !config.accessToken) {
      console.error("WebSocketService missing required config: baseUrl or accessToken");
      return false;
    }
    return true;
  }

  connect(config: TConfig): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn("[WebSocketService] Already connected");
      return;
    }

    if (!this.validateConfig(config)) return;

    this.config = config;
    this.reconnectAttempts = 0;
    this.intentionalDisconnect = false;
    this.createConnection();
  }

  send(message: Record<string, unknown>): boolean {
    if (this.ws?.readyState !== WebSocket.OPEN) {
      console.error("WebSocketService cannot send: not connected");
      return false;
    }

    try {
      this.ws.send(JSON.stringify(message));
      return true;
    } catch (error) {
      console.error("WebSocketService failed to send message:", error);
      this.emitError({ type: "network", message: "Failed to send message", retryable: false });
      return false;
    }
  }

  sendWithAuth(message: Record<string, unknown>): boolean {
    return this.send({ ...message, authorization: this.config?.accessToken });
  }

  subscribe(listener: EventListener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  disconnect(): void {
    this.intentionalDisconnect = true;

    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }

    if (this.ws) {
      this.ws.close(1000, "Client disconnect");
      this.ws = null;
    }
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  getReadyState(): number | null {
    return this.ws?.readyState ?? null;
  }

  // Private — connection lifecycle
  private createConnection(): void {
    if (!this.config) {
      this.emitError({ type: "connection", message: "No configuration provided", retryable: false });
      return;
    }

    // Uses the app-specific URL builder instead of a hardcoded URL
    const url = this.buildUrl(this.config);

    if (!url || url.trim() === "") {
      this.emitError({ type: "connection", message: "WebSocket URL is empty or invalid", retryable: false });
      return;
    }

    try {
      this.ws = new WebSocket(url);
      this.setupEventHandlers();
    } catch (error) {
      this.emitError({
        type: "connection",
        message: error instanceof Error ? error.message : "Failed to create WebSocket",
        retryable: true,
      });
      this.scheduleReconnect();
    }
  }

  private setupEventHandlers(): void {
    if (!this.ws) return;

    this.ws.onopen = () => {
      console.warn("[WebSocketService] Connected");
      this.reconnectAttempts = 0;
      this.emit({ type: "connected" });
    };

    this.ws.onmessage = (event: MessageEvent) => {
      try {
        const message = JSON.parse(event.data) as Record<string, unknown>;
        this.emit({ type: "message", data: message });
      } catch (error) {
        console.error("WebSocketService failed to parse message:", error);
        this.emitError({ type: "unknown", message: "Failed to parse WebSocket message", retryable: false });
      }
    };

    this.ws.onerror = (event: Event) => {
      console.error("WebSocketService webSocket error:", event);
      this.emitError({ type: "network", message: "WebSocket connection error", retryable: true });
    };

    this.ws.onclose = (event: CloseEvent) => {
      console.warn(`WebSocketService disconnected: ${event.code} - ${event.reason || "No reason"}`);
      this.ws = null;

      if (this.intentionalDisconnect) {
        this.emit({ type: "disconnected", data: { code: event.code, reason: event.reason, wasClean: event.wasClean } });
        return;
      }

      const isNonRetryable = this.nonRetryableCloseCodes.has(event.code);

      if (isNonRetryable) {
        const wsError: MaestroWebSocketError = {
          type: this.getErrorTypeFromCode(event.code),
          message: this.getCloseCodeMessage(event.code, event.reason),
          code: event.code,
          reason: event.reason,
          retryable: false,
        };
        console.error(`WebSocketService non-retryable close: ${wsError.message}`);
        this.emit({ type: "connectionFailed", error: new Error(wsError.message), data: wsError });
      } else {
        this.emit({ type: "disconnected", data: { code: event.code, reason: event.reason, wasClean: event.wasClean } });

        if (this.config?.enableReconnect !== false && !event.wasClean) {
          this.scheduleReconnect();
        }
      }
    };
  }

  private scheduleReconnect(): void {
    const maxAttempts = this.config?.maxReconnectAttempts ?? this.defaultMaxReconnectAttempts;

    if (this.reconnectAttempts >= maxAttempts) {
      this.emit({ type: "connectionFailed", error: new Error(`Failed to reconnect after ${maxAttempts} attempts`) });
      return;
    }

    this.reconnectAttempts++;
    const baseDelay = this.config?.reconnectDelay ?? this.defaultBaseReconnectDelay;
    const delay = Math.min(baseDelay * Math.pow(2, this.reconnectAttempts - 1), this.defaultMaxReconnectDelay);

    console.warn(`WebSocketService reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${maxAttempts})...`);
    this.emit({ type: "reconnecting", data: { attempt: this.reconnectAttempts } });

    this.reconnectTimeout = setTimeout(() => this.createConnection(), delay);
  }

  private emitError(error: MaestroWebSocketError): void {
    console.error(`WebSocketService error [${error.type}]:`, error.message, error);
    this.emit({ type: "error", error: new Error(error.message), data: error });
  }

  private emit(event: WebSocketEvent): void {
    this.listeners.forEach((listener) => {
      try {
        listener(event);
      } catch (err) {
        console.error("WebSocketService uncaught error in event listener:", err);
      }
    });
  }

  private getErrorTypeFromCode(code: number): MaestroWebSocketError["type"] {
    if (code === 4001 || code === 4003) return "authentication";
    if (code === 1011 || code === 4000) return "server";
    return "connection";
  }

  private getCloseCodeMessage(code: number, serverReason?: string): string {
    const message = CLOSE_CODE_MESSAGES[code] ?? `Connection closed with code ${code}`;
    return serverReason ? `${message}: ${serverReason}` : message;
  }
}
