import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { productsApi } from '@/api/products';
import { extractErrorMessage } from '@/api/client';
import type { CreateProductRequest } from '@/types';

export const PRODUCTS_KEY = ['products'];

export function useProducts() {
  return useQuery({
    queryKey: PRODUCTS_KEY,
    queryFn: productsApi.getAll,
    staleTime: 30_000,
  });
}

export function useProduct(uuid: string) {
  return useQuery({
    queryKey: ['products', uuid],
    queryFn: () => productsApi.getOne(uuid),
    enabled: !!uuid,
  });
}

export function useCreateProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateProductRequest) => productsApi.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: PRODUCTS_KEY });
      toast.success('Product created successfully.');
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}
