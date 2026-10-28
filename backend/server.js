const http = require('http');
const fs = require('fs');
const path = require('path');

const port = Number(process.env.PORT || 10000);
const publicDir = path.join(__dirname, '..', 'public');
const contentTypes = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.jpeg': 'image/jpeg',
  '.jpg': 'image/jpeg',
  '.png': 'image/png',
  '.svg': 'image/svg+xml'
};

function sendJson(response, status, value) {
  response.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
  response.end(JSON.stringify(value));
}

const server = http.createServer((request, response) => {
  const pathname = new URL(request.url, `http://${request.headers.host || 'localhost'}`).pathname;

  if (pathname === '/api/health') {
    sendJson(response, 200, { status: 'ok', service: 'fake-medicine-detector' });
    return;
  }

  const requestedPath = pathname === '/' ? 'index.html' : pathname.replace(/^\/+/, '');
  const filePath = path.resolve(publicDir, requestedPath);
  if (!filePath.startsWith(`${path.resolve(publicDir)}${path.sep}`) && filePath !== path.join(publicDir, 'index.html')) {
    sendJson(response, 403, { error: 'Forbidden' });
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      if (error.code === 'ENOENT') {
        fs.readFile(path.join(publicDir, 'index.html'), (fallbackError, fallbackData) => {
          if (fallbackError) {
            sendJson(response, 404, { error: 'Not found' });
            return;
          }
          response.writeHead(200, { 'Content-Type': contentTypes['.html'] });
          response.end(fallbackData);
        });
        return;
      }
      sendJson(response, 500, { error: 'Unable to read file' });
      return;
    }

    response.writeHead(200, { 'Content-Type': contentTypes[path.extname(filePath).toLowerCase()] || 'application/octet-stream' });
    response.end(data);
  });
});

server.listen(port, '0.0.0.0', () => {
  console.log(`Fake Medicine Detection web landing page listening on port ${port}`);
});
