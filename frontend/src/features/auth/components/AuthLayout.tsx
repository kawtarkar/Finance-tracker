import type { ReactNode } from 'react';
import './auth.css';

interface AuthLayoutProps {
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
  footer: ReactNode;
}

export default function AuthLayout({
  eyebrow,
  title,
  description,
  children,
  footer,
}: AuthLayoutProps) {
  return (
    <main className="auth-page">
      <section className="auth-showcase" aria-label="Finance Tracker overview">
        <div className="brand-lockup">
          <span className="brand-mark" aria-hidden="true">
            ft
          </span>
          <span>Finance Tracker</span>
        </div>

        <div className="showcase-content">
          <p className="showcase-kicker">A clearer view of your money</p>
          <h2>Make every decision count.</h2>
          <p className="showcase-copy">
            Bring your accounts, spending, and goals into one calm, focused
            place.
          </p>

          <div className="insight-card">
            <div className="insight-card-heading">
              <span>Monthly overview</span>
              <span className="status-dot">On track</span>
            </div>
            <strong>$4,280.50</strong>
            <span className="insight-label">Available balance</span>
            <div className="balance-bars" aria-hidden="true">
              <span />
              <span />
              <span />
              <span />
              <span />
              <span />
              <span />
            </div>
            <div className="insight-footer">
              <span>Income</span>
              <b>+$6,420</b>
              <span>Spent</span>
              <b>-$2,139</b>
            </div>
          </div>
        </div>

        <p className="showcase-note">Private by design. Built for your next move.</p>
      </section>

      <section className="auth-panel">
        <div className="auth-panel-inner">
          <p className="auth-eyebrow">{eyebrow}</p>
          <h1>{title}</h1>
          <p className="auth-description">{description}</p>
          {children}
          {footer}
        </div>
      </section>
    </main>
  );
}