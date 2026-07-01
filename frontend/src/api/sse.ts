import type { StockUpdateEvent } from '@/types';

interface SSEOptions {
  onUpdate: (event: StockUpdateEvent) => void;
  onConnected?: () => void;
  onDisconnected?: () => void;
}

const MAX_BACKOFF_MS = 30000;

/**
 * Creates a managed EventSource connection to the stock-updates SSE endpoint.
 * Returns a cleanup function.
 */
export function createStockSSE(saleItemUuid: string, options: SSEOptions): () => void {
  let es: EventSource | null = null;
  let backoffMs = 1000;
  let destroyed = false;
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

  function connect() {
    if (destroyed) return;

    // SSE needs auth — use the access token as a query param since
    // EventSource does not support custom headers natively.
    const token = localStorage.getItem('flash-sale-auth')
      ? JSON.parse(localStorage.getItem('flash-sale-auth')!).state?.accessToken
      : null;

    const url = `/api/v1/stock-updates/${saleItemUuid}`;

    es = new EventSource(url);

    es.onopen = () => {
      backoffMs = 1000; // reset on successful connect
      options.onConnected?.();
    };

    es.addEventListener('stock-update', (e: MessageEvent) => {
      try {
        const data: StockUpdateEvent = JSON.parse(e.data);
        options.onUpdate(data);
      } catch {
        // malformed event — ignore
      }
    });

    // heartbeat event — keep-alive, no action needed
    es.addEventListener('heartbeat', () => {/* no-op */});

    es.onerror = () => {
      es?.close();
      es = null;
      options.onDisconnected?.();

      if (!destroyed) {
        reconnectTimer = setTimeout(() => {
          backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
          connect();
        }, backoffMs);
      }
    };
  }

  connect();

  return () => {
    destroyed = true;
    if (reconnectTimer !== null) clearTimeout(reconnectTimer);
    es?.close();
    es = null;
  };
}
