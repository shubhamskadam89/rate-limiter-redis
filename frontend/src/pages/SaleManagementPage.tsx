import { useState, useEffect } from 'react';
import { PlusIcon } from '@heroicons/react/24/outline';
import { PageHeader } from '@/components/ui/PageHeader';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { SaleTable } from '@/features/sales/SaleTable';
import { CreateSaleForm } from '@/features/sales/CreateSaleForm';
import { AddSaleItemForm } from '@/features/sales/AddSaleItemForm';
import { getLocalSales, useSales } from '@/hooks/useSales';
import type { LocalSaleEntry, SaleResponse, SaleItemResponse } from '@/types';

export default function SaleManagementPage() {
  const [showCreate, setShowCreate] = useState(false);
  const [addItemSale, setAddItemSale] = useState<LocalSaleEntry | null>(null);
  
  // Fetch sales list directly from new backend GET endpoint
  const { data: backendSales = [], refetch } = useSales();
  const [sales, setSales] = useState<LocalSaleEntry[]>([]);

  // Synchronize backend sales with any local item registration info
  useEffect(() => {
    const local = getLocalSales();
    const merged: LocalSaleEntry[] = backendSales.map((bs: SaleResponse) => {
      // check if we have tracked items locally for this sale
      const match = local.find((l) => l.saleUuid === bs.saleUuid);
      return {
        saleUuid: bs.saleUuid,
        name: bs.name,
        status: bs.status,
        createdAt: match ? match.createdAt : new Date().toISOString(),
        saleItems: match ? match.saleItems : [] as SaleItemResponse[],
      };
    });
    setSales(merged);
  }, [backendSales]);

  const handleRefresh = () => {
    refetch();
  };

  return (
    <div>
      <PageHeader
        title="Sale Management"
        description="Create flash sales, add products, and activate them to load inventory into Redis."
        action={
          <Button size="sm" onClick={() => setShowCreate(true)} id="create-sale-btn">
            <PlusIcon className="h-4 w-4" />
            New Sale
          </Button>
        }
      />

      <SaleTable
        sales={sales}
        onAddItem={(sale) => setAddItemSale(sale)}
      />

      {/* Create Sale Modal */}
      <Modal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        title="Create Sale Event"
      >
        <CreateSaleForm
          onSuccess={() => {
            setShowCreate(false);
            handleRefresh();
          }}
        />
      </Modal>

      {/* Add Item Modal */}
      <Modal
        open={!!addItemSale}
        onClose={() => setAddItemSale(null)}
        title="Add Item to Sale"
        size="lg"
      >
        {addItemSale && (
          <AddSaleItemForm
            sale={addItemSale}
            onSuccess={() => {
              setAddItemSale(null);
              handleRefresh();
            }}
          />
        )}
      </Modal>
    </div>
  );
}
