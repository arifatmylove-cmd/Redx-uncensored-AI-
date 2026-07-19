#!/usr/bin/env node
/**
 * Redx AI — Push Android project to GitHub and trigger build workflow
 */
import fs from "fs";
import path from "path";

const TOKEN = process.env.GITHUB_TOKEN || "";
const USERNAME = process.env.GITHUB_USERNAME || "";
const REPO_RAW = process.env.GITHUB_REPO_NAME || "redx-uncensored-ai";
// GitHub repo names can't have spaces; convert to hyphens
const REPO = REPO_RAW.replace(/\s+/g, "-");
const PROJECT_DIR = path.resolve(process.cwd(), "android-project");

if (!TOKEN || !USERNAME) {
  console.error("Missing GITHUB_TOKEN or GITHUB_USERNAME");
  process.exit(1);
}

const API = "https://api.github.com";
const HEADERS = {
  Authorization: `token ${TOKEN}`,
  Accept: "application/vnd.github.v3+json",
  "User-Agent": "RedxAI-Builder/1.0",
  "Content-Type": "application/json",
};

async function gh(method: string, endpoint: string, body?: unknown): Promise<Response> {
  const res = await fetch(`${API}${endpoint}`, {
    method,
    headers: HEADERS,
    body: body ? JSON.stringify(body) : undefined,
  });
  return res;
}

async function ensureRepo(): Promise<void> {
  console.log(`\n📦 Checking repo: ${USERNAME}/${REPO}`);
  const check = await gh("GET", `/repos/${USERNAME}/${REPO}`);
  if (check.status === 200) {
    console.log("  ✓ Repo exists");
    return;
  }
  console.log("  → Creating repo...");
  const create = await gh("POST", "/user/repos", {
    name: REPO,
    description: "Redx AI — Uncensored AI Android App",
    private: false,
    auto_init: true,
  });
  if (!create.ok) {
    const err = await create.text();
    // If repo already exists (422), that's fine
    if (create.status === 422 && err.includes("already exists")) {
      console.log("  ✓ Repo already exists");
      return;
    }
    throw new Error(`Failed to create repo: ${create.status} ${err}`);
  }
  console.log("  ✓ Repo created");
  // Give GitHub a moment
  await new Promise((r) => setTimeout(r, 2000));
}

async function getFileSha(filePath: string): Promise<string | undefined> {
  const res = await gh("GET", `/repos/${USERNAME}/${REPO}/contents/${filePath}`);
  if (res.status === 200) {
    const data = (await res.json()) as { sha?: string };
    return data.sha;
  }
  return undefined;
}

async function pushFile(relativePath: string, content: string, isBinary = false): Promise<void> {
  const encoded = isBinary
    ? content
    : Buffer.from(content, "utf-8").toString("base64");

  const sha = await getFileSha(relativePath);

  const res = await gh("PUT", `/repos/${USERNAME}/${REPO}/contents/${relativePath}`, {
    message: `Redx AI: ${sha ? "update" : "add"} ${relativePath}`,
    content: encoded,
    sha,
    branch: "main",
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Failed to push ${relativePath}: ${res.status} ${err.slice(0, 200)}`);
  }
}

function collectFiles(dir: string, base = ""): { rel: string; abs: string }[] {
  const files: { rel: string; abs: string }[] = [];
  for (const entry of fs.readdirSync(dir)) {
    const abs = path.join(dir, entry);
    const rel = base ? `${base}/${entry}` : entry;
    const stat = fs.statSync(abs);
    if (stat.isDirectory()) {
      // Skip hidden dirs (except .github)
      if (entry.startsWith(".") && entry !== ".github") continue;
      files.push(...collectFiles(abs, rel));
    } else {
      files.push({ rel, abs });
    }
  }
  return files;
}

async function run(): Promise<void> {
  await ensureRepo();

  const files = collectFiles(PROJECT_DIR);
  console.log(`\n📤 Pushing ${files.length} files to ${USERNAME}/${REPO}...\n`);

  let pushed = 0;
  let failed = 0;

  for (const { rel, abs } of files) {
    try {
      const content = fs.readFileSync(abs, "utf-8");
      await pushFile(rel, content);
      console.log(`  ✓ ${rel}`);
      pushed++;
      // Rate limit: 300ms between pushes
      await new Promise((r) => setTimeout(r, 300));
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : String(e);
      console.error(`  ✗ ${rel}: ${msg}`);
      failed++;
    }
  }

  console.log(`\n📊 Result: ${pushed} pushed, ${failed} failed`);
  console.log(`\n🔗 Repo: https://github.com/${USERNAME}/${REPO}`);
  console.log(`🔗 Actions: https://github.com/${USERNAME}/${REPO}/actions`);

  if (failed > 0) {
    console.warn("\n⚠️  Some files failed to push. Check errors above.");
    process.exit(1);
  }

  console.log("\n✅ All files pushed! GitHub Actions will now compile the APK.");
  console.log("   Monitor at: https://github.com/${USERNAME}/${REPO}/actions");
}

run().catch((e) => {
  console.error("Fatal:", e);
  process.exit(1);
});
