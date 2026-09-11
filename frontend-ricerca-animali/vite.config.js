import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

// Build scoped al solo widget della barra di ricerca (vedi src/ricercaAnimali.jsx):
// produce UN SOLO file .js gia' compilato (JSX gia' trasformato in fase di
// build, React incluso nel bundle) dentro src/main/resources/static/js, cosi'
// che animali.html lo carichi con un normale <script>, senza bisogno di
// Babel o React da CDN a runtime nel browser di chi visita il sito.
export default defineConfig({
    plugins: [react()],
    // Senza questo "define", in modalita' "lib" React puo' restare in modalita'
    // sviluppo dentro al bundle (controlli/avvisi extra, molto piu' pesante):
    // questo lo forza sempre in produzione, come fa "vite build" per un'app normale.
    define: {
        'process.env.NODE_ENV': JSON.stringify('production'),
    },
    build: {
        outDir: fileURLToPath(new URL('../src/main/resources/static/js', import.meta.url)),
        emptyOutDir: false,
        lib: {
            entry: fileURLToPath(new URL('./src/ricercaAnimali.jsx', import.meta.url)),
            name: 'RicercaAnimaliWidget',
            formats: ['iife'],
            fileName: () => 'ricercaAnimali.js',
        },
    },
});
