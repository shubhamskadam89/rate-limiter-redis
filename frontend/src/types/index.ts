// ─── User & Auth ────────────────────────────────────────────────────────────

export type UserRole = 'USER' | 'VIP' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  role?: UserRole;
}

export interface UserResponseDto {
  uuid: string;
  email: string;
  role: UserRole;
  isActive: boolean;
}

export interface AuthUser {
  email: string;
  role: UserRole;
  userId: number;
}

// ─── Products ────────────────────────────────────────────────────────────────

export interface ProductResponse {
  uuid: string;
  name: string;
  description: string | null;
  basePrice: number;
  metadata: Record<string, unknown> | null;
  isActive: boolean;
}

export interface CreateProductRequest {
  name: string;
  description?: string;
  basePrice: number;
  metadata?: Record<string, unknown>;
}

// ─── Sales ───────────────────────────────────────────────────────────────────

export type SaleStatus = 'DRAFT' | 'ACTIVE' | 'ENDED' | 'CANCELLED';

export interface SaleResponse {
  saleUuid: string;
  name: string;
  startTime: string;
  endTime: string;
  status: SaleStatus;
}

export interface CreateSaleRequest {
  name: string;
  startTime: string; // ISO-8601 LocalDateTime
  endTime: string;
}

export interface SaleItemResponse {
  saleItemUuid: string;
  saleEventUuid: string;
  productUuid: string;
  productName: string;
  salePrice: number;
  inventory: number;
  finalCount: number | null;
  maxPerUser: number;
}

export interface AddSaleItemRequest {
  productUuid: string;
  salePrice: number;
  inventory: number;
  maxPerUser: number;
}

// ─── Purchase & Orders ───────────────────────────────────────────────────────

export interface PurchaseRequest {
  quantity: number;
}

export interface PurchaseResponse {
  orderUuid: string;
  saleItemUuid: string;
  productUuid: string;
  quantity: number;
  remainingInventory: number;
  message: string;
}

export interface OrderResponse {
  orderUuid: string;
  saleItemUuid: string;
  productUuid: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  status: string;
  createdAt: string;
}

// ─── Errors ──────────────────────────────────────────────────────────────────

export interface ApiError {
  timestamp: string;
  status: number;
  message: string;
  errorCode: string;
  path: string;
  details: unknown | null;
}

// ─── SSE ─────────────────────────────────────────────────────────────────────

export interface StockUpdateEvent {
  saleUuid: string;
  saleItemUuid: string;
  remainingInventory: number;
  timestamp: string;
}

// ─── Local Sale Registry ─────────────────────────────────────────────────────

export interface LocalSaleEntry {
  saleUuid: string;
  name: string;
  status: SaleStatus;
  createdAt: string;
  saleItems: SaleItemResponse[];
}
