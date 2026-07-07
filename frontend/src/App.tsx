import { Navigate, Route, Routes } from "react-router-dom";
import { AdminRoute } from "./auth/AdminRoute";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { AppLayout } from "./components/layout/AppLayout";
import { AdminSalesPage } from "./pages/AdminSalesPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { OrderDetailPage } from "./pages/OrderDetailPage";
import { OrderLookupPage } from "./pages/OrderLookupPage";
import { ProductDetailPage } from "./pages/ProductDetailPage";
import { ProductsPage } from "./pages/ProductsPage";
import { PurchasePage } from "./pages/PurchasePage";
import { SetupRegisterPage } from "./pages/SetupRegisterPage";
import { UnauthorizedPage } from "./pages/UnauthorizedPage";

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/setup/register" element={<Navigate to="/login?tab=register" replace />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/purchase" replace />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:productUuid" element={<ProductDetailPage />} />
          <Route path="/purchase" element={<PurchasePage />} />
          <Route path="/orders/lookup" element={<OrderLookupPage />} />
          <Route path="/orders/:orderUuid" element={<OrderDetailPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />
          <Route element={<AdminRoute />}>
            <Route path="/admin/sales" element={<AdminSalesPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
