import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Input, Select } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useAddSaleItem } from '@/hooks/useSales';
import { useProducts } from '@/hooks/useProducts';
import type { LocalSaleEntry } from '@/types';

const schema = z.object({
  productUuid: z.string().min(1, 'Select a product'),
  salePrice: z
    .string()
    .min(1, 'Sale price is required')
    .refine((v) => !isNaN(parseFloat(v)) && parseFloat(v) >= 0.01, 'Price must be at least 0.01'),
  inventory: z
    .string()
    .min(1, 'Inventory is required')
    .refine((v) => !isNaN(parseInt(v, 10)) && parseInt(v, 10) >= 1, 'Must be at least 1'),
  maxPerUser: z
    .string()
    .refine((v) => !isNaN(parseInt(v, 10)) && parseInt(v, 10) >= 1 && parseInt(v, 10) <= 10, 'Between 1 and 10'),
});

type FormInput = {
  productUuid: string;
  salePrice: string;
  inventory: string;
  maxPerUser: string;
};

interface Props {
  sale: LocalSaleEntry;
  onSuccess: () => void;
}

export function AddSaleItemForm({ sale, onSuccess }: Props) {
  const { data: products = [], isLoading: loadingProducts } = useProducts();
  const { mutate: addItem, isPending } = useAddSaleItem();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormInput>({
    resolver: zodResolver(schema) as never,
    defaultValues: { maxPerUser: '1' },
  });

  const onSubmit = (raw: FormInput) => {
    addItem(
      {
        saleUuid: sale.saleUuid,
        data: {
          productUuid: raw.productUuid,
          salePrice: parseFloat(raw.salePrice),
          inventory: parseInt(raw.inventory, 10),
          maxPerUser: parseInt(raw.maxPerUser, 10),
        },
      },
      {
        onSuccess: () => {
          reset();
          onSuccess();
        },
      }
    );
  };

  return (
    <form onSubmit={handleSubmit(onSubmit as never)} className="space-y-4" noValidate>
      <div className="rounded-md bg-blue-50 px-4 py-3 text-sm text-blue-700 border border-blue-200">
        Adding item to: <strong>{sale.name}</strong>
      </div>

      <Select
        label="Product"
        error={errors.productUuid?.message}
        disabled={loadingProducts}
        {...register('productUuid')}
      >
        <option value="">— Select a product —</option>
        {products.map((p) => (
          <option key={p.uuid} value={p.uuid}>
            {p.name} (base: ₹{p.basePrice.toLocaleString('en-IN')})
          </option>
        ))}
      </Select>

      <Input
        label="Sale price (₹)"
        type="number"
        step="0.01"
        min="0.01"
        hint="Must be less than or equal to base price"
        error={errors.salePrice?.message}
        {...register('salePrice')}
      />
      <Input
        label="Inventory (units)"
        type="number"
        min="1"
        step="1"
        error={errors.inventory?.message}
        {...register('inventory')}
      />
      <Input
        label="Max per user"
        type="number"
        min="1"
        max="10"
        step="1"
        error={errors.maxPerUser?.message}
        {...register('maxPerUser')}
      />

      <div className="flex justify-end gap-3 pt-2">
        <Button type="submit" loading={isPending}>
          Add Item
        </Button>
      </div>
    </form>
  );
}
