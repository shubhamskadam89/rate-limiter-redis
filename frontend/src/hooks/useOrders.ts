import { useMutation, useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ordersApi } from '@/api/orders';
import { extractErrorMessage } from '@/api/client';
import type { PurchaseRequest } from '@/types';

export function useOrder(orderUuid: string) {
  return useQuery({
    queryKey: ['orders', orderUuid],
    queryFn: () => ordersApi.getOrder(orderUuid),
    enabled: !!orderUuid,
  });
}

export function usePurchase(saleUuid: string, saleItemUuid: string) {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: ({ data, idempotencyKey }: { data: PurchaseRequest; idempotencyKey: string }) =>
      ordersApi.purchase(saleUuid, saleItemUuid, idempotencyKey, data),
    onSuccess: (response) => {
      toast.success(response.message || 'Purchase confirmed!');
      navigate(`/orders/${response.orderUuid}`);
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}
