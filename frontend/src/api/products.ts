import apiClient from './client';
import type { CreateProductRequest, ProductResponse } from '@/types';

export const productsApi = {
  getAll: async (): Promise<ProductResponse[]> => {
    const res = await apiClient.get<ProductResponse[]>('/v1/products');
    return res.data;
  },

  getOne: async (productUuid: string): Promise<ProductResponse> => {
    const res = await apiClient.get<ProductResponse>(`/v1/products/${productUuid}`);
    return res.data;
  },

  create: async (data: CreateProductRequest): Promise<ProductResponse> => {
    const res = await apiClient.post<ProductResponse>('/v1/products', data);
    return res.data;
  },
};
