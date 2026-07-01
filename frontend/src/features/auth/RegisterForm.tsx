import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link } from 'react-router-dom';
import { Input, Select } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useRegister } from '@/hooks/useAuth';
import type { UserRole } from '@/types';

const schema = z.object({
  fullName: z.string().min(2, 'Full name must be at least 2 characters'),
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  role: z.enum(['USER', 'VIP', 'ADMIN'] as const),
});

type FormValues = z.infer<typeof schema>;

export function RegisterForm() {
  const { mutate: register, isPending } = useRegister();

  const { register: field, handleSubmit, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'USER' },
  });

  const onSubmit = (data: FormValues) =>
    register({ ...data, role: data.role as UserRole });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <Input
        label="Full name"
        type="text"
        autoComplete="name"
        error={errors.fullName?.message}
        {...field('fullName')}
      />
      <Input
        label="Email address"
        type="email"
        autoComplete="email"
        error={errors.email?.message}
        {...field('email')}
      />
      <Input
        label="Password"
        type="password"
        autoComplete="new-password"
        hint="Minimum 8 characters"
        error={errors.password?.message}
        {...field('password')}
      />
      <Select
        label="Role"
        error={errors.role?.message}
        {...field('role')}
      >
        <option value="USER">User (Customer)</option>
        <option value="VIP">VIP</option>
        <option value="ADMIN">Admin</option>
      </Select>

      <Button type="submit" loading={isPending} className="w-full justify-center">
        Create account
      </Button>

      <p className="text-center text-sm text-gray-500">
        Already have an account?{' '}
        <Link to="/login" className="text-blue-600 hover:underline font-medium">
          Sign in
        </Link>
      </p>
    </form>
  );
}
