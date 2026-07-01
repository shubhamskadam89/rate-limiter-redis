import {
  CubeIcon,
  TagIcon,
  BoltIcon,
  UserCircleIcon,
} from '@heroicons/react/24/outline';
import { StatCard, CardSkeleton } from '@/components/ui/Card';
import { PageHeader } from '@/components/ui/PageHeader';
import { useAuthStore } from '@/store/authStore';
import { useProducts } from '@/hooks/useProducts';
import { getLocalSales } from '@/hooks/useSales';
import { Link } from 'react-router-dom';

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);
  const { data: products, isLoading: loadingProducts } = useProducts();
  const localSales = getLocalSales();
  const activeSales = localSales.filter((s) => s.status === 'ACTIVE');

  return (
    <div>
      <PageHeader
        title="Dashboard"
        description="Overview of the Flash Sale Engine backend features."
      />

      {/* Stat cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
        {loadingProducts ? (
          Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />)
        ) : (
          <>
            <StatCard
              label="Total Products"
              value={products?.length ?? 0}
              icon={<CubeIcon className="h-5 w-5" />}
              description="Registered in the catalogue"
            />
            <StatCard
              label="Sales Created"
              value={localSales.length}
              icon={<TagIcon className="h-5 w-5" />}
              description="Tracked in this session"
            />
            <StatCard
              label="Active Flash Sales"
              value={activeSales.length}
              icon={<BoltIcon className="h-5 w-5" />}
              description="Inventory loaded in Redis"
            />
            <StatCard
              label="Signed In As"
              value={user?.role ?? '—'}
              icon={<UserCircleIcon className="h-5 w-5" />}
              description={user?.email ?? ''}
            />
          </>
        )}
      </div>

      {/* Backend features callout */}
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <h2 className="text-sm font-semibold text-gray-900 mb-4">Backend Capabilities Demonstrated</h2>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {[
            { title: 'JWT + Refresh Tokens', desc: 'HS256 access tokens with automatic refresh on expiry.' },
            { title: 'Identity-Aware Rate Limiting', desc: 'Token bucket, sliding window, and fixed window algorithms.' },
            { title: 'Redis Lua Scripts', desc: 'Atomic inventory decrement and user purchase limit enforcement.' },
            { title: 'Zero Oversell Guarantee', desc: 'Lua script runs atomically — no race condition possible.' },
            { title: 'Async Order Queue', desc: 'LPUSH → BRPOP Redis list queue with persistence job.' },
            { title: 'Server-Sent Events', desc: 'Real-time stock updates via Redis Pub/Sub → SSE stream.' },
          ].map((f) => (
            <div key={f.title} className="rounded-md border border-gray-100 bg-gray-50 px-4 py-3">
              <p className="text-xs font-semibold text-gray-700">{f.title}</p>
              <p className="text-xs text-gray-500 mt-0.5">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Quick links for active sales */}
      {activeSales.length > 0 && (
        <div className="mt-6 rounded-lg border border-blue-100 bg-blue-50 p-5">
          <h3 className="text-sm font-semibold text-blue-800 mb-3">Active Flash Sales</h3>
          <div className="space-y-2">
            {activeSales.map((sale) =>
              sale.saleItems.map((item) => (
                <Link
                  key={item.saleItemUuid}
                  to={`/sales/${sale.saleUuid}/items/${item.saleItemUuid}`}
                  className="flex items-center justify-between rounded-md border border-blue-200 bg-white px-4 py-2.5 text-sm hover:bg-blue-50 transition-colors"
                >
                  <div>
                    <span className="font-medium text-gray-900">{item.productName}</span>
                    <span className="ml-2 text-gray-500">{sale.name}</span>
                  </div>
                  <span className="text-blue-600 text-xs font-medium">Open →</span>
                </Link>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
