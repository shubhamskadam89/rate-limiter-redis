import axios from "axios";
import { zodResolver } from "@hookform/resolvers/zod";
import { LogIn, UserPlus, Copy, Check } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Navigate, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { z } from "zod";
import { normalizeApiError, type ApiError } from "../api/api-error";
import { useAuth } from "../auth/useAuth";
import { registerUser, registerSetupUser } from "../api/auth.api";
import { ApiErrorNotice } from "../components/common/ApiErrorNotice";
import { Button } from "../components/common/Button";
import { FormField } from "../components/common/FormField";
import { notifyError, notifySuccess } from "../utils/notify";

const loginSchema = z.object({
  email: z.string().email("Enter a valid email."),
  password: z.string().min(1, "Password is required.")
});

const registerSchema = z.object({
  fullName: z.string().min(1, "Full name is required."),
  email: z.string().email("Enter a valid email."),
  password: z.string().min(6, "Use at least 6 characters."),
  role: z.enum(["ADMIN", "USER"])
});

type LoginFormValues = z.infer<typeof loginSchema>;
type RegisterFormValues = z.infer<typeof registerSchema>;

type RegisteredUserDetail = {
  uuid: string;
  fullName: string;
  email: string;
  role: string;
};

// Mirrors the request pipeline documented in the README's Architecture Snapshot.
// Update this if the pipeline changes — it should always reflect the real flow.
const PIPELINE_STAGES = ["AUTH", "RATE LIMIT", "IDEMPOTENCY", "PURCHASE"];

