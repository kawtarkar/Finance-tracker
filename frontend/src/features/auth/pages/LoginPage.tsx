import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../api/authApi';
import AuthLayout from '../components/AuthLayout';
import { useAuthStore } from '../store/authStore';

const loginSchema = z.object({
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(1, 'Enter your password'),
});

type LoginFormData = z.infer<typeof loginSchema>;

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

  return 'We could not sign you in. Check your details and try again.';
}

export default function LoginPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [serverError, setServerError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (data: LoginFormData) => {
    setServerError('');
    try {
      const response = await authApi.login(data);
      setAuth(response.data);
      navigate('/dashboard', { replace: true });
    } catch (error) {
      setServerError(getErrorMessage(error));
    }
  };

  return (
    <AuthLayout
      eyebrow="Welcome back"
      title="Your money, in focus."
      description="Sign in to pick up where you left off and keep your plans moving."
      footer={
        <p className="auth-footer">
          New to Finance Tracker? <Link to="/register">Create an account</Link>
        </p>
      }
    >
      <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
        {serverError && <p className="form-error" role="alert">{serverError}</p>}

        <div className="field-group">
          <label htmlFor="login-email">Email address</label>
          <input id="login-email" type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} />
          {errors.email && <p className="field-error">{errors.email.message}</p>}
        </div>

        <div className="field-group">
          <label htmlFor="login-password">Password</label>
          <input id="login-password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Enter your password" {...register('password')} />
          {errors.password && <p className="field-error">{errors.password.message}</p>}
        </div>

        <button className="primary-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>

      <button className="password-toggle" type="button" onClick={() => setShowPassword((value) => !value)}>
        {showPassword ? 'Hide password' : 'Show password'}
      </button>

      <div className="divider"><span>or continue with</span></div>
      <a className="google-button" href="http://localhost:8089/oauth2/authorization/google">
        <span className="google-icon" aria-hidden="true">G</span>
        Continue with Google
      </a>
    </AuthLayout>
  );
}
