import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useCreateSale } from '@/hooks/useSales';
import { formatLocalDateTimeForApi } from '@/utils/formatters';

const schema = z.object({
  name: z.string().min(2, 'Sale name must be at least 2 characters'),
  startTime: z.string().min(1, 'Start time is required'),
  endTime: z.string().min(1, 'End time is required'),
}).refine((d) => new Date(d.endTime) > new Date(d.startTime), {
  message: 'End time must be after start time',
  path: ['endTime'],
});

type FormValues = z.infer<typeof schema>;

interface Props {
  onSuccess: () => void;
}

export function CreateSaleForm({ onSuccess }: Props) {
  const { mutate: createSale, isPending } = useCreateSale();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const onSubmit = (data: FormValues) => {
    createSale(
      {
        name: data.name,
        startTime: formatLocalDateTimeForApi(new Date(data.startTime)),
        endTime: formatLocalDateTimeForApi(new Date(data.endTime)),
      },
      {
        onSuccess: () => {
          reset();
          onSuccess();
        },
      }
    );
  };

  // Default values: start 1 min from now, end 2 hours from now
  const now = new Date();
  const startDefault = new Date(now.getTime() + 60 * 1000);
  const endDefault = new Date(now.getTime() + 120 * 60 * 1000);
  const toInputValue = (d: Date) =>
    d.toISOString().slice(0, 16); // YYYY-MM-DDTHH:mm

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <Input
        label="Sale name"
        placeholder="e.g. Summer Flash Sale"
        error={errors.name?.message}
        {...register('name')}
      />
      <Input
        label="Start time"
        type="datetime-local"
        defaultValue={toInputValue(startDefault)}
        error={errors.startTime?.message}
        {...register('startTime')}
      />
      <Input
        label="End time"
        type="datetime-local"
        defaultValue={toInputValue(endDefault)}
        error={errors.endTime?.message}
        {...register('endTime')}
      />
      <div className="flex justify-end gap-3 pt-2">
        <Button type="submit" loading={isPending}>
          Create Sale
        </Button>
      </div>
    </form>
  );
}
