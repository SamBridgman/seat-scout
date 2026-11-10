import { createClient } from '@supabase/supabase-js';

// Optional catalog integration. Reports stay local in this presentation demo.
const url = import.meta.env.VITE_SUPABASE_URL;
const key = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY;
export const supabase = url && key ? createClient(url, key) : null;

export async function checkCatalogConnection() {
  if (!supabase) return { connected: false, message: 'Using sample data' };
  const { error } = await supabase.from('study_spaces').select('id').limit(1);
  return { connected: !error, message: error ? 'Catalog unavailable; using sample data' : 'Catalog reachable; demo uses sample data' };
}
