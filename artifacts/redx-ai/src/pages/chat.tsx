import { Terminal } from "lucide-react";
import { Link } from "wouter";
import { Button } from "@/components/ui/button";

export default function ChatPlaceholder() {
  return (
    <div className="min-h-screen w-full bg-background text-foreground font-mono selection:bg-primary/30 flex flex-col items-center justify-center relative overflow-hidden">
      {/* Background Grid Pattern */}
      <div className="absolute inset-0 z-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]"></div>
      
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[400px] h-[400px] bg-primary/10 rounded-full blur-[80px] pointer-events-none"></div>

      <div className="relative z-10 flex flex-col items-center text-center gap-6 p-8 border border-primary/20 bg-card/50 backdrop-blur-sm rounded-lg shadow-[0_0_30px_-10px_rgba(204,0,0,0.3)] max-w-md w-full mx-4">
        <div className="p-4 bg-black/50 rounded-full border border-primary/30 relative">
          <Terminal className="w-8 h-8 text-primary" />
          <div className="absolute top-0 right-0 w-3 h-3 bg-primary rounded-full animate-ping"></div>
        </div>
        
        <div className="space-y-2">
          <h1 className="text-2xl font-bold uppercase tracking-widest text-primary drop-shadow-[0_0_8px_rgba(204,0,0,0.8)]">
            Web Terminal
          </h1>
          <h2 className="text-xl font-bold">COMING SOON</h2>
        </div>

        <p className="text-sm text-muted-foreground border-y border-border/50 py-4 w-full">
          The full uncensored chat interface is currently available exclusively in the Android APK. Web access is undergoing final security protocols.
        </p>

        <div className="w-full flex flex-col gap-3 mt-2">
          <Button asChild variant="outline" className="w-full border-primary/50 text-primary hover:bg-primary/10 font-bold uppercase tracking-widest">
            <Link href="/">
              ← Abort / Return to Dashboard
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}