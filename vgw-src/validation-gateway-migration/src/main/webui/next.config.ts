import type { NextConfig } from "next";

const basePath = "/evs-migration";

const nextConfig: NextConfig = {
  output: "export",
  trailingSlash: false,
  basePath,
  assetPrefix: `${basePath}/`,
};

export default nextConfig;
