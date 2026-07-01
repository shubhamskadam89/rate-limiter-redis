import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ClipboardDocumentIcon } from '@heroicons/react/24/outline';
import { DataTable } from '@/components/ui/DataTable';
import { Badge, saleStatusBadge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { useActivateSale, getLocalSales } from '@/hooks/useSales';
import { formatDate, truncateUuid } from '@/utils/formatters';
import type { LocalSaleEntry, SaleItemResponse } from '@/types';

interface Props {
  sales: LocalSaleEntry[];
  onAddItem: (sale: LocalSaleEntry) => void;
}

export function SaleTable({ sales, onAddItem }: Props) {
  const { mutate: activate, isPending: activating } = useActivateSale();
  const [activatingId, setActivatingId] = useState<string | null>(null);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const handleActivate = (saleUuid: string) => {
    setActivatingId(saleUuid);
    activate(saleUuid, { onSettled: () => setActivatingId(null) });
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text).then(() => {
      setCopiedId(id);
      setTimeout(() => setCopiedId(null), 2000);
    });
  };

  if (sales.length === 0) {
    return (
      <EmptyState
        title="No sales created yet"
        description="Create a sale event, add a product item, then activate it to load inventory into Redis."
      />
    );
  }

  return (
    <div className="space-y-4">
      {sales.map((sale) => (
        <div key={sale.saleUuid} className="rounded-lg border border-gray-200 bg-white overflow-hidden">
          {/* Sale header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
            <div className="flex items-center gap-3">
              <Badge variant={saleStatusBadge(sale.status)}>{sale.status}</Badge>
              <h3 className="font-medium text-gray-900">{sale.name}</h3>
              <code className="text-xs text-gray-400 font-mono">{truncateUuid(sale.saleUuid)}</code>
            </div>
            <div className="flex items-center gap-2">
              {sale.status === 'DRAFT' && (
                <>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => onAddItem(sale)}
                  >
                    + Add Item
                  </Button>
                  <Button
                    size="sm"
                    loading={activatingId === sale.saleUuid && activating}
                    disabled={sale.saleItems.length === 0}
                    onClick={() => handleActivate(sale.saleUuid)}
                    title={sale.saleItems.length === 0 ? 'Add at least one item first' : ''}
                  >
                    Activate
                  </Button>
                </>
              )}
            </div>
          </div>

          {/* Sale items */}
          {sale.saleItems.length > 0 ? (
            <table className="min-w-full text-sm">
              <thead className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wider">
                <tr>
                  <th className="px-5 py-2.5 text-left font-medium">Product</th>
                  <th className="px-5 py-2.5 text-left font-medium">Sale Price</th>
                  <th className="px-5 py-2.5 text-left font-medium">Inventory</th>
                  <th className="px-5 py-2.5 text-left font-medium">Max/User</th>
                  <th className="px-5 py-2.5 text-left font-medium">Flash Sale URL</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {sale.saleItems.map((item: SaleItemResponse) => {
                  const flashUrl = `/sales/${sale.saleUuid}/items/${item.saleItemUuid}`;
                  return (
                    <tr key={item.saleItemUuid} className="hover:bg-gray-50">
                      <td className="px-5 py-3 font-medium text-gray-900">{item.productName}</td>
                      <td className="px-5 py-3 text-gray-700">₹{item.salePrice.toLocaleString('en-IN')}</td>
                      <td className="px-5 py-3 text-gray-700">{item.inventory.toLocaleString()}</td>
                      <td className="px-5 py-3 text-gray-700">{item.maxPerUser}</td>
                      <td className="px-5 py-3">
                        <div className="flex items-center gap-2">
                          <Link
                            to={flashUrl}
                            className="text-blue-600 hover:underline text-xs font-mono"
                            target={sale.status === 'ACTIVE' ? '_blank' : undefined}
                          >
                            {truncateUuid(item.saleItemUuid)}
                          </Link>
                          <button
                            onClick={() => copyToClipboard(window.location.origin + flashUrl, item.saleItemUuid)}
                            className="text-gray-400 hover:text-gray-600 transition-colors"
                            title="Copy flash sale URL"
                          >
                            <ClipboardDocumentIcon className="h-3.5 w-3.5" />
                          </button>
                          {copiedId === item.saleItemUuid && (
                            <span className="text-xs text-green-600">Copied!</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          ) : (
            <p className="px-5 py-4 text-sm text-gray-400 italic">
              No items added yet. Click "+ Add Item" to add a product.
            </p>
          )}

          <div className="px-5 py-2.5 border-t border-gray-100 bg-gray-50">
            <p className="text-xs text-gray-400">Created {formatDate(sale.createdAt)}</p>
          </div>
        </div>
      ))}
    </div>
  );
}
