/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import { useSearchParams } from "next/navigation";
import { useAclPrivacy } from "../useAclPrivacy";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { Session } from "next-auth";
import * as aclActions from "@/shared/utils/acl/aclActions";

// Mock dependencies
vi.mock("next/navigation", () => ({
  useSearchParams: vi.fn(),
}));

vi.mock("@/shared/utils/acl/aclActions");

describe("useAclPrivacy", () => {
  const mockSession: Session = {
      user: {
          gazelleId: "user-123",
          name: "Test User",
          email: "test@example.com",
          id: "",
          groups: [],
          organization: ""
      },
      access_token: "mock-token",
      expires: "2099-01-01",
      refresh_token: "",
      expires_in: 0,
      id_token: "",
      error: ""
  };

  const mockOnAclUpdate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useSearchParams).mockReturnValue({
      get: vi.fn(() => null),
    } as any);

    // Mock global location
    Object.defineProperty(globalThis, "location", {
      value: {
        href: "http://localhost:3000/test",
      },
      writable: true,
    });
  });

  describe("policy determination", () => {
    it("should return 'public' policy when isPublic is true", () => {
      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: true,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.policy.type).toBe("public");
      expect(result.current.policy.label).toBe("Public");
    });

    it("should return 'link' policy when readAccessKey exists", () => {
      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
        readAccessKey: "access-key-123",
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.policy.type).toBe("link");
      expect(result.current.policy.label).toBe("Anyone with the link");
    });

    it("should return 'link' policy when readAccessKey in URL params", () => {
      vi.mocked(useSearchParams).mockReturnValue({
        get: vi.fn((key) => (key === "readAccessKey" ? "url-key" : null)),
      } as any);

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.policy.type).toBe("link");
    });

    it("should return 'users' policy when 'user' is in readers", () => {
      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: ["user"],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.policy.type).toBe("users");
      expect(result.current.policy.label).toBe("Gazelle users");
    });

    it("should return 'private' policy by default", () => {
      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.policy.type).toBe("private");
      expect(result.current.policy.label).toBe("Private");
    });
  });

  describe("currentURL generation", () => {
    it("should generate URL with access key", () => {
      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
        readAccessKey: "test-key-456",
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.currentURL).toBe("http://localhost:3000/test?readAccessKey=test-key-456");
    });

    it("should add access key with & if URL already has params", () => {
      Object.defineProperty(globalThis, "location", {
        value: {
          href: "http://localhost:3000/test?existing=param",
        },
        writable: true,
      });

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
        readAccessKey: "test-key-789",
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.currentURL).toBe("http://localhost:3000/test?existing=param&readAccessKey=test-key-789");
    });

    it("should return current URL when no access key", () => {
      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      expect(result.current.currentURL).toBe("http://localhost:3000/test");
    });
  });

  describe("onPrivacyPolicyChange", () => {
    it("should call updatePrivacyPolicy action", async () => {
      const updatedAcl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: true,
      };

      vi.spyOn(aclActions, "updatePrivacyPolicy").mockResolvedValue(updatedAcl);

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      await act(async () => {
        await result.current.onPrivacyPolicyChange("public");
      });

      await waitFor(() => {
        expect(aclActions.updatePrivacyPolicy).toHaveBeenCalledWith({
          policy: "public",
          itemId: "item-123",
          currentAcl: acl,
          session: mockSession,
          t: expect.any(Function),
          customPersist: undefined,
        });
      });
    });

    it("should call onAclUpdate callback with updated ACL", async () => {
      const updatedAcl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: true,
      };

      vi.spyOn(aclActions, "updatePrivacyPolicy").mockResolvedValue(updatedAcl);

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      await act(async () => {
        await result.current.onPrivacyPolicyChange("public");
      });

      await waitFor(() => {
        expect(mockOnAclUpdate).toHaveBeenCalledWith(updatedAcl);
      });
    });

    it("should handle errors gracefully", async () => {
      const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});
      vi.spyOn(aclActions, "updatePrivacyPolicy").mockRejectedValue(new Error("Update failed"));

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      await act(async () => {
        await result.current.onPrivacyPolicyChange("public");
      });

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith("Failed to update privacy policy:", expect.any(Error));
      });

      consoleErrorSpy.mockRestore();
    });

    it("should pass custom persist function to updatePrivacyPolicy", async () => {
      const customPersist = vi.fn().mockResolvedValue({});
      const updatedAcl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: true,
      };

      vi.spyOn(aclActions, "updatePrivacyPolicy").mockResolvedValue(updatedAcl);

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
          customPersist,
        }),
      );

      await act(async () => {
        await result.current.onPrivacyPolicyChange("public");
      });

      await waitFor(() => {
        expect(aclActions.updatePrivacyPolicy).toHaveBeenCalledWith(
          expect.objectContaining({
            customPersist,
          }),
        );
      });
    });

    it("should set isUpdating state during update", async () => {
      let resolveUpdate: (value: AccessControlList) => void;
      const updatePromise = new Promise<AccessControlList>((resolve) => {
        resolveUpdate = resolve;
      });

      vi.spyOn(aclActions, "updatePrivacyPolicy").mockReturnValue(updatePromise);

      const acl: AccessControlList = {
        owners: ["user-123"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclPrivacy({
          acl,
          itemId: "item-123",
          session: mockSession,
          onAclUpdate: mockOnAclUpdate,
        }),
      );

      let changePromise: Promise<void>;
      act(() => {
        changePromise = result.current.onPrivacyPolicyChange("public");
      });

      await waitFor(() => {
        expect(result.current.isUpdating).toBe(true);
      });

      await act(async () => {
        resolveUpdate!({
          ...acl,
          isPublic: true,
        });

        await changePromise!;
      });

      await waitFor(() => {
        expect(result.current.isUpdating).toBe(false);
      });
    });
  });
});
