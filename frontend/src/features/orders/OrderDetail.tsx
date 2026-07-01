import { CheckCircleIcon, ClockIcon } from '@heroicons/react/24/outline';
import { Badge } from '@/components/ui/Badge';
import { Card } from '@/components/ui/Card';
import { formatCurrency, formatDate, truncateUuid } from '@/utils/formatters';
import type { OrderResponse } from '@/types';

interface Props {
  order: OrderResponse;
}

function orderStatusVariant(status: string) {
  switch (status) {
    case 'CONFIRMED': return 'success';
    case 'PENDING': return 'warning';
    case 'CANCELLED': return 'danger';
    default: return 'default' as const;
  }
}

export function OrderDetail({ order }: Props) {
  return (
    <div className="max-w-lg mx-auto space-y-6">
      {/* Status banner */}
      <div className="flex items-center gap-3 rounded-lg border border-green-200 bg-green-50 px-5 py-4">
        <CheckCircleIcon className="h-6 w-6 text-green-600 shrink-0" />
        <div>
          <p className="text-sm font-semibold text-green-800">Order Confirmed</p>
          <p className="text-xs text-green-600">
            Your order has been queued for processing via Redis async queue.
          </p>
        </div>
      </div>

      {/* Order details card */}
      <Card>
        <h2 className="text-base font-semibold text-gray-900 mb-4">Order Details</h2>
        <dl className="space-y-3 text-sm">
          <Row label="Order UUID">
            <code className="font-mono text-xs text-gray-600" title={order.orderUuid}>
              {order.orderUuid}
            </code>
          </Row>
          <Row label="Product">{order.productName}</Row>
          <Row label="Quantity">{order.quantity}</Row>
          <Row label="Unit Price">{formatCurrency(order.unitPrice)}</Row>
          <Row label="Total Price">
            <span className="font-semibold">{formatCurrency(order.totalPrice)}</span>
          </Row>
          <Row label="Status">
            <Badge variant={orderStatusVariant(order.status)}>{order.status}</Badge>
          </Row>
          <Row label="Placed at">
            <span className="flex items-center gap-1">
              <ClockIcon className="h-3.5 w-3.5 text-gray-400" />
              {formatDate(order.createdAt)}
            </span>
          </Row>
        </dl>
      </Card>

      {/* UUIDs card */}
      <Card>
        <h3 className="text-sm font-medium text-gray-600 mb-3">Reference IDs</h3>
        <dl className="space-y-2 text-xs font-mono text-gray-500">
          <div className="flex justify-between gap-4">
            <dt>Sale Item UUID</dt>
            <dd className="text-gray-700 truncate">{order.saleItemUuid}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt>Product UUID</dt>
            <dd className="text-gray-700 truncate">{order.productUuid}</dd>
          </div>
        </dl>
      </Card>
    </div>
  );
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-4">
      <dt className="text-gray-500 shrink-0">{label}</dt>
      <dd className="text-gray-900 text-right">{children}</dd>
    </div>
  );
}
