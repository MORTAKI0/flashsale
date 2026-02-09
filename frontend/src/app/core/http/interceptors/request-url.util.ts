const URL_BASE = 'http://localhost';

function toPath(url: string): string {
  return new URL(url, URL_BASE).pathname;
}

export function isApiRequest(url: string): boolean {
  return toPath(url).startsWith('/api/');
}

export function isPublicRequest(url: string): boolean {
  const path = toPath(url);
  return path.startsWith('/public/') || path.startsWith('/api/public/');
}
