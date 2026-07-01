import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useCreateProduct } from '@/hooks/useProducts';

const schema = z.object({
  name: z.string().min(1, 'Product name is required'),
  description: z.string().optional(),
  basePrice: z
    .string()
    .min(1, 'Base price is required')
    .refine((v) => !isNaN(parseFloat(v)) && parseFloat(v) >= 0.01, 'Price must be at least 0.01'),
});

type FormInput = { name: string; description?: string; basePrice: string };

interface Props {
  onSuccess: () => void;
}

export function ProductForm({ onSuccess }: Props) {
  const { mutate: create, isPending } = useCreateProduct();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormInput>({ resolver: zodResolver(schema) as never });

  const onSubmit = (raw: FormInput) => {
    create(
      {
        name: raw.name,
        description: raw.description || undefined,
        basePrice: parseFloat(raw.basePrice),
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
      <Input
        label="Product name"
        error={errors.name?.message}
        {...register('name')}
      />
      <Input
        label="Description"
        hint="Optional"
        error={errors.description?.message}
        {...register('description')}
      />
      <Input
        label="Base price (₹)"
        type="number"
        step="0.01"
        min="0.01"
        error={errors.basePrice?.message}
        {...register('basePrice')}
      />
      <div className="flex justify-end gap-3 pt-2">
        <Button type="submit" loading={isPending}>
          Create Product
        </Button>
      </div>
    </form>
  );
}
