# Use Node.js base image
FROM node:22-alpine AS base

# Change to the specific app folder
WORKDIR /app

# Ensure pnpm runs non-interactively in Docker builds
ENV CI=true

# Install pnpm globally
RUN npm install -g pnpm@11.6.0 --ignore-scripts

# Copy dependency manifests first to leverage Docker layer caching
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml tsconfig.json next.config.ts tailwind.config.ts postcss.config.js /app/

# Use only read-only registries during image dependency installation.
RUN printf '%s\n' \
  '@gazelle:registry=https://nexus.ihe-catalyst.net/repository/npm-public/' \
  'registry=https://registry.npmjs.org' \
  'strict-ssl=false' \
  'loglevel=notice' \
  > .npmrc

# Install dependencies with pnpm using BuildKit cache
RUN --mount=type=cache,id=pnpm-store,target=/pnpm/store \
    pnpm install --frozen-lockfile --ignore-scripts

# Copy the rest of the application code after dependencies are installed
COPY . /app/

ENV NEXT_PUBLIC_GZL_LANGUAGE_BRETON_ENABLED="true"
ENV NEXT_PUBLIC_GZL_LANGUAGE_CZECH_ENABLED="true"

# Build the Next.js application
RUN pnpm run build

# Expose port 3030 for the app
EXPOSE 3030

# Define the runner stage for production
FROM node:22-alpine AS runner

# Set environment variables
ENV PORT=3030
ENV HOSTNAME="127.0.0.1"

# Create a non-root user for running the app
RUN addgroup --system --gid 1001 nodejs && adduser --system --uid 1001 nextjs

# Install curl
RUN apk add --no-cache curl

# Set the working directory to the built app folder
WORKDIR /app

# Copy the build files from the build stage
COPY --from=base --chown=nextjs:nodejs /app/.next/standalone .next/standalone
COPY --from=base --chown=nextjs:nodejs /app/.next/static .next/standalone/.next/static

# Copy required files
COPY --chown=nextjs:nodejs resources /app/.next/standalone/resources
COPY --chown=nextjs:nodejs public /app/.next/standalone/public

# Set the user to 'nextjs' for running the app
#USER nextjs [Error: EACCES: permission denied, mkdir '/opt/gazelle-user-interface/home']
USER root

# Start the application
CMD ["node", "/app/.next/standalone/server.js"]
