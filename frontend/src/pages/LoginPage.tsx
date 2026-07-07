import { zodResolver } from "@hookform/resolvers/zod";
import { LogIn, UserPlus, Copy, Check, Zap, Cpu, Database, Activity } from "lucide-react";
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
      } catch (err: any) {
        if (err?.response?.status === 404 || err?.status === 404) {
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
    <main className="flex min-h-screen bg-slate-50">
      {/* Left Column: Project Architectural Overview (Only visible on MD+) */}
      <div className="hidden w-1/2 bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 text-white p-12 lg:p-16 md:flex flex-col justify-between relative overflow-hidden">
        {/* Background decorative glows */}
        <div className="absolute top-[-20%] left-[-20%] w-[85%] h-[85%] rounded-full bg-blue-500/10 blur-[130px] pointer-events-none"></div>
        <div className="absolute bottom-[-20%] right-[-20%] w-[85%] h-[85%] rounded-full bg-indigo-500/10 blur-[130px] pointer-events-none"></div>

        <div className="z-10">
          <div className="flex items-center gap-2 mb-10">
            <div className="flex items-center justify-center h-10 w-10 rounded-xl bg-gradient-to-tr from-blue-500 to-indigo-600 shadow-md">
              <Zap size={22} className="text-white fill-white" />
            </div>
            <span className="text-xl font-bold tracking-wider text-slate-100">Flash Sale Engine</span>
          </div>

          <div className="space-y-6">
            <h1 className="text-4xl lg:text-5xl font-extrabold tracking-tight leading-tight">
              High-Throughput <br />
              <span className="bg-gradient-to-r from-blue-400 to-indigo-300 bg-clip-text text-transparent">Commerce Engine</span>
            </h1>
            <p className="text-slate-300 text-base lg:text-lg leading-relaxed max-w-xl">
              An enterprise-ready distributed engine running on Spring Boot, designed to coordinate transactional flash sale campaigns, order placement queues, and inventory safeguards under load.
            </p>
          </div>

          {/* Architectural highlights */}
          <div className="mt-12 space-y-8 max-w-lg">
            <div className="flex gap-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-white/10 text-blue-400">
                <Cpu size={20} />
              </div>
              <div>
                <h3 className="font-semibold text-white">Multi-Strategy Rate Limiting</h3>
                <p className="text-sm text-slate-300 mt-1">
                  Enforces policies like Auth window limits or transactional locks using Redis Lua scripts (Token Bucket, Sliding Window, Fixed Window).
                </p>
              </div>
            </div>

            <div className="flex gap-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-white/10 text-indigo-400">
                <Database size={20} />
              </div>
              <div>
                <h3 className="font-semibold text-white">Asynchronous Persistence Queue</h3>
                <p className="text-sm text-slate-300 mt-1">
                  Funnels purchase updates from Redis into permanent relational storage using Java 21 virtual threads for optimal DB performance.
                </p>
              </div>
            </div>

            <div className="flex gap-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-white/10 text-emerald-400">
                <Activity size={20} />
              </div>
              <div>
                <h3 className="font-semibold text-white">Idempotent API Filters</h3>
                <p className="text-sm text-slate-300 mt-1">
                  Prevents double-purchase hazards and transaction duplications automatically via custom HTTP idempotency request header filters.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="z-10 border-t border-white/10 pt-8 mt-12">
          <div className="flex items-center gap-6 text-xs text-slate-400 font-mono">
            <span>Spring Boot 3.x</span>
            <span>Redis Sentinel</span>
            <span>React 18</span>
            <span>Tailwind CSS</span>
          </div>
        </div>
      </div>

      {/* Right Column: Sign In & Register Forms */}
      <div className="w-full md:w-1/2 flex items-center justify-center p-6 md:p-12 lg:p-16 bg-slate-50">
        <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-soft p-8 relative">

          {/* Logo for mobile view */}
          <div className="flex items-center gap-2 mb-8 md:hidden">
            <div className="flex items-center justify-center h-8 w-8 rounded-lg bg-gradient-to-tr from-blue-500 to-indigo-600 shadow-sm">
              <Zap size={18} className="text-white fill-white" />
            </div>
            <span className="text-lg font-bold tracking-wider text-slate-900">Flash Sale Engine</span>
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
              className={`flex-1 pb-3 text-center font-semibold text-sm border-b-2 transition ${activeTab === "login"
                  ? "border-primary-600 text-primary-600"
                  : "border-transparent text-slate-500 hover:text-slate-800"
                }`}
            >
              Sign In
            </button>
            <button
              type="button"
              onClick={() => {
                setSearchParams({ tab: "register" });
                setRegisteredUser(null);
                setLoginError(null);
                setRegisterError(null);
              }}
              className={`flex-1 pb-3 text-center font-semibold text-sm border-b-2 transition ${activeTab === "register"
                  ? "border-primary-600 text-primary-600"
                  : "border-transparent text-slate-500 hover:text-slate-800"
                }`}
            >
              Create Account
            </button>
          </div>

          {activeTab === "login" ? (
            <div className="space-y-6 animate-fadeIn">
              <div>
                <h2 className="text-2xl font-bold text-slate-950">Welcome Back</h2>
                <p className="mt-1.5 text-sm text-slate-500">Sign in to query product catalogs or configure admin sales campaigns.</p>
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
                <Button type="submit" className="w-full mt-2" disabled={loginForm.formState.isSubmitting}>
                  <LogIn size={16} /> {loginForm.formState.isSubmitting ? "Signing in..." : "Sign in"}
                </Button>
              </form>
            </div>
          ) : (
            <div className="space-y-6 animate-fadeIn">
              <div>
                <h2 className="text-2xl font-bold text-slate-950">Get Started</h2>
                <p className="mt-1.5 text-sm text-slate-500">Create a developer or admin account to start exploring campaigns.</p>
              </div>

              {registeredUser ? (
                /* Success Card with UUID Copy Button */
                <div className="p-5 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-950 space-y-4 shadow-sm animate-fadeIn">
                  <div className="flex items-center gap-2">
                    <div className="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-500 text-white">
                      <Check size={12} className="stroke-[3]" />
                    </div>
                    <span className="font-bold text-emerald-800 text-sm">Registration Successful!</span>
                  </div>

                  <div className="space-y-2 text-sm">
                    <p><span className="font-semibold text-emerald-800">Name:</span> {registeredUser.fullName}</p>
                    <p><span className="font-semibold text-emerald-800">Email:</span> {registeredUser.email}</p>
                    <p>
                      <span className="font-semibold text-emerald-800">Role:</span>{" "}
                      <span className="px-2 py-0.5 rounded bg-emerald-200/50 text-[11px] font-bold text-emerald-800">
                        {registeredUser.role}
                      </span>
                    </p>

                    <div className="mt-3 pt-3 border-t border-emerald-100 flex flex-col gap-1.5">
                      <span className="font-semibold text-emerald-800 text-xs tracking-wider uppercase">User UUID</span>
                      <div className="flex gap-2 items-center bg-white border border-emerald-200 rounded-lg p-2.5 font-mono text-xs select-all shadow-inner">
                        <span className="truncate flex-1 text-slate-700 font-medium">{registeredUser.uuid}</span>
                        <button
                          type="button"
                          onClick={handleCopyUuid}
                          className="px-2.5 py-1 rounded bg-slate-100 hover:bg-slate-200 transition text-slate-600 flex items-center gap-1.5 text-[11px] font-medium border border-slate-200 shadow-sm shrink-0"
                          title="Copy UUID to clipboard"
                        >
                          {copied ? <Check size={12} className="text-green-600 stroke-[3]" /> : <Copy size={12} />}
                          {copied ? "Copied" : "Copy"}
                        </button>
                      </div>
                    </div>
                  </div>

                  <Button
                    type="button"
                    className="w-full mt-3 bg-emerald-600 hover:bg-emerald-700 text-white border-none shadow"
                    onClick={handleAutofillAndSwitch}
                  >
                    Auto-fill & Switch to Sign In
                  </Button>
                </div>
              ) : (
                <form className="space-y-4" onSubmit={registerForm.handleSubmit(onRegisterSubmit)}>
                  <ApiErrorNotice error={registerError} title="Registration failed" />
                  <FormField
                    label="Full Name"
                    placeholder="Jane Doe"
                    autoComplete="name"
                    {...registerForm.register("fullName")}
                    error={registerForm.formState.errors.fullName?.message}
                  />
                  <FormField
                    label="Email Address"
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
                    Account Role
                    <select
                      className="mt-1 block min-h-10 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-soft outline-none transition focus:border-primary-600 focus:ring-2 focus:ring-primary-600/20"
                      {...registerForm.register("role")}
                    >
                      <option value="ADMIN">ADMIN (Configure & Activate Sales)</option>
                      <option value="USER">USER (Browse Catalogs & Purchase)</option>
                    </select>
                  </label>
                  <Button type="submit" className="w-full mt-2" disabled={registerForm.formState.isSubmitting}>
                    <UserPlus size={16} /> {registerForm.formState.isSubmitting ? "Creating account..." : "Register User"}
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
