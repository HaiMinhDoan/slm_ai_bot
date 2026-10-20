# syntax=docker/dockerfile:1.7

# CMS (vben admin).
#   docker build --target cms    .

# ---------- Cài phụ thuộc cho cả workspace ----------
FROM node:20-alpine AS deps
RUN corepack enable
WORKDIR /app
COPY . .
# cache mount cho kho pnpm: không phải layer nên vẫn dùng lại được sau `build --no-cache`.
# --frozen-lockfile: bản build phải khớp đúng pnpm-lock.yaml, không tự nâng phiên bản.
RUN --mount=type=cache,target=/root/.local/share/pnpm/store,sharing=locked \
    pnpm install --frozen-lockfile

# ---------- Build CMS ----------
FROM deps AS build-cms
# --mode deploy đọc .env.deploy: tắt mock, giữ VITE_GLOB_API_URL=/basic-api (nginx chuyển tiếp)
RUN NODE_OPTIONS=--max-old-space-size=4096 pnpm vite build --mode deploy


# ---------- Ảnh chạy: CMS ----------
FROM nginx:alpine AS cms
COPY docker/nginx-cms.conf /etc/nginx/conf.d/default.conf
COPY --from=build-cms /app/dist /usr/share/nginx/html
EXPOSE 5173
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:5173/ || exit 1

