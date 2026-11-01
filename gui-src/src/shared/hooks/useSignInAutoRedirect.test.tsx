import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook } from "@testing-library/react";
import { useSession } from "next-auth/react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import useSignInAutoRedirect from "./useSignInAutoRedirect";

vi.mock("next-auth/react");
vi.mock("next/navigation");

describe("useSignInAutoRedirect", () => {
  const push = vi.fn();

  const run = (callbackUrl: string) => {
    vi.mocked(useSearchParams).mockReturnValue(new URLSearchParams({ callbackUrl }) as never);
    renderHook(() => useSignInAutoRedirect());
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useRouter).mockReturnValue({ push } as never);
    vi.mocked(usePathname).mockReturnValue("/signin");
    vi.mocked(useSession).mockReturnValue({ status: "authenticated" } as never);
  });

  it("follows a same-origin callback URL", () => {
    run("/validation-portal/profiles");

    expect(push).toHaveBeenCalledWith("/validation-portal/profiles");
  });

  it.each(["https://attacker.test/", "//attacker.test/", "/\\attacker.test/", "javascript:alert(1)"])(
    "does not follow %s",
    (callbackUrl) => {
      run(callbackUrl);

      expect(push).toHaveBeenCalledWith("/");
    }
  );
});
