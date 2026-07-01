import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { PageHeader } from '@/components/ui/PageHeader';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';

export default function OrderLookupPage() {
  const [uuid, setUuid] = useState('');
  const navigate = useNavigate();

  const handleLookup = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = uuid.trim();
    if (trimmed) navigate(`/orders/${trimmed}`);
  };

  return (
    <div>
      <PageHeader
        title="Order Lookup"
        description="Retrieve an order by its UUID."
      />
      <div className="max-w-md">
        <form onSubmit={handleLookup} className="flex gap-3">
          <div className="flex-1">
            <Input
              placeholder="Enter Order UUID"
              value={uuid}
              onChange={(e) => setUuid(e.target.value)}
              id="order-uuid-input"
            />
          </div>
          <Button type="submit" disabled={!uuid.trim()} id="lookup-order-btn">
            <MagnifyingGlassIcon className="h-4 w-4" />
            Lookup
          </Button>
        </form>
        <p className="mt-3 text-xs text-gray-400">
          Order UUIDs are shown after a successful purchase, or can be found in the confirmation email.
        </p>
      </div>
    </div>
  );
}
