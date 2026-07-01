import type { ReactNode } from 'react';
import { LoadingSkeleton } from './LoadingSkeleton';

interface CardProps {
  children: ReactNode;
  className?: string;
  padding?: boolean;
}

export function Card({ children, className = '', padding = true }: CardProps) {
  return (
    <div
      className={[
        'bg-white border border-gray-200 rounded-lg',
        padding ? 'p-6' : '',
        className,
      ].join(' ')}
    >
      {children}
    </div>
  );
}

interface StatCardProps {
  label: string;
  value: string | number;
  icon?: ReactNode;
  description?: string;
}

export function StatCard({ label, value, icon, description }: StatCardProps) {
  return (
    <Card>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500">{label}</p>
          <p className="mt-2 text-3xl font-semibold text-gray-900">{value}</p>
          {description && (
            <p className="mt-1 text-xs text-gray-400">{description}</p>
          )}
        </div>
        {icon && (
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
}

export function CardSkeleton({ className = '' }: { className?: string }) {
  void LoadingSkeleton; // imported for side-effect avoidance check
  return (
    <div className={`animate-pulse rounded-lg bg-white border border-gray-200 p-6 ${className}`}>
      <div className="h-4 w-1/3 rounded bg-gray-200 mb-3" />
      <div className="h-8 w-1/2 rounded bg-gray-200 mb-2" />
      <div className="h-3 w-2/3 rounded bg-gray-200" />
    </div>
  );
}

