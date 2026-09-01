import type { NextConfig } from "next";
import * as dotenv from "dotenv";
import path from "node:path";

dotenv.config({ path: path.resolve("../../.env") });

const nextConfig: NextConfig = {
  basePath: "/gazelle",
  output: "standalone",
  assetPrefix: "/gazelle",
  typedRoutes: true,
  logging: {
    browserToTerminal: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "*",
        pathname: "/**",
      },
    ],
  },
  // Keep both to support differing Next.js config shapes across versions.
  experimental: {
    // Request body cap for app proxy/middleware path (default is 10mb).
    proxyClientMaxBodySize: "50mb",
    serverActions: {
      bodySizeLimit: "50mb",
    },
  },
  async redirects() {
    return [
      {
        source: "/",
        destination: "/gazelle/home",
        basePath: false,
        permanent: true,
      },
      {
        source: "/gazelle",
        destination: "/gazelle/home",
        basePath: false,
        permanent: true,
      },
    ];
  },
};

export default nextConfig;
