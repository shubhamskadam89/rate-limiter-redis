import { ArrowRightOnRectangleIcon, UserCircleIcon } from '@heroicons/react/24/outline';
import { useAuthStore } from '@/store/authStore';
import { useLogout } from '@/hooks/useAuth';
import { Button } from '@/components/ui/Button';

export function Topbar() {
  const user = useAuthStore((s) => s.user);
  const logout = useLogout();

  return (
    <header className="flex h-14 shrink-0 items-center justify-between border-b border-gray-200 bg-white px-6">
      <div className="flex items-center gap-2 text-sm text-gray-500">
        <span className="font-medium text-gray-900">Flash Sale Engine</span>
        <span className="text-gray-300">—</span>
        <span>Demo Client</span>
      </div>

      <div className="flex items-center gap-4">
        {user && (
          <div className="flex items-center gap-2">
            <UserCircleIcon className="h-5 w-5 text-gray-400" />
            <span className="text-sm text-gray-700 font-medium">{user.email}</span>
          </div>
        )}
        <Button
          variant="ghost"
          size="sm"
          onClick={logout}
          className="flex items-center gap-1.5 text-gray-500 hover:text-gray-900"
        >
          <ArrowRightOnRectangleIcon className="h-4 w-4" />
          Logout
        </Button>
      </div>
    </header>
  );
}
