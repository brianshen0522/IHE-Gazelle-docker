import { ReactNode } from "react";
import Link from "next/link";
import { Route } from "next";

const AppLink = ({ href, children, className }: { href: string; children: ReactNode; className?: string }) => (
  <Link
    href={href as Route}
    className={`${className} flex items-center justify-center border border-purple p-2 rounded-md hover:bg-purple hover:text-white transition-colors duration-300 shadow-md`}
    target={href.startsWith("http") ? "_blank" : undefined}
    rel={href.startsWith("http") ? "noopener noreferrer" : undefined}
  >
    {children}
  </Link>
);

export default AppLink;
