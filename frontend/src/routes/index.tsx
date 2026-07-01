import { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { FullPageSpinner } from '@/components/ui/Spinner';

const LoginPage = lazy(() => import('@/pages/LoginPage'));
const RegisterPage = lazy(() => import('@/pages/RegisterPage'));
const DashboardPage = lazy(() => import('@/pages/DashboardPage'));
const ProductsPage = lazy(() => import('@/pages/ProductsPage'));
const SaleManagementPage = lazy(() => import('@/pages/SaleManagementPage'));
const FlashSalePage = lazy(() => import('@/pages/FlashSalePage'));
const OrderPage = lazy(() => import('@/pages/OrderPage'));
const OrderLookupPage = lazy(() => import('@/pages/OrderLookupPage'));

const Wrap = ({ children }: { children: React.ReactNode }) => (
  <Suspense fallback={<FullPageSpinner />}>{children}</Suspense>
);

export const router = createBrowserRouter([
  // Public routes
  {
    path: '/login',
    element: <Wrap><LoginPage /></Wrap>,
  },
  {
    path: '/register',
    element: <Wrap><RegisterPage /></Wrap>,
  },

  // Protected routes inside AppLayout
  {
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      {
        path: '/dashboard',
        element: <Wrap><DashboardPage /></Wrap>,
      },
      {
        path: '/products',
        element: <Wrap><ProductsPage /></Wrap>,
      },
      {
        path: '/admin/sales',
        element: (
          <ProtectedRoute requiredRole="ADMIN">
            <Wrap><SaleManagementPage /></Wrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/sales/:saleUuid/items/:saleItemUuid',
        element: <Wrap><FlashSalePage /></Wrap>,
      },
      {
        path: '/orders/lookup',
        element: <Wrap><OrderLookupPage /></Wrap>,
      },
      {
        path: '/orders/:orderUuid',
        element: <Wrap><OrderPage /></Wrap>,
      },
    ],
  },

  // Catch-all
  { path: '*', element: <Navigate to="/dashboard" replace /> },
]);
