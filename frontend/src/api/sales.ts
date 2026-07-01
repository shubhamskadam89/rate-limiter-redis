import apiClient from './client';
import type {
  AddSaleItemRequest,
  CreateSaleRequest,
  SaleItemResponse,
  SaleResponse,
} from '@/types';

export const salesApi = {
  createSale: async (data: CreateSaleRequest): Promise<SaleResponse> => {
    const res = await apiClient.post<SaleResponse>('/v1/admin/sales', data);
    return res.data;
  },

  addItem: async (
    saleUuid: string,
    data: AddSaleItemRequest
  ): Promise<SaleItemResponse> => {
    const res = await apiClient.post<SaleItemResponse>(
      `/v1/admin/sales/${saleUuid}/items`,
      data
    );
    return res.data;
  },

  activateSale: async (saleUuid: string): Promise<SaleResponse> => {
    const res = await apiClient.post<SaleResponse>(
      `/v1/admin/sales/${saleUuid}/activate`
    );
    return res.data;
  },

  getAllSales: async (): Promise<SaleResponse[]> => {
    const res = await apiClient.get<SaleResponse[]>('/v1/admin/sales');
    return res.data;
  },
};
