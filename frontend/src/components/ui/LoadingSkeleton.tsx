interface LoadingSkeletonProps {
  rows?: number;
  className?: string;
}

export function LoadingSkeleton({ rows = 5, className = '' }: LoadingSkeletonProps) {
  return (
    <div className={`animate-pulse space-y-3 ${className}`} aria-label="Loading...">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4 items-center">
          <div className="h-4 w-1/4 rounded bg-gray-200" />
          <div className="h-4 w-1/3 rounded bg-gray-200" />
          <div className="h-4 w-1/5 rounded bg-gray-200" />
          <div className="h-4 flex-1 rounded bg-gray-200" />
        </div>
      ))}
    </div>
  );
}

export function CardSkeleton({ className = '' }: { className?: string }) {
  return (
    <div className={`animate-pulse rounded-lg bg-white border border-gray-200 p-6 ${className}`}>
      <div className="h-4 w-1/3 rounded bg-gray-200 mb-3" />
      <div className="h-8 w-1/2 rounded bg-gray-200 mb-2" />
      <div className="h-3 w-2/3 rounded bg-gray-200" />
    </div>
  );
}
