import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { salesApi } from '@/api/sales';
import { extractErrorMessage } from '@/api/client';
import type { AddSaleItemRequest, CreateSaleRequest, LocalSaleEntry } from '@/types';

const SALES_STORAGE_KEY = 'flash-sale-registry';
export const SALES_KEY = ['sales'];

export function getLocalSales(): LocalSaleEntry[] {
  try {
    const raw = localStorage.getItem(SALES_STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveLocalSales(sales: LocalSaleEntry[]) {
  localStorage.setItem(SALES_STORAGE_KEY, JSON.stringify(sales));
}

export function useSales() {
  return useQuery({
    queryKey: SALES_KEY,
    queryFn: salesApi.getAllSales,
    staleTime: 10_000,
  });
}

export function useCreateSale() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateSaleRequest) => salesApi.createSale(data),
    onSuccess: (sale) => {
      const current = getLocalSales();
      const entry: LocalSaleEntry = {
        saleUuid: sale.saleUuid,
        name: sale.name,
        status: sale.status,
        createdAt: new Date().toISOString(),
        saleItems: [],
      };
      saveLocalSales([entry, ...current]);
      qc.invalidateQueries({ queryKey: SALES_KEY });
      toast.success(`Sale "${sale.name}" created.`);
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}

export function useAddSaleItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ saleUuid, data }: { saleUuid: string; data: AddSaleItemRequest }) =>
      salesApi.addItem(saleUuid, data),
    onSuccess: (item, { saleUuid }) => {
      const current = getLocalSales();
      const updated = current.map((s) =>
        s.saleUuid === saleUuid
          ? { ...s, saleItems: [...s.saleItems, item] }
          : s
      );
      saveLocalSales(updated);
      qc.invalidateQueries({ queryKey: SALES_KEY });
      toast.success(`Item "${item.productName}" added to sale.`);
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}

export function useActivateSale() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (saleUuid: string) => salesApi.activateSale(saleUuid),
    onSuccess: (sale) => {
      const current = getLocalSales();
      const updated = current.map((s) =>
        s.saleUuid === sale.saleUuid ? { ...s, status: sale.status } : s
      );
      saveLocalSales(updated);
      qc.invalidateQueries({ queryKey: SALES_KEY });
      toast.success(`Sale activated and inventory loaded into Redis.`);
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error));
    },
  });
}
