import { NavLink } from 'react-router-dom';
import {
  HomeIcon,
  CubeIcon,
  TagIcon,
  ShoppingBagIcon,
  BoltIcon,
} from '@heroicons/react/24/outline';
import { useAuthStore } from '@/store/authStore';

interface NavItem {
  label: string;
  to: string;
  icon: React.ComponentType<{ className?: string }>;
  adminOnly?: boolean;
}

const navItems: NavItem[] = [
  { label: 'Dashboard', to: '/dashboard', icon: HomeIcon },
  { label: 'Products', to: '/products', icon: CubeIcon },
  { label: 'Sale Management', to: '/admin/sales', icon: TagIcon, adminOnly: true },
  { label: 'Orders', to: '/orders/lookup', icon: ShoppingBagIcon },
];

export function Sidebar() {
  const role = useAuthStore((s) => s.user?.role);
  const isAdmin = role === 'ADMIN';

  const visibleItems = navItems.filter((item) => !item.adminOnly || isAdmin);

  return (
    <aside className="flex h-full w-56 flex-col bg-gray-900 text-gray-300">
      {/* Logo */}
      <div className="flex items-center gap-2 px-5 py-5 border-b border-gray-700">
        <BoltIcon className="h-6 w-6 text-blue-400" />
        <div>
          <p className="text-sm font-semibold text-white leading-tight">Flash Sale</p>
          <p className="text-xs text-gray-500">Engine</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
        {visibleItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              [
                'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors duration-150',
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-400 hover:bg-gray-800 hover:text-white',
              ].join(' ')
            }
          >
            <item.icon className="h-4 w-4 shrink-0" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* Role badge at bottom */}
      <div className="px-5 py-4 border-t border-gray-700">
        <span className={[
          'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
          isAdmin ? 'bg-blue-900 text-blue-300' : 'bg-gray-700 text-gray-400',
        ].join(' ')}>
          {role ?? 'USER'}
        </span>
      </div>
    </aside>
  );
}
