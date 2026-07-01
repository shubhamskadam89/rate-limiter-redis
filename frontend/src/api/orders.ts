import apiClient from './client';
import type { OrderResponse, PurchaseRequest, PurchaseResponse } from '@/types';

export const ordersApi = {
  getOrder: async (orderUuid: string): Promise<OrderResponse> => {
    const res = await apiClient.get<OrderResponse>(`/v1/orders/${orderUuid}`);
    return res.data;
  },

  purchase: async (
    saleUuid: string,
    saleItemUuid: string,
    idempotencyKey: string,
    data: PurchaseRequest
  ): Promise<PurchaseResponse> => {
    const res = await apiClient.post<PurchaseResponse>(
      `/v1/sales/${saleUuid}/items/${saleItemUuid}/purchase`,
      data,
      {
        headers: { 'X-Idempotency-Key': idempotencyKey },
      }
    );
    return res.data;
  },
};
