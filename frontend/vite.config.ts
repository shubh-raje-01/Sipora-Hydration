import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],

  // ── Path aliases ==>
  // Mirrors the src/ folder structure so imports read naturally:
  //   import { Button } from "@/components/ui/Button"
  //   import { useAuth } from "@/hooks/useAuth"
  resolve: {
    alias: {
      "@":           path.resolve(__dirname, "./src"),
      "@api":        path.resolve(__dirname, "./src/api"),
      "@components": path.resolve(__dirname, "./src/components"),
      "@features":   path.resolve(__dirname, "./src/features"),
      "@hooks":      path.resolve(__dirname, "./src/hooks"),
      "@pages":      path.resolve(__dirname, "./src/pages"),
      "@store":      path.resolve(__dirname, "./src/store"),
      "@types":      path.resolve(__dirname, "./src/types"),
      "@utils":      path.resolve(__dirname, "./src/utils"),
    },
  },

  // ── Dev server ->
  server: {
    port: 5173,
    strictPort: true,
    open: true,

    // Proxy all /api calls to the Spring Boot backend during development.
    // This eliminates CORS issues — the browser sees requests going to
    // localhost:5173 but Vite silently forwards them to localhost:8080.
    proxy: {
      "/api": {
        target:      "http://localhost:8080",
        changeOrigin: true,
        secure:       false,
        // Log proxied requests in the terminal for debugging
        configure: (proxy) => {
          proxy.on("error", (err) => {
            console.error("[proxy] error", err.message);
          });
          proxy.on("proxyReq", (_proxyReq, req) => {
            console.log("[proxy]", req.method, req.url);
          });
        },
      },
    },
  },

  // ── Preview (production build local test) ->
  preview: {
    port: 4173,
  },

  // ── Build
  build: {
    // Target modern browsers only — no IE11 polyfills
    target: "es2020",

    // Output directory
    outDir: "dist",

    // Source maps for production debugging (disable if bundle size matters more)
    sourcemap: false,

    // Split the bundle at logical boundaries to improve initial load time.
    // Vendor chunks (React, router, query) are cached separately from app code.
    rollupOptions: {
      output: {
        manualChunks: {
          // Core React — rarely changes
          "vendor-react": ["react", "react-dom", "react-router-dom"],

          // Data layer — changes on library updates only
          "vendor-query": ["@tanstack/react-query", "axios", "zustand"],

          // Animation — large, separate chunk so it's only loaded when needed
          "vendor-motion": ["framer-motion"],

          // Form validation
          "vendor-forms": ["react-hook-form", "@hookform/resolvers", "zod"],
        },
      },
    },

    // Warn if any chunk exceeds 600kb
    chunkSizeWarningLimit: 600,
  },

  // ── Environment variables ->
  // Variables prefixed with VITE_ are exposed to the browser bundle.
  // NEVER put secrets here — only public config like feature flags.
  // All API calls go through the proxy above, so no API URL needed here.
});