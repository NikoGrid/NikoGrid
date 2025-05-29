FROM node:22-slim AS build

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable

RUN mkdir /opt/app
WORKDIR /opt/app

COPY package.json pnpm-lock.yaml ./
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile

COPY . .
ARG VITE_BACKEND_BASE_URL=/
RUN pnpm run build

FROM nginx:1.27-alpine-slim

LABEL org.opencontainers.image.source=https://github.com/NikoGrid/NikoGrid
EXPOSE 80

# Install curl for healtchecks
RUN apk --no-cache add curl

COPY ./docker/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /opt/app/dist /usr/share/nginx/html

# vi: ft=dockerfile
