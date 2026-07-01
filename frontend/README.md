# Flash Sale Engine — Frontend

A production-quality React 19 + TypeScript + Vite demo client that showcases the Flash Sale Engine backend.

## Tech Stack

| Tool | Purpose |
|------|---------|
| React 19 | UI framework |
| TypeScript (strict) | Type safety |
| Vite 6 | Build tooling |
| React Router v6 | Client-side routing with lazy loading |
| TanStack Query v5 | Data fetching & caching |
| Axios | HTTP client with interceptors |
| Zustand | Auth state management with localStorage persistence |
| React Hook Form + Zod | Type-safe form validation |
| Tailwind CSS v4 | Utility-first styling |
| Heroicons | Icon set |
| react-hot-toast | Toast notifications |

---

## Local Development

### Prerequisites

- Node.js 20+
- Flash Sale Engine backend running on `http://localhost:8080`

### Setup

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on **http://localhost:5173** and proxies all `/api/*` requests to `http://localhost:8080` — no CORS configuration required.

### Environment

Copy `.env.example` to `.env` (not required for dev proxy mode):

```bash
cp .env.example .env
```

---

## Production (Docker)

```bash
# From the project root
cd docker
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8080 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

The frontend Nginx container proxies `/api/*` to the backend service internally, solving CORS without modifying the backend.

---

## Features

### Authentication
- JWT login/register with role selector (USER / VIP / ADMIN)
- Automatic silent token refresh on 401
- Logout clears tokens and redirects to login
- Role-based route protection (client-side)

### Dashboard
- Stat cards: total products, sales created, active sales, current user role
- Backend capabilities overview
- Quick links to active flash sales

### Products
- View all products in a data table
- Admin: create new products via modal form

### Sale Management (Admin only)
- Create sale → add product item → activate sale
- Activation loads inventory into Redis
- Copyable Flash Sale URLs for each item
- Status badges: DRAFT / ACTIVE / ENDED / CANCELLED

### Flash Sale Page
- Live inventory via **Server-Sent Events** (`/api/v1/stock-updates/:saleItemUuid`)
- Inventory counter briefly highlights on change (CSS animation)
- SSE connection indicator (live / connecting / reconnecting)
- Auto-reconnect with exponential backoff on disconnect
- Purchase with auto-generated idempotency key
- Sold out and limit exceeded states

### Orders
- Order lookup by UUID
- After purchase, redirected to order confirmation page

---

## API Limitations (Documented)

The frontend is built against the backend without modifications. Two API gaps were identified and documented:

1. **No `GET /api/v1/admin/sales` list endpoint**  
   The backend only exposes `POST /admin/sales` (create), not a list. Sale UUIDs are persisted to `localStorage` in the admin session. This is sufficient for demonstration purposes.

2. **No customer sale browse endpoint**  
   There is no `GET /api/v1/sales` to list active sales. The Flash Sale page URL (`/sales/:saleUuid/items/:saleItemUuid`) must be shared by the admin — exactly as the spec requires.

3. **No `role` claim in JWT**  
   The JWT only contains `sub` (email) and `userId`. Role is stored in Zustand from the login/register API response. Admin route protection is client-side only.

---

## Project Structure

```
src/
├── api/          # Axios client + per-resource API modules
├── components/
│   ├── ui/       # Button, Card, Badge, DataTable, Modal, Input, etc.
│   └── layout/   # AppLayout, Sidebar, Topbar
├── features/     # Feature-scoped components
│   ├── auth/     # LoginForm, RegisterForm
│   ├── products/ # ProductTable, ProductForm
│   ├── sales/    # SaleTable, CreateSaleForm, AddSaleItemForm
│   ├── flash-sale/ # FlashSaleCard, InventoryCounter
│   └── orders/   # OrderDetail
├── hooks/        # useAuth, useProducts, useSales, useOrders, useStockSSE
├── pages/        # Route-level page components
├── routes/       # React Router config + ProtectedRoute
├── store/        # Zustand auth store
├── types/        # TypeScript interfaces (matches backend DTOs exactly)
└── utils/        # JWT decode, currency/date formatters
```
