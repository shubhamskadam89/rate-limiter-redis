import { useState } from 'react';
import { PlusIcon } from '@heroicons/react/24/outline';
import { PageHeader } from '@/components/ui/PageHeader';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { ProductTable } from '@/features/products/ProductTable';
import { ProductForm } from '@/features/products/ProductForm';
import { useAuthStore } from '@/store/authStore';

export default function ProductsPage() {
  const role = useAuthStore((s) => s.user?.role);
  const isAdmin = role === 'ADMIN';
  const [showCreate, setShowCreate] = useState(false);

  return (
    <div>
      <PageHeader
        title="Products"
        description="Product catalogue — base prices and metadata."
        action={
          isAdmin ? (
            <Button
              size="sm"
              onClick={() => setShowCreate(true)}
              id="create-product-btn"
            >
              <PlusIcon className="h-4 w-4" />
              New Product
            </Button>
          ) : undefined
        }
      />
      <ProductTable />

      {isAdmin && (
        <Modal
          open={showCreate}
          onClose={() => setShowCreate(false)}
          title="Create Product"
        >
          <ProductForm onSuccess={() => setShowCreate(false)} />
        </Modal>
      )}
    </div>
  );
}
