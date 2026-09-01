import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import { useAclManagement } from "../useAclManagement";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { Session } from "next-auth";
import * as aclActions from "@/shared/utils/acl/aclActions";

// Mock dependencies
vi.mock("react-toastify", () => ({
  toast: {
    error: vi.fn(),
  },
}));

vi.mock("@message-capture/services/formatUserOrGroupById", () => ({
  formatUserOrGroupById: vi.fn((id: string) =>
    Promise.resolve({
      id,
      name: `User ${id}`,
      organization: `Org ${id}`,
    }),
  ),
}));

vi.mock("@shared/utils/acl/aclFormatters", () => ({
  formatWithTranslationWithOrga: vi.fn((value) => value),
}));

vi.mock("@/shared/utils/acl/aclActions");

import { toast } from "react-toastify";

describe("useAclManagement", () => {
  const mockSession: Session = {
    user: {
      gazelleId: "user-123",
      name: "Test User",
      email: "test@example.com",
      id: "",
      groups: [],
      organization: "",
    },
    access_token: "mock-token",
    expires: "2099-01-01",
    refresh_token: "",
    expires_in: 0,
    id_token: "",
    error: "",
  };

  const mockAcl: AccessControlList = {
    owners: ["user-123"],
    editors: ["user-456"],
    readers: ["user-789"],
    isPublic: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should initialize with provided ACL", async () => {
    const { result } = renderHook(() =>
      useAclManagement({
        initialAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      }),
    );

    await waitFor(() => {
      expect(result.current.acl).toEqual(mockAcl);
    });
  });

  it("should format members on mount", async () => {
    const { result } = renderHook(() =>
      useAclManagement({
        initialAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      }),
    );

    await waitFor(() => {
      expect(result.current.formattedMembers.owners).toHaveLength(1);
      expect(result.current.formattedMembers.editors).toHaveLength(1);
      expect(result.current.formattedMembers.viewers).toHaveLength(1);
    });
  });

  describe("onChangeToOwner", () => {
    it("should call changeUserToOwner action", async () => {
      const updatedAcl = { ...mockAcl, owners: ["user-123", "user-456"] };
      vi.spyOn(aclActions, "changeUserToOwner").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToOwner("user-456");
      });

      await waitFor(() => {
        expect(aclActions.changeUserToOwner).toHaveBeenCalledWith({
          userId: "user-456",
          currentAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
          customPersist: undefined,
        });
      });
    });

    it("should update ACL state after successful change", async () => {
      const updatedAcl = { ...mockAcl, owners: ["user-123", "user-456"] };
      vi.spyOn(aclActions, "changeUserToOwner").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToOwner("user-456");
      });

      await waitFor(() => {
        expect(result.current.acl.owners).toContain("user-456");
      });
    });
  });

  describe("onChangeToEditor", () => {
    it("should prevent current user from demoting themselves", async () => {
      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToEditor("user-123");
      });

      expect(toast.error).toHaveBeenCalledWith("You cannot change your own owner role");
      expect(aclActions.changeUserToEditor).not.toHaveBeenCalled();
    });

    it("should prevent removing the last owner", async () => {
      const aclWithOneOwner: AccessControlList = {
        owners: ["user-999"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: aclWithOneOwner,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToEditor("user-999");
      });

      expect(toast.error).toHaveBeenCalledWith("You cannot remove the last owner");
      expect(aclActions.changeUserToEditor).not.toHaveBeenCalled();
    });

    it("should call changeUserToEditor for valid changes", async () => {
      const updatedAcl = { ...mockAcl, owners: [], editors: ["user-456", "user-789"] };
      vi.spyOn(aclActions, "changeUserToEditor").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToEditor("user-789");
      });

      await waitFor(() => {
        expect(aclActions.changeUserToEditor).toHaveBeenCalled();
      });
    });
  });

  describe("onChangeToViewer", () => {
    it("should prevent current user from demoting themselves", async () => {
      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToViewer("user-123");
      });

      expect(toast.error).toHaveBeenCalledWith("You cannot change your own owner role");
      expect(aclActions.changeUserToViewer).not.toHaveBeenCalled();
    });

    it("should prevent removing the last owner", async () => {
      const aclWithOneOwner: AccessControlList = {
        owners: ["user-999"],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: aclWithOneOwner,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onChangeToViewer("user-999");
      });

      expect(toast.error).toHaveBeenCalledWith("You cannot remove the last owner");
      expect(aclActions.changeUserToViewer).not.toHaveBeenCalled();
    });
  });

  describe("onRemoveMember", () => {
    it("should prevent removing yourself as owner", async () => {
      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onRemoveMember("user-123");
      });

      expect(toast.error).toHaveBeenCalledWith("You cannot remove the last owner");
      expect(aclActions.removeMemberFromAcl).not.toHaveBeenCalled();
    });

    it("should prevent removing the last owner", async () => {
      const aclWithOneOwner: AccessControlList = {
        owners: ["user-999"],
        editors: ["user-123"],
        readers: [],
        isPublic: false,
      };

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: aclWithOneOwner,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onRemoveMember("user-999");
      });

      expect(toast.error).toHaveBeenCalledWith("You cannot remove the last owner");
      expect(aclActions.removeMemberFromAcl).not.toHaveBeenCalled();
    });

    it("should allow removing non-owner members", async () => {
      const updatedAcl = { ...mockAcl, editors: [] };
      vi.spyOn(aclActions, "removeMemberFromAcl").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.onRemoveMember("user-456");
      });

      await waitFor(() => {
        expect(aclActions.removeMemberFromAcl).toHaveBeenCalledWith({
          userId: "user-456",
          currentAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
          customPersist: undefined,
        });
      });
    });
  });

  describe("handleGiveAccess", () => {
    it("should call onChangeToOwner for owner role", async () => {
      const updatedAcl = { ...mockAcl, owners: ["user-123", "new-user"] };
      vi.spyOn(aclActions, "changeUserToOwner").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.handleGiveAccess("new-user", "owner");
      });

      await waitFor(() => {
        expect(aclActions.changeUserToOwner).toHaveBeenCalled();
      });
    });

    it("should call onChangeToEditor for editor role", async () => {
      const updatedAcl = { ...mockAcl, editors: ["user-456", "new-user"] };
      vi.spyOn(aclActions, "changeUserToEditor").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.handleGiveAccess("new-user", "editor");
      });

      await waitFor(() => {
        expect(aclActions.changeUserToEditor).toHaveBeenCalled();
      });
    });

    it("should call onChangeToViewer for viewer role", async () => {
      const updatedAcl = { ...mockAcl, readers: ["user-789", "new-user"] };
      vi.spyOn(aclActions, "changeUserToViewer").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      await act(async () => {
        await result.current.handleGiveAccess("new-user", "viewer");
      });

      await waitFor(() => {
        expect(aclActions.changeUserToViewer).toHaveBeenCalled();
      });
    });
  });

  describe("updateAcl", () => {
    it("should update ACL state", async () => {
      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
        }),
      );

      const newAcl: AccessControlList = {
        owners: ["new-owner"],
        editors: [],
        readers: [],
        isPublic: true,
      };

      act(() => {
        result.current.updateAcl(newAcl);
      });

      await waitFor(() => {
        expect(result.current.acl).toEqual(newAcl);
      });
    });

    it("should sync with external ACL changes", async () => {
      const { result, rerender } = renderHook(
        ({ acl }) =>
          useAclManagement({
            initialAcl: acl,
            itemId: "item-123",
            session: mockSession,
          }),
        {
          initialProps: { acl: mockAcl },
        },
      );

      await waitFor(() => {
        expect(result.current.acl).toEqual(mockAcl);
      });

      // Simulate external ACL update (e.g., from parent component)
      const updatedExternalAcl: AccessControlList = {
        ...mockAcl,
        isPublic: true,
      };

      rerender({ acl: updatedExternalAcl });

      await waitFor(() => {
        expect(result.current.acl.isPublic).toBe(true);
      });
    });
  });

  describe("custom persist function", () => {
    it("should pass custom persist function to action calls", async () => {
      const customPersist = vi.fn().mockResolvedValue(mockAcl);
      const updatedAcl = { ...mockAcl, owners: ["user-123", "user-456"] };
      vi.spyOn(aclActions, "changeUserToOwner").mockResolvedValue(updatedAcl);

      const { result } = renderHook(() =>
        useAclManagement({
          initialAcl: mockAcl,
          itemId: "item-123",
          session: mockSession,
          customPersist,
        }),
      );

      await act(async () => {
        await result.current.onChangeToOwner("user-456");
      });

      await waitFor(() => {
        expect(aclActions.changeUserToOwner).toHaveBeenCalledWith(
          expect.objectContaining({
            customPersist,
          }),
        );
      });
    });
  });
});
