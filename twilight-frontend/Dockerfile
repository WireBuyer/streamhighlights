ARG NODE_VERSION=24.4-alpine
ARG NGINX_VERSION=alpine3.22

# Build Stage
FROM node:${NODE_VERSION} as build

# set working directory
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
RUN npm run build

# Production Stage
FROM nginxinc/nginx-unprivileged:${NGINX_VERSION} AS production

USER nginx

# better SPA support
COPY default.conf /etc/nginx/conf.d/default.conf
COPY --chown=nginx:nginx --from=build /app/dist /usr/share/nginx/html

EXPOSE 5173

CMD ["nginx", "-g", "daemon off;"] 