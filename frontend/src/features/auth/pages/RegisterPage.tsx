import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../api/authApi';
import AuthLayout from '../components/AuthLayout';

const registerSchema = z
  .object({
    firstName: z.string().trim().min(1, 'Enter your first name'),
    lastName: z.string().trim().min(1, 'Enter your last name'),
    email: z.string().email('Enter a valid email address'),
    password: z.string().min(8, 'Use at least 8 characters'),
    confirmPassword: z.string().min(1, 'Confirm your password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

type RegisterFormData = z.infer<typeof registerSchema>;

function getErrorMessage(error: unknown) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = error.response;
    if (typeof response === 'object' && response !== null && 'data' in response) {
      const data = response.data;
      if (typeof data === 'object' && data !== null && 'message' in data) {
        return String(data.message);
      }
    }
  }

  return 'We could not create your account. Please try again.';
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({ resolver: zodResolver(registerSchema) });

  const onSubmit = async ({ confirmPassword: _confirmPassword, ...data }: RegisterFormData) => {
    setServerError('');
    setSuccessMessage('');
    try {
      const response = await authApi.register(data);
      setSuccessMessage(response.data.message ?? 'Account created. Check your email to verify it.');
      window.setTimeout(() => navigate('/login'), 2200);
    } catch (error) {
      setServerError(getErrorMessage(error));
    }
  };

  return (
    <AuthLayout
      eyebrow="Start with clarity"
      title="Build a better money habit."
      description="Create your account and give every euro a direction that feels right for you."
      footer={
        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      }
    >
      <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
        {serverError && <p className="form-error" role="alert">{serverError}</p>}
        {successMessage && <p className="form-success" role="status">{successMessage}</p>}

        <div className="name-fields">
          <div className="field-group">
            <label htmlFor="register-first-name">First name</label>
            <input id="register-first-name" autoComplete="given-name" placeholder="Alex" {...register('firstName')} />
            {errors.firstName && <p className="field-error">{errors.firstName.message}</p>}
          </div>
          <div className="field-group">
            <label htmlFor="register-last-name">Last name</label>
            <input id="register-last-name" autoComplete="family-name" placeholder="Morgan" {...register('lastName')} />
            {errors.lastName && <p className="field-error">{errors.lastName.message}</p>}
          </div>
        </div>

        <div className="field-group">
          <label htmlFor="register-email">Email address</label>
          <input id="register-email" type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} />
          {errors.email && <p className="field-error">{errors.email.message}</p>}
        </div>

        <div className="field-group">
          <label htmlFor="register-password">Password</label>
          <input id="register-password" type="password" autoComplete="new-password" placeholder="At least 8 characters" {...register('password')} />
          {errors.password && <p className="field-error">{errors.password.message}</p>}
        </div>

        <div className="field-group">
          <label htmlFor="register-confirm-password">Confirm password</label>
          <input id="register-confirm-password" type="password" autoComplete="new-password" placeholder="Repeat your password" {...register('confirmPassword')} />
          {errors.confirmPassword && <p className="field-error">{errors.confirmPassword.message}</p>}
        </div>

        <button className="primary-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <div className="divider"><span>or continue with</span></div>
      <a className="google-button" href="http://localhost:8089/oauth2/authorization/google">
        <span className="google-icon" aria-hidden="true">G</span>
        Sign up with Google
      </a>
    </AuthLayout>
  );
}
