import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  logLevel: "info",
  // server: {
  //   proxy: {
  //     "/api": {
  //       target: "http://localhost",
  //       changeOrigin: true,
  //     },
  //   },
  // },
});
