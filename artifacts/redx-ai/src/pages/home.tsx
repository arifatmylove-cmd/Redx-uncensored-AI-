import { Download, Github, Smartphone, MessageSquare, Wrench, Zap, Shield, Star, ChevronRight } from "lucide-react";

const REPO = "https://github.com/arifatmylove-cmd/Redx-uncensored-AI-";
const APK_URL = `${REPO}/actions/runs/29689932907/artifacts/8443296221`;
const ACTIONS_URL = `${REPO}/actions`;

const features = [
  {
    icon: MessageSquare,
    title: "Uncensored AI Chat",
    desc: "Multi-session conversations with Dolphin-Llama-3-70b and 9+ unrestricted models via OpenRouter. No filters, no guardrails.",
  },
  {
    icon: Wrench,
    title: "APK Builder",
    desc: "Describe any Android app in plain language. Redx AI writes the Kotlin code, compiles it via GitHub Actions, and delivers the APK.",
  },
  {
    icon: Shield,
    title: "Bring Your Own Key",
    desc: "Your OpenRouter API key stays on your device. No data leaves to our servers. Total privacy by design.",
  },
  {
    icon: Zap,
    title: "Auto-Fix Engine",
    desc: "Build errors are caught, analyzed, and fixed automatically — up to 5 retry cycles with AI-powered error correction.",
  },
];

const steps = [
  { n: "01", title: "Download & Install", desc: "Grab the APK from GitHub Actions and install on your Android device (API 26+)." },
  { n: "02", title: "Add API Key", desc: "Open Settings and paste your OpenRouter API key. It's stored locally, never shared." },
  { n: "03", title: "Start Chatting", desc: "Select a model and start a conversation. No topics are off-limits." },
  { n: "04", title: "Build Apps", desc: "Use the Builder tab to describe any Android app — Redx compiles and delivers it as an APK." },
];

export default function Home() {
  return (
    <div className="min-h-screen bg-[#0f0f0f] text-[#f0ebe0] font-sans overflow-x-hidden">

      {/* Subtle background gradient */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[900px] h-[500px] bg-gradient-radial from-red-900/20 via-transparent to-transparent rounded-full blur-3xl" />
        <div className="absolute bottom-0 right-0 w-[400px] h-[400px] bg-gradient-radial from-amber-900/10 via-transparent to-transparent rounded-full blur-3xl" />
      </div>

      {/* Nav */}
      <nav className="relative z-10 flex items-center justify-between px-6 md:px-12 py-5 border-b border-white/5">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-red-600 to-red-800 flex items-center justify-center shadow-lg">
            <span className="text-white font-black text-sm">R</span>
          </div>
          <span className="font-bold text-lg tracking-wide text-white">Redx <span className="text-red-500">AI</span></span>
        </div>
        <div className="flex items-center gap-4">
          <a href={REPO} target="_blank" rel="noreferrer"
            className="flex items-center gap-1.5 text-sm text-[#a09880] hover:text-[#c8a84b] transition-colors">
            <Github className="w-4 h-4" /> GitHub
          </a>
          <a href={APK_URL} target="_blank" rel="noreferrer"
            className="flex items-center gap-1.5 text-sm font-semibold px-4 py-2 rounded-lg bg-red-700 hover:bg-red-600 text-white transition-colors shadow-md">
            <Download className="w-3.5 h-3.5" /> Download APK
          </a>
        </div>
      </nav>

      {/* Hero */}
      <section className="relative z-10 flex flex-col items-center text-center px-6 pt-20 pb-16 fade-up">
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold mb-8 fade-up fade-up-delay-1">
          <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
          APK Build Successful — Run #53
        </div>

        <h1 className="text-6xl md:text-8xl font-black tracking-tight leading-none mb-4 fade-up fade-up-delay-2">
          <span className="text-white">REDX</span>
          <span className="shimmer-text"> AI</span>
        </h1>

        <p className="text-xl md:text-2xl text-[#a09880] max-w-xl mx-auto mb-2 fade-up fade-up-delay-3 font-light">
          Uncensored Android AI assistant
        </p>
        <p className="text-sm text-[#6b6052] mb-10 fade-up fade-up-delay-3">
          Powered by Dolphin-Llama-3-70b &amp; OpenRouter · No filters · No limits
        </p>

        <div className="flex flex-col sm:flex-row items-center gap-3 fade-up fade-up-delay-4">
          <a href={APK_URL} target="_blank" rel="noreferrer"
            className="flex items-center gap-2 px-7 py-3.5 rounded-xl bg-gradient-to-r from-red-700 to-red-600 text-white font-bold text-sm shadow-[0_4px_24px_rgba(185,28,28,0.4)] hover:shadow-[0_4px_32px_rgba(185,28,28,0.6)] hover:scale-[1.02] transition-all">
            <Download className="w-4 h-4" />
            Download APK
            <ChevronRight className="w-3.5 h-3.5 ml-1 opacity-70" />
          </a>
          <a href={ACTIONS_URL} target="_blank" rel="noreferrer"
            className="flex items-center gap-2 px-7 py-3.5 rounded-xl border border-[#c8a84b]/30 text-[#c8a84b] font-semibold text-sm hover:bg-[#c8a84b]/8 hover:border-[#c8a84b]/50 transition-all">
            <Github className="w-4 h-4" />
            View Build
          </a>
        </div>
      </section>

      {/* Build status card */}
      <section className="relative z-10 px-6 md:px-12 pb-12 max-w-4xl mx-auto fade-up fade-up-delay-2">
        <div className="rounded-2xl border border-white/8 bg-[#161616] overflow-hidden shadow-xl">
          <div className="flex items-center justify-between px-6 py-4 border-b border-white/6 bg-[#111]">
            <div className="flex items-center gap-2 text-sm font-semibold text-[#a09880]">
              <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              GitHub Actions — Build Status
            </div>
            <a href={ACTIONS_URL} target="_blank" rel="noreferrer"
              className="text-xs text-[#c8a84b] hover:underline flex items-center gap-1">
              View all runs <ChevronRight className="w-3 h-3" />
            </a>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 divide-x divide-y md:divide-y-0 divide-white/6">
            {[
              { label: "Status", value: "Success", color: "text-emerald-400" },
              { label: "Build", value: "#53", color: "text-[#c8a84b]" },
              { label: "APK Size", value: "~24.5 MB", color: "text-white" },
              { label: "Platform", value: "Android 8+", color: "text-white" },
            ].map((item) => (
              <div key={item.label} className="px-6 py-5">
                <div className="text-xs text-[#6b6052] uppercase tracking-widest mb-1">{item.label}</div>
                <div className={`text-lg font-bold ${item.color}`}>{item.value}</div>
              </div>
            ))}
          </div>
          <div className="px-6 py-4 bg-[#111] border-t border-white/6">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-[#6b6052]">Compilation complete</span>
              <span className="text-xs font-semibold text-emerald-400">100%</span>
            </div>
            <div className="h-1.5 rounded-full bg-white/5 overflow-hidden">
              <div className="h-full w-full rounded-full bg-gradient-to-r from-red-700 via-red-600 to-amber-500" />
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="relative z-10 px-6 md:px-12 py-16 max-w-5xl mx-auto">
        <div className="text-center mb-12">
          <div className="inline-flex items-center gap-2 text-[#c8a84b] text-xs font-bold uppercase tracking-widest mb-3">
            <Star className="w-3.5 h-3.5" /> Core Features
          </div>
          <h2 className="text-3xl md:text-4xl font-black text-white">Everything in one APK</h2>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {features.map((f, i) => (
            <div key={i}
              className="group p-6 rounded-2xl border border-white/6 bg-[#141414] hover:border-red-800/40 hover:bg-[#181414] transition-all duration-300">
              <div className="flex items-start gap-4">
                <div className="shrink-0 w-10 h-10 rounded-xl bg-gradient-to-br from-red-800/50 to-red-900/30 border border-red-800/30 flex items-center justify-center group-hover:from-red-700/60 transition-all">
                  <f.icon className="w-5 h-5 text-red-400" />
                </div>
                <div>
                  <h3 className="font-bold text-white mb-1.5 text-[15px]">{f.title}</h3>
                  <p className="text-sm text-[#7a7060] leading-relaxed">{f.desc}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className="relative z-10 px-6 md:px-12 py-16 border-t border-white/5">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-12">
            <div className="inline-flex items-center gap-2 text-[#c8a84b] text-xs font-bold uppercase tracking-widest mb-3">
              <Zap className="w-3.5 h-3.5" /> How It Works
            </div>
            <h2 className="text-3xl md:text-4xl font-black text-white">Get started in minutes</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {steps.map((s, i) => (
              <div key={i} className="relative p-6 rounded-2xl bg-[#141414] border border-white/6 hover:border-amber-800/30 transition-all">
                <div className="text-4xl font-black text-white/5 mb-4 leading-none">{s.n}</div>
                <div className="w-px h-3 bg-gradient-to-b from-[#c8a84b] to-transparent mb-3" />
                <h3 className="font-bold text-white text-[15px] mb-2">{s.title}</h3>
                <p className="text-sm text-[#7a7060] leading-relaxed">{s.desc}</p>
                {i < steps.length - 1 && (
                  <ChevronRight className="absolute top-1/2 -right-3 -translate-y-1/2 w-5 h-5 text-[#c8a84b]/30 hidden lg:block" />
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Phone mockup CTA */}
      <section className="relative z-10 px-6 md:px-12 py-20 border-t border-white/5">
        <div className="max-w-3xl mx-auto flex flex-col md:flex-row items-center gap-10">
          {/* Icon visual */}
          <div className="float-anim shrink-0">
            <div className="w-32 h-32 rounded-3xl bg-gradient-to-br from-red-800 to-red-950 border border-red-700/30 shadow-[0_8px_40px_rgba(185,28,28,0.35)] flex items-center justify-center">
              <Smartphone className="w-14 h-14 text-red-300" />
            </div>
          </div>
          <div>
            <h2 className="text-3xl md:text-4xl font-black text-white mb-3">
              Ready on Android
            </h2>
            <p className="text-[#7a7060] leading-relaxed mb-6">
              Requires Android 8.0 (API 26) or higher. Install the debug APK directly — no Google Play needed. Enable "Install from unknown sources" in your device settings.
            </p>
            <a href={APK_URL} target="_blank" rel="noreferrer"
              className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-gradient-to-r from-red-700 to-red-600 text-white font-bold text-sm shadow-[0_4px_20px_rgba(185,28,28,0.35)] hover:shadow-[0_4px_28px_rgba(185,28,28,0.55)] hover:scale-[1.02] transition-all">
              <Download className="w-4 h-4" />
              Download app-debug.apk
            </a>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="relative z-10 border-t border-white/5 px-6 md:px-12 py-8">
        <div className="max-w-5xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-md bg-gradient-to-br from-red-600 to-red-800 flex items-center justify-center">
              <span className="text-white font-black text-xs">R</span>
            </div>
            <span className="text-sm font-semibold text-white">Redx AI</span>
          </div>
          <p className="text-xs text-[#4a4438] text-center">
            Powered by cognitivecomputations/dolphin-llama-3-70b · 9+ uncensored models via OpenRouter
          </p>
          <a href={REPO} target="_blank" rel="noreferrer"
            className="flex items-center gap-1.5 text-xs text-[#6b6052] hover:text-[#c8a84b] transition-colors">
            <Github className="w-3.5 h-3.5" /> Source Code
          </a>
        </div>
      </footer>
    </div>
  );
}
