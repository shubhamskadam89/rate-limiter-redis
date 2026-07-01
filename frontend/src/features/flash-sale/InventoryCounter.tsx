import { useEffect, useRef, useState } from 'react';

type InventoryCounterProps = {
  remainingInventory?: number | null;
  className?: string;
};

/**
 * Displays the live inventory count. Briefly highlights the number
 * when it changes (CSS animation — no JS animation library needed).
 */
export function InventoryCounter({ remainingInventory, className = '' }: InventoryCounterProps) {
  const [flash, setFlash] = useState(false);
  const prevValueRef = useRef<number | null | undefined>(undefined);

  useEffect(() => {
    if (
      prevValueRef.current !== undefined &&
      prevValueRef.current !== null &&
      prevValueRef.current !== remainingInventory
    ) {
      setFlash(true);
      const timer = setTimeout(() => setFlash(false), 800);
      return () => clearTimeout(timer);
    }
    prevValueRef.current = remainingInventory;
  }, [remainingInventory]);

  if (remainingInventory === undefined || remainingInventory === null) {
    return <span className={`text-gray-400 ${className}`}>—</span>;
  }

  const isCritical = remainingInventory <= 10;
  const isSoldOut = remainingInventory === 0;

  return (
    <span
      className={[
        'tabular-nums transition-colors rounded-sm px-1',
        isSoldOut
          ? 'text-red-600 font-semibold'
          : isCritical
          ? 'text-amber-600 font-semibold'
          : 'text-gray-900 font-semibold',
        flash ? 'inventory-flash' : '',
        className,
      ].join(' ')}
    >
      {isSoldOut ? 'Sold Out' : remainingInventory.toLocaleString()}
    </span>
  );
}
