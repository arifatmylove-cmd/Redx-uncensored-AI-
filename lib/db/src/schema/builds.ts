import { pgTable, serial, text, integer, timestamp, jsonb } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const buildsTable = pgTable("builds", {
  id: serial("id").primaryKey(),
  appName: text("app_name").notNull(),
  description: text("description").notNull(),
  packageName: text("package_name").notNull().default("com.redxai.generated"),
  status: text("status").notNull().default("queued"), // queued | pushing | running | fixing | success | failed
  runId: integer("run_id"),
  attempt: integer("attempt").notNull().default(1),
  apkUrl: text("apk_url"),
  logs: text("logs"),
  fixSummary: text("fix_summary"),
  fixHistory: jsonb("fix_history").default([]),
  generatedFiles: jsonb("generated_files").default([]),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().notNull(),
});

export const settingsTable = pgTable("settings", {
  id: serial("id").primaryKey(),
  key: text("key").notNull().unique(),
  value: text("value").notNull(),
  updatedAt: timestamp("updated_at").defaultNow().notNull(),
});

export const insertBuildSchema = createInsertSchema(buildsTable).omit({ id: true, createdAt: true, updatedAt: true });
export const insertSettingSchema = createInsertSchema(settingsTable).omit({ id: true, updatedAt: true });

export type InsertBuild = z.infer<typeof insertBuildSchema>;
export type Build = typeof buildsTable.$inferSelect;
export type InsertSetting = z.infer<typeof insertSettingSchema>;
export type Setting = typeof settingsTable.$inferSelect;
