import { useState } from 'react';
import { PlusIcon } from '@heroicons/react/24/outline';
import { DataTable } from '@/components/ui/DataTable';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { useProducts } from '@/hooks/useProducts';
import { useAuthStore } from '@/store/authStore';
import { formatCurrency, truncateUuid } from '@/utils/formatters';
import { ProductForm } from './ProductForm';
import type { ProductResponse } from '@/types';

export function ProductTable() {
  const { data: products = [], isLoading } = useProducts();
  const role = useAuthStore((s) => s.user?.role);
  const isAdmin = role === 'ADMIN';
  const [showCreate, setShowCreate] = useState(false);

  return (
    <>
      <DataTable<ProductResponse>
        columns={[
          {
            key: 'name',
            header: 'Name',
            render: (p) => (
              <span className="font-medium text-gray-900">{p.name}</span>
            ),
          },
          {
            key: 'description',
            header: 'Description',
            render: (p) => (
              <span className="text-gray-500 max-w-xs truncate block">
                {p.description ?? '—'}
              </span>
            ),
          },
          {
            key: 'basePrice',
            header: 'Base Price',
            render: (p) => formatCurrency(p.basePrice),
          },
          {
            key: 'isActive',
            header: 'Status',
            render: (p) => (
              <Badge variant={p.isActive ? 'success' : 'default'}>
                {p.isActive ? 'Active' : 'Inactive'}
              </Badge>
            ),
          },
          {
            key: 'uuid',
            header: 'UUID',
            render: (p) => (
              <code className="text-xs text-gray-400 font-mono" title={p.uuid}>
                {truncateUuid(p.uuid)}
              </code>
            ),
          },
        ]}
        data={products}
        keyExtractor={(p) => p.uuid}
        loading={isLoading}
        emptyTitle="No products yet"
        emptyDescription={
          isAdmin ? 'Create a product to get started.' : 'No products are available.'
        }
        emptyAction={
          isAdmin ? (
            <Button size="sm" onClick={() => setShowCreate(true)}>
              <PlusIcon className="h-4 w-4" />
              New Product
            </Button>
          ) : undefined
        }
      />

      {isAdmin && (
        <Modal
          open={showCreate}
          onClose={() => setShowCreate(false)}
          title="Create Product"
        >
          <ProductForm onSuccess={() => setShowCreate(false)} />
        </Modal>
      )}
    </>
  );
}