const CAPABILITIES = [
  {
    label: "Rate limiting",
    detail: "Fixed window, sliding window, and token bucket strategies, enforced via Redis Lua scripts."
  },
  {
    label: "Async persistence",
    detail: "Purchase writes are queued in Redis and persisted to MySQL by a background worker."
  },
  {
    label: "Idempotent purchases",
    detail: "Duplicate purchase requests are rejected via idempotency-key filters at the API layer."
  }
];

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const [loginError, setLoginError] = useState<ApiError | null>(null);
  const [registerError, setRegisterError] = useState<ApiError | null>(null);

  const [registeredUser, setRegisteredUser] = useState<RegisteredUserDetail | null>(null);
  const [copied, setCopied] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();

  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname;

  const activeTab = searchParams.get("tab") === "register" ? "register" : "login";

  const loginForm = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" }
  });

  const registerForm = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { fullName: "", email: "", password: "", role: "ADMIN" }
  });

  if (isAuthenticated) return <Navigate to="/purchase" replace />;

  async function onLoginSubmit(values: LoginFormValues) {
    setLoginError(null);
    try {
      const user = await login(values);
      notifySuccess("Signed in successfully.", { id: "auth-login-success" });
      navigate(from ?? (user.role === "ADMIN" ? "/admin/sales" : "/purchase"), { replace: true });
    } catch (caught) {
      const normalized = normalizeApiError(caught);
      const customError = {
        ...normalized,
        message:
          normalized.status === 500
            ? "Sign in failed. Check your credentials and try again."
            : normalized.message
      };
      setLoginError(customError);
      notifyError(customError, "Unable to sign in. Please verify your credentials.", {
        id: "auth-login-error"
      });
    }
  }

  async function onRegisterSubmit(values: RegisterFormValues) {
    setRegisterError(null);
    setRegisteredUser(null);
    setCopied(false);
    try {
      // Attempt registerSetupUser first to preserve the selected role.
      // Fallback to standard registerUser if registration setup endpoint is unavailable.
      let res;
      try {
        res = await registerSetupUser(values);
      } catch (err: unknown) {
        if (axios.isAxiosError(err) && (err.response?.status === 404 || err.status === 404)) {
          res = await registerUser(values);
        } else {
          throw err;
        }
      }

      setRegisteredUser({
        uuid: res.uuid || "N/A",
        fullName: values.fullName,
        email: values.email,
        role: res.role || values.role
      });

      notifySuccess("Setup account created successfully.", { id: "register-success" });
    } catch (caught) {
      const normalized = normalizeApiError(caught);
      setRegisterError(normalized);
      notifyError(normalized, "Registration failed. Please try again.", { id: "register-error" });
    }
  }

  const handleCopyUuid = () => {
    if (registeredUser?.uuid) {
      navigator.clipboard.writeText(registeredUser.uuid);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleAutofillAndSwitch = () => {
    if (registeredUser) {
      loginForm.setValue("email", registeredUser.email);
      loginForm.setValue("password", registerForm.getValues("password"));
      setSearchParams({ tab: "login" });
      setRegisteredUser(null);
    }
  };

  return (
    <main className="flex min-h-screen bg-white">
      {/* Left Column: System Overview */}
      <div className="hidden w-1/2 bg-slate-950 text-slate-200 md:flex flex-col justify-between p-12 lg:p-16">
        <div>
          <div className="mb-16">
            <span className="font-mono text-xs tracking-[0.2em] text-slate-500 uppercase">
              System
            </span>
            <h1 className="mt-2 text-2xl font-semibold tracking-tight text-white">
              Flash Sale Engine
            </h1>
          </div>

          <p className="max-w-sm text-[15px] leading-relaxed text-slate-400">
            A distributed Spring Boot backend coordinating flash sale campaigns, order
            queues, and inventory safety under concurrent load.
          </p>

          {/* Signature element: the real request pipeline, not decoration */}
          <div className="mt-10 flex flex-wrap items-center gap-x-2 gap-y-2 font-mono text-[11px] tracking-wide text-slate-500">
            {PIPELINE_STAGES.map((stage, i) => (
              <span key={stage} className="flex items-center gap-2">
                <span className="rounded border border-slate-800 px-2 py-1 text-slate-400">
                  {stage}
                </span>
                {i < PIPELINE_STAGES.length - 1 && (
                  <span className="text-slate-700">→</span>
                )}
              </span>
            ))}
          </div>

          <div className="mt-14 space-y-7 max-w-sm border-t border-slate-800 pt-10">
            {CAPABILITIES.map((item) => (
              <div key={item.label}>
                <h3 className="text-sm font-medium text-white">{item.label}</h3>
                <p className="mt-1 text-[13px] leading-relaxed text-slate-500">
                  {item.detail}
                </p>
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-5 font-mono text-[11px] text-slate-600">
          <span>Spring Boot</span>
          <span>Redis</span>
          <span>MySQL</span>
          <span>React</span>
        </div>
      </div>

      {/* Right Column: Sign In & Register Forms */}
      <div className="w-full md:w-1/2 flex items-center justify-center p-6 md:p-12 lg:p-16">
        <div className="w-full max-w-sm">

          {/* Logo for mobile view */}
          <div className="mb-10 md:hidden">
            <span className="font-mono text-xs tracking-[0.2em] text-slate-400 uppercase">
              System
            </span>
            <h1 className="mt-1 text-xl font-semibold tracking-tight text-slate-950">
              Flash Sale Engine
            </h1>
          </div>

          {/* Form Tabs Header */}
          <div className="flex border-b border-slate-200 mb-8">
            <button
              type="button"
              onClick={() => {
                setSearchParams({ tab: "login" });
                setRegisteredUser(null);
                setLoginError(null);
                setRegisterError(null);
              }}
              className={`flex-1 pb-3 text-center text-sm font-medium border-b-2 transition ${activeTab === "login"
                ? "border-slate-900 text-slate-900"
                : "border-transparent text-slate-400 hover:text-slate-700"
                }`}
            >
              Sign in
            </button>
            <button
              type="button"
              onClick={() => {
                setSearchParams({ tab: "register" });
                setRegisteredUser(null);
                setLoginError(null);
                setRegisterError(null);
              }}
              className={`flex-1 pb-3 text-center text-sm font-medium border-b-2 transition ${activeTab === "register"
                ? "border-slate-900 text-slate-900"
                : "border-transparent text-slate-400 hover:text-slate-700"
                }`}
            >
              Create account
            </button>
          </div>

          {activeTab === "login" ? (
            <div className="space-y-6 animate-fadeIn">
              <div>
                <h2 className="text-xl font-semibold text-slate-950">Welcome back</h2>
                <p className="mt-1 text-sm text-slate-500">
                  Sign in to browse the catalog or manage sale campaigns.
                </p>
              </div>

              <form className="space-y-4" onSubmit={loginForm.handleSubmit(onLoginSubmit)}>
                <ApiErrorNotice error={loginError} title="Sign in failed" />
                <FormField
                  label="Email"
                  type="email"
                  placeholder="name@company.com"
                  autoComplete="email"
                  {...loginForm.register("email")}
                  error={loginForm.formState.errors.email?.message}
                />
                <FormField
                  label="Password"
                  type="password"
                  placeholder="••••••••"
                  autoComplete="current-password"
                  {...loginForm.register("password")}
                  error={loginForm.formState.errors.password?.message}
                />
                <Button
                  type="submit"
                  className="w-full mt-2 bg-slate-950 hover:bg-slate-800"
                  disabled={loginForm.formState.isSubmitting}
                >
                  <LogIn size={16} /> {loginForm.formState.isSubmitting ? "Signing in..." : "Sign in"}
                </Button>
              </form>
            </div>
          ) : (
            <div className="space-y-6 animate-fadeIn">
              <div>
                <h2 className="text-xl font-semibold text-slate-950">Create an account</h2>
                <p className="mt-1 text-sm text-slate-500">
                  Register as an admin to configure sales, or a user to purchase.
                </p>
              </div>

              {registeredUser ? (
                /* Success Card with UUID Copy Button */
                <div className="p-5 rounded-lg border border-slate-200 bg-slate-50 space-y-4">
                  <div className="flex items-center gap-2">
                    <div className="flex h-5 w-5 items-center justify-center rounded-full bg-slate-900 text-white">
                      <Check size={12} className="stroke-[3]" />
                    </div>
                    <span className="font-medium text-slate-900 text-sm">Account created</span>
                  </div>

                  <div className="space-y-1.5 text-sm text-slate-700">
                    <p><span className="text-slate-500">Name</span> — {registeredUser.fullName}</p>
                    <p><span className="text-slate-500">Email</span> — {registeredUser.email}</p>
                    <p>
                      <span className="text-slate-500">Role</span> —{" "}
                      <span className="font-mono text-xs">{registeredUser.role}</span>
                    </p>

                    <div className="mt-3 pt-3 border-t border-slate-200 flex flex-col gap-1.5">
                      <span className="text-slate-500 text-xs tracking-wide uppercase">User UUID</span>
                      <div className="flex gap-2 items-center bg-white border border-slate-200 rounded-md p-2.5 font-mono text-xs">
                        <span className="truncate flex-1 text-slate-700">{registeredUser.uuid}</span>
                        <button
                          type="button"
                          onClick={handleCopyUuid}
                          className="px-2 py-1 rounded bg-slate-100 hover:bg-slate-200 transition text-slate-600 flex items-center gap-1.5 text-[11px] font-medium shrink-0"
                          title="Copy UUID to clipboard"
                        >
                          {copied ? <Check size={12} /> : <Copy size={12} />}
                          {copied ? "Copied" : "Copy"}
                        </button>
                      </div>
                    </div>
                  </div>

                  <Button
                    type="button"
                    className="w-full mt-1 bg-slate-950 hover:bg-slate-800"
                    onClick={handleAutofillAndSwitch}
                  >
                    Continue to sign in
                  </Button>
                </div>
              ) : (
                <form className="space-y-4" onSubmit={registerForm.handleSubmit(onRegisterSubmit)}>
                  <ApiErrorNotice error={registerError} title="Registration failed" />
                  <FormField
                    label="Full name"
                    placeholder="Jane Doe"
                    autoComplete="name"
                    {...registerForm.register("fullName")}
                    error={registerForm.formState.errors.fullName?.message}
                  />
                  <FormField
                    label="Email address"
                    type="email"
                    placeholder="name@company.com"
                    autoComplete="email"
                    {...registerForm.register("email")}
                    error={registerForm.formState.errors.email?.message}
                  />
                  <FormField
                    label="Password"
                    type="password"
                    placeholder="At least 6 characters"
                    autoComplete="new-password"
                    {...registerForm.register("password")}
                    error={registerForm.formState.errors.password?.message}
                  />
                  <label className="block text-sm font-medium text-slate-700">
                    Account role
                    <select
                      className="mt-1 block min-h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-900/10"
                      {...registerForm.register("role")}
                    >
                      <option value="ADMIN">Admin — configure and activate sales</option>
                      <option value="USER">User — browse catalog and purchase</option>
                    </select>
                  </label>
                  <Button
                    type="submit"
                    className="w-full mt-2 bg-slate-950 hover:bg-slate-800"
                    disabled={registerForm.formState.isSubmitting}
                  >
                    <UserPlus size={16} /> {registerForm.formState.isSubmitting ? "Creating account..." : "Register"}
                  </Button>
                </form>
              )}
            </div>
          )}
        </div>
      </div>
    </main>
  );
}