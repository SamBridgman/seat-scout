import { build } from 'esbuild';
import { readFile, writeFile } from 'node:fs/promises';

const result = await build({
  entryPoints: ['src/main.jsx'],
  outfile: 'offline.js',
  bundle: true,
  format: 'iife',
  platform: 'browser',
  target: 'es2020',
  minify: true,
  write: false,
  define: { 'process.env.NODE_ENV': '"production"' },
});
const javascript = result.outputFiles.find(file => file.path.endsWith('.js'))?.text;
const css = result.outputFiles.find(file => file.path.endsWith('.css'))?.text;
if (!javascript || !css) throw new Error('Offline bundle is missing JavaScript or CSS.');
const template = await readFile('index.html', 'utf8');
const html = template
  .replace('</head>', `<style>${css.replace(/<\/style/gi, '<\\/style')}</style></head>`)
  .replace(/<script\b[^>]*src="\/src\/main\.jsx"[^>]*><\/script>/, () => `<script>${javascript.replace(/<\/script/gi, '<\\/script')}</script>`);
if (html.includes('src="/src/main.jsx"')) throw new Error('Development entry was not replaced.');
await writeFile('Seat-Scout.html', html);
console.log('Built Seat-Scout.html: standalone, no server or network required.');
