// LocalStorage persistence for conversations and settings

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt: number;
  error?: boolean;
}

export interface Conversation {
  id: string;
  title: string;
  model: string;
  messages: Message[];
  createdAt: number;
  updatedAt: number;
}

export interface Settings {
  veniceKey: string;
  defaultModel: string;
}

const CONVERSATIONS_KEY = 'redx_conversations';
const SETTINGS_KEY = 'redx_settings';

function uid(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

// ── Settings ────────────────────────────────────────────────────────────────

export function getSettings(): Settings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) return JSON.parse(raw);
  } catch {}
  return { veniceKey: '', defaultModel: 'dolphin-2.9.3-mistral-nemo-12b' };
}

export function saveSettings(s: Settings): void {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(s));
}

// ── Conversations ────────────────────────────────────────────────────────────

export function getConversations(): Conversation[] {
  try {
    const raw = localStorage.getItem(CONVERSATIONS_KEY);
    if (raw) return JSON.parse(raw);
  } catch {}
  return [];
}

function saveConversations(convs: Conversation[]): void {
  localStorage.setItem(CONVERSATIONS_KEY, JSON.stringify(convs));
}

export function createConversation(model: string): Conversation {
  const conv: Conversation = {
    id: uid(),
    title: 'New chat',
    model,
    messages: [],
    createdAt: Date.now(),
    updatedAt: Date.now(),
  };
  const all = getConversations();
  saveConversations([conv, ...all]);
  return conv;
}

export function getConversation(id: string): Conversation | null {
  return getConversations().find((c) => c.id === id) ?? null;
}

export function updateConversation(conv: Conversation): void {
  const all = getConversations();
  const idx = all.findIndex((c) => c.id === conv.id);
  if (idx >= 0) all[idx] = conv;
  else all.unshift(conv);
  saveConversations(all);
}

export function deleteConversation(id: string): void {
  saveConversations(getConversations().filter((c) => c.id !== id));
}

export function addMessage(convId: string, role: 'user' | 'assistant', content: string, error = false): Message {
  const conv = getConversation(convId);
  if (!conv) throw new Error('Conversation not found');
  const msg: Message = { id: uid(), role, content, createdAt: Date.now(), error };
  conv.messages = [...conv.messages, msg];
  conv.updatedAt = Date.now();
  // Auto-title from first user message
  if (role === 'user' && conv.messages.filter((m) => m.role === 'user').length === 1) {
    conv.title = content.slice(0, 48) + (content.length > 48 ? '…' : '');
  }
  updateConversation(conv);
  return msg;
}

export function updateLastAssistantMessage(convId: string, content: string, error = false): void {
  const conv = getConversation(convId);
  if (!conv) return;
  const msgs = [...conv.messages];
  for (let i = msgs.length - 1; i >= 0; i--) {
    if (msgs[i].role === 'assistant') {
      msgs[i] = { ...msgs[i], content, error };
      break;
    }
  }
  conv.messages = msgs;
  conv.updatedAt = Date.now();
  updateConversation(conv);
}
