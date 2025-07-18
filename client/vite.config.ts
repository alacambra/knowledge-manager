import { defineConfig } from 'vite'
import preact from '@preact/preset-vite'

export default defineConfig({
  plugins: [preact()],
  resolve: {
    alias: {
      "react": "preact/compat",
      "react-dom": "preact/compat"
    }
  },
  build: {
    sourcemap: true,
  },
  esbuild: {
    sourcemap: true,
  },
  css: {
    devSourcemap: true,
  }
})