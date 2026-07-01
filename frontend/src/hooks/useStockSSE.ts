import { useEffect, useRef, useState } from 'react';
import { createStockSSE } from '@/api/sse';

type ConnectionState = 'connecting' | 'connected' | 'disconnected';

interface UseStockSSEResult {
  inventory: number | null;
  connectionState: ConnectionState;
}

/**
 * Hook that subscribes to live inventory updates via SSE for a given sale item.
 * Automatically reconnects with exponential backoff on disconnect.
 */
export function useStockSSE(
  saleItemUuid: string,
  initialInventory: number | null = null
): UseStockSSEResult {
  const [inventory, setInventory] = useState<number | null>(initialInventory);
  const [connectionState, setConnectionState] = useState<ConnectionState>('connecting');
  const cleanupRef = useRef<(() => void) | null>(null);

  // Update local state when initialInventory changes (e.g. from API fetch)
  useEffect(() => {
    setInventory(initialInventory);
  }, [initialInventory]);

  useEffect(() => {
    if (!saleItemUuid) return;

    setConnectionState('connecting');

    const cleanup = createStockSSE(saleItemUuid, {
      onUpdate: (event) => {
        setInventory(event.remainingInventory);
      },
      onConnected: () => setConnectionState('connected'),
      onDisconnected: () => setConnectionState('disconnected'),
    });

    cleanupRef.current = cleanup;

    return () => {
      cleanup();
      cleanupRef.current = null;
    };
  }, [saleItemUuid]);

  return { inventory, connectionState };
}
