import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  root: 'src/main/frontend',
  build: {
    outDir: '../../target/frontend',
    emptyOutDir: true
  },
  resolve: {
    alias: {
      Frontend: path.resolve(__dirname, 'src/main/frontend')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
