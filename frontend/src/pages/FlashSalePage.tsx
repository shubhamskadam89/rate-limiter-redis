import { useParams } from 'react-router-dom';
import { FlashSaleCard } from '@/features/flash-sale/FlashSaleCard';
import { Spinner } from '@/components/ui/Spinner';
import { getLocalSales } from '@/hooks/useSales';
import type { SaleItemResponse } from '@/types';

export default function FlashSalePage() {
  const { saleUuid, saleItemUuid } = useParams<{
    saleUuid: string;
    saleItemUuid: string;
  }>();

  if (!saleUuid || !saleItemUuid) {
    return (
      <div className="flex h-64 items-center justify-center text-sm text-gray-500">
        Invalid sale URL. Please check the link.
      </div>
    );
  }

  // Look up item and base price from local registry
  const localSales = getLocalSales();
  const sale = localSales.find((s) => s.saleUuid === saleUuid);
  let item: SaleItemResponse | undefined = sale?.saleItems.find(
    (i) => i.saleItemUuid === saleItemUuid
  );

  // If the sale item is NOT in the admin's local session registry (e.g. customer navigated directly),
  // construct a stub item with the required fields.
  if (!item) {
    item = {
      saleItemUuid: saleItemUuid,
      saleEventUuid: saleUuid,
      productUuid: '', // Will be updated by metadata if needed, but sse/purchase only require saleItemUuid
      productName: 'Flash Sale Item',
      salePrice: 0,
      inventory: 0,
      finalCount: null,
      maxPerUser: 1,
    };
  }

  return (
    <FlashSalePageContent
      saleUuid={saleUuid}
      saleItemUuid={saleItemUuid}
      item={item}
    />
  );
}

import { useProduct } from '@/hooks/useProducts';

function FlashSalePageContent({
  saleUuid,
  saleItemUuid,
  item,
}: {
  saleUuid: string;
  saleItemUuid: string;
  item: SaleItemResponse;
}) {
  const { data: product, isLoading } = useProduct(item.productUuid);

  if (isLoading && item.productUuid) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner className="h-8 w-8" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <h1 className="text-lg font-semibold text-gray-900">Flash Sale</h1>
        <p className="text-sm text-gray-500">Limited inventory — purchase before it runs out.</p>
      </div>
      <FlashSaleCard
        saleUuid={saleUuid}
        item={item}
        basePrice={product?.basePrice ?? item.salePrice}
      />
    </div>
  );
}
