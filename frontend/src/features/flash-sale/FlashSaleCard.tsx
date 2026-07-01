import { useState } from 'react';
import { ShoppingCartIcon, SignalIcon, XCircleIcon } from '@heroicons/react/24/outline';
import { InventoryCounter } from './InventoryCounter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { usePurchase } from '@/hooks/useOrders';
import { useStockSSE } from '@/hooks/useStockSSE';
import { formatCurrency, discountPercent } from '@/utils/formatters';
import type { SaleItemResponse } from '@/types';

interface Props {
  saleUuid: string;
  item: SaleItemResponse;
  basePrice: number;
}

function generateUuidV4(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export function FlashSaleCard({ saleUuid, item, basePrice }: Props) {
  const { inventory, connectionState } = useStockSSE(item.saleItemUuid, item.inventory);
  const { mutate: purchase, isPending } = usePurchase(saleUuid, item.saleItemUuid);

  const [idempotencyKey] = useState(() => generateUuidV4());
  const isSoldOut = inventory !== null && inventory === 0;
  const discount = discountPercent(basePrice, item.salePrice);

  const handlePurchase = () => {
    purchase({ data: { quantity: 1 }, idempotencyKey });
  };

  return (
    <div className="mx-auto max-w-lg rounded-xl border border-gray-200 bg-white overflow-hidden">
      {/* Product image placeholder */}
      <div className="flex h-48 items-center justify-center bg-gray-50 border-b border-gray-100">
        <div className="flex flex-col items-center gap-2 text-gray-300">
          <ShoppingCartIcon className="h-14 w-14" />
          <span className="text-xs font-medium">Product Image</span>
        </div>
      </div>

      <div className="p-6 space-y-5">
        {/* Product name */}
        <div>
          <h2 className="text-xl font-semibold text-gray-900">{item.productName}</h2>
          <p className="mt-0.5 text-sm text-gray-500 font-mono truncate" title={item.saleItemUuid}>
            Item ID: {item.saleItemUuid}
          </p>
        </div>

        {/* Pricing */}
        <div className="flex items-end gap-3">
          <span className="text-3xl font-bold text-gray-900">
            {formatCurrency(item.salePrice)}
          </span>
          {discount > 0 && (
            <>
              <span className="text-lg text-gray-400 line-through mb-0.5">
                {formatCurrency(basePrice)}
              </span>
              <Badge variant="success" className="mb-0.5">
                {discount}% off
              </Badge>
            </>
          )}
        </div>

        {/* Inventory & limits */}
        <div className="rounded-lg bg-gray-50 border border-gray-100 px-4 py-3 space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-500">Remaining inventory</span>
            <InventoryCounter remainingInventory={inventory} className="text-base" />
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-500">Limit per customer</span>
            <span className="font-medium text-gray-700">{item.maxPerUser} unit{item.maxPerUser > 1 ? 's' : ''}</span>
          </div>
        </div>

        {/* SSE connection indicator */}
        <div className="flex items-center gap-1.5 text-xs text-gray-400">
          {connectionState === 'connected' ? (
            <>
              <span className="live-dot h-2 w-2 rounded-full bg-green-500 inline-block" />
              <span>Live inventory updates active</span>
            </>
          ) : connectionState === 'connecting' ? (
            <>
              <span className="h-2 w-2 rounded-full bg-amber-400 inline-block animate-pulse" />
              <span>Connecting to live updates…</span>
            </>
          ) : (
            <>
              <XCircleIcon className="h-3.5 w-3.5 text-red-400" />
              <span>Reconnecting…</span>
            </>
          )}
        </div>

        {/* Purchase button */}
        <Button
          size="lg"
          className="w-full justify-center"
          disabled={isSoldOut || isPending}
          loading={isPending}
          onClick={handlePurchase}
          id="purchase-btn"
        >
          {isSoldOut ? (
            'Sold Out'
          ) : (
            <>
              <ShoppingCartIcon className="h-5 w-5" />
              Buy Now — {formatCurrency(item.salePrice)}
            </>
          )}
        </Button>

        {isSoldOut && (
          <p className="text-center text-sm text-red-600 font-medium">
            All units have been claimed.
          </p>
        )}
      </div>
    </div>
  );
}
