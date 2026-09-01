import { describe, it, expect, vi, beforeEach } from "vitest";
import { Session } from "next-auth";
import {
  persistAcl,
  persistTestRunExecutionAcl,
  changeUserToOwner,
  changeUserToEditor,
  changeUserToViewer,
  removeMemberFromAcl,
  generateShareableAccessKey,
  deleteShareableAccessKey,
  updatePrivacyPolicy,
} from "../aclActions";
import { updateConnectionAndAllReferencedMessageAcl } from "@message-capture/services/updateAclOfMessageOfAConnection";
import { generateReadAccessKey } from "@message-capture/services/generateReadAccessKey";
import { deleteAccessKey } from "@message-capture/services/deleteAccessKey";
import { updateTestRunExecutionAcl } from "@/app/test-execution/components/test-run/actions";
import { toast } from "react-toastify";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";

// Mock dependencies
vi.mock("@message-capture/services/updateAclOfMessageOfAConnection", () => ({
  updateConnectionAndAllReferencedMessageAcl: vi.fn(),
}));

vi.mock("@message-capture/services/generateReadAccessKey", () => ({
  generateReadAccessKey: vi.fn(),
}));

vi.mock("@message-capture/services/deleteAccessKey", () => ({
  deleteAccessKey: vi.fn(),
}));

vi.mock("@/app/test-execution/components/test-run/actions", () => ({
  updateTestRunExecutionAcl: vi.fn(),
}));

vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe("aclActions", () => {
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

  describe("persistAcl", () => {
    it("should persist ACL successfully", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await persistAcl({
        itemId: "item-123",
        accessControlList: mockAcl,
        session: mockSession,
      });

      expect(result).toEqual(mockAcl);
      expect(updateConnectionAndAllReferencedMessageAcl).toHaveBeenCalledWith({
        itemId: "item-123",
        accessControlList: mockAcl,
        session: mockSession,
      });
    });

    it("should throw error on failure", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockRejectedValue(new Error("API Error"));

      await expect(
        persistAcl({
          itemId: "item-123",
          accessControlList: mockAcl,
          session: mockSession,
        }),
      ).rejects.toThrow("API Error");
    });
  });

  describe("persistTestRunExecutionAcl", () => {
    it("should persist test run execution ACL successfully", async () => {
      vi.mocked(updateTestRunExecutionAcl).mockResolvedValue({
        success: true,
        data: { accessControlList: mockAcl },
      });

      const result = await persistTestRunExecutionAcl({
        itemId: "exec-123",
        accessControlList: mockAcl,
        session: mockSession,
      });

      expect(result).toEqual(mockAcl);
      expect(updateTestRunExecutionAcl).toHaveBeenCalledWith({
        executionId: "exec-123",
        accessControlList: mockAcl,
      });
    });

    it("should throw error when update fails", async () => {
      vi.mocked(updateTestRunExecutionAcl).mockResolvedValue({
        success: false,
        error: "Update failed",
      });

      await expect(
        persistTestRunExecutionAcl({
          itemId: "exec-123",
          accessControlList: mockAcl,
          session: mockSession,
        }),
      ).rejects.toThrow("Update failed");
    });

    it("should fallback to provided ACL if response missing accessControlList", async () => {
      vi.mocked(updateTestRunExecutionAcl).mockResolvedValue({
        success: true,
        data: {},
      });

      const result = await persistTestRunExecutionAcl({
        itemId: "exec-123",
        accessControlList: mockAcl,
        session: mockSession,
      });

      expect(result).toEqual(mockAcl);
    });
  });

  describe("changeUserToOwner", () => {
    it("should add user to owners and remove from other roles", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await changeUserToOwner({
        userId: "user-456",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.owners).toContain("user-123");
      expect(result.owners).toContain("user-456");
      expect(result.editors).not.toContain("user-456");
      expect(result.readers).toEqual(["user-789"]);
    });

    it("should not duplicate user in owners", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await changeUserToOwner({
        userId: "user-123",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.owners).toEqual(["user-123"]);
      expect(result.owners).toHaveLength(1);
    });

    it("should use custom persist function when provided", async () => {
      const customPersist = vi.fn().mockResolvedValue(mockAcl);

      await changeUserToOwner({
        userId: "user-456",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
        customPersist,
      });

      expect(customPersist).toHaveBeenCalled();
      expect(updateConnectionAndAllReferencedMessageAcl).not.toHaveBeenCalled();
    });
  });

  describe("changeUserToEditor", () => {
    it("should add user to editors and remove from other roles", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await changeUserToEditor({
        userId: "user-123",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.owners).not.toContain("user-123");
      expect(result.editors).toContain("user-123");
      expect(result.editors).toContain("user-456");
      expect(result.readers).toEqual(["user-789"]);
    });

    it("should not duplicate user in editors", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await changeUserToEditor({
        userId: "user-456",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.editors).toEqual(["user-456"]);
      expect(result.editors).toHaveLength(1);
    });
  });

  describe("changeUserToViewer", () => {
    it("should add user to viewers and remove from other roles", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await changeUserToViewer({
        userId: "user-123",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.owners).not.toContain("user-123");
      expect(result.editors).toEqual(["user-456"]);
      expect(result.readers).toContain("user-123");
      expect(result.readers).toContain("user-789");
    });

    it("should not duplicate user in readers", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await changeUserToViewer({
        userId: "user-789",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.readers).toEqual(["user-789"]);
      expect(result.readers).toHaveLength(1);
    });
  });

  describe("removeMemberFromAcl", () => {
    it("should remove user from all roles", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await removeMemberFromAcl({
        userId: "user-456",
        currentAcl: mockAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.owners).toEqual(["user-123"]);
      expect(result.editors).toEqual([]);
      expect(result.readers).toEqual(["user-789"]);
    });

    it("should handle empty arrays", async () => {
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const emptyAcl: AccessControlList = {
        owners: [],
        editors: [],
        readers: [],
        isPublic: false,
      };

      const result = await removeMemberFromAcl({
        userId: "user-123",
        currentAcl: emptyAcl,
        itemId: "item-123",
        session: mockSession,
      });

      expect(result.owners).toEqual([]);
      expect(result.editors).toEqual([]);
      expect(result.readers).toEqual([]);
    });
  });

  describe("generateShareableAccessKey", () => {
    it("should generate access key and update ACL", async () => {
      const aclWithKey: AccessControlList = {
        ...mockAcl,
        readAccessKey: "generated-key-123",
      };

      vi.mocked(generateReadAccessKey).mockResolvedValue({
        accessControlList: aclWithKey,
      });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await generateShareableAccessKey({
        itemId: "item-123",
        currentAcl: mockAcl,
        session: mockSession,
      });

      expect(result.accessKey).toBe("generated-key-123");
      expect(result.acl.isPublic).toBe(false);
      expect(result.acl.readAccessKey).toBe("generated-key-123");
    });

    it("should throw error if access key generation fails", async () => {
      vi.mocked(generateReadAccessKey).mockResolvedValue({
        error: "Failed to generate",
      });

      await expect(
        generateShareableAccessKey({
          itemId: "item-123",
          currentAcl: mockAcl,
          session: mockSession,
        }),
      ).rejects.toThrow("Failed to generate access key");
    });
  });

  describe("deleteShareableAccessKey", () => {
    it("should delete access key and set policy to public", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ accessControlList: mockAcl });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await deleteShareableAccessKey({
        itemId: "item-123",
        currentAcl: { ...mockAcl, readAccessKey: "old-key" },
        policy: "public",
        session: mockSession,
      });

      expect(result.isPublic).toBe(true);
      expect(result.readAccessKey).toBeUndefined();
      expect(deleteAccessKey).toHaveBeenCalledWith({ id: "item-123", session: mockSession });
    });

    it("should add 'user' to readers when policy is 'users'", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ accessControlList: mockAcl });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await deleteShareableAccessKey({
        itemId: "item-123",
        currentAcl: { ...mockAcl, readAccessKey: "old-key" },
        policy: "users",
        session: mockSession,
      });

      expect(result.isPublic).toBe(false);
      expect(result.readers).toContain("user");
    });

    it("should NOT call deleteAccessKey when no readAccessKey exists", async () => {
      vi.mocked(deleteAccessKey).mockClear();
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await deleteShareableAccessKey({
        itemId: "item-123",
        currentAcl: mockAcl, // No readAccessKey
        policy: "public",
        session: mockSession,
      });

      expect(result.isPublic).toBe(true);
      expect(result.readAccessKey).toBeUndefined();
      expect(deleteAccessKey).not.toHaveBeenCalled();
    });

    it("should remove 'user' from readers when policy is 'private'", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ accessControlList: mockAcl });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const aclWithUser: AccessControlList = {
        ...mockAcl,
        readers: ["user-789", "user"],
      };

      const result = await deleteShareableAccessKey({
        itemId: "item-123",
        currentAcl: aclWithUser,
        policy: "private",
        session: mockSession,
      });

      expect(result.readers).not.toContain("user");
      expect(result.readers).toContain("user-789");
    });

    it("should throw error when deleteAccessKey fails", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ error: "Unable to delete access key" });

      await expect(
        deleteShareableAccessKey({
          itemId: "item-123",
          currentAcl: { ...mockAcl, readAccessKey: "existing-key" },
          policy: "public",
          session: mockSession,
        }),
      ).rejects.toThrow("Unable to delete access key");
    });
  });

  describe("updatePrivacyPolicy", () => {
    const mockT = vi.fn((key: string) => key);

    it("should generate access key when policy is 'link'", async () => {
      const aclWithKey: AccessControlList = {
        ...mockAcl,
        readAccessKey: "new-key",
      };

      vi.mocked(generateReadAccessKey).mockResolvedValue({
        accessControlList: aclWithKey,
      });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await updatePrivacyPolicy({
        policy: "link",
        itemId: "item-123",
        currentAcl: mockAcl,
        session: mockSession,
        t: mockT,
      });

      expect(result.readAccessKey).toBe("new-key");
      expect(result.isPublic).toBe(false);
      expect(toast.success).toHaveBeenCalled();
    });

    it("should set public when policy is 'public'", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ accessControlList: mockAcl });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await updatePrivacyPolicy({
        policy: "public",
        itemId: "item-123",
        currentAcl: mockAcl,
        session: mockSession,
        t: mockT,
      });

      expect(result.isPublic).toBe(true);
      expect(toast.success).toHaveBeenCalled();
    });

    it("should add 'user' to readers when policy is 'users'", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ accessControlList: mockAcl });
      vi.mocked(updateConnectionAndAllReferencedMessageAcl).mockResolvedValue(undefined);

      const result = await updatePrivacyPolicy({
        policy: "users",
        itemId: "item-123",
        currentAcl: mockAcl,
        session: mockSession,
        t: mockT,
      });

      expect(result.readers).toContain("user");
      expect(result.isPublic).toBe(false);
      expect(toast.success).toHaveBeenCalled();
    });

    it("should show error toast on failure", async () => {
      vi.mocked(deleteAccessKey).mockResolvedValue({ error: "Delete failed" });

      await expect(
        updatePrivacyPolicy({
          policy: "public",
          itemId: "item-123",
          currentAcl: { ...mockAcl, readAccessKey: "existing-key" },
          session: mockSession,
          t: mockT,
        }),
      ).rejects.toThrow("Delete failed");

      expect(toast.error).toHaveBeenCalled();
    });

    it("should use custom persist function when provided", async () => {
      const customPersist = vi.fn().mockResolvedValue(mockAcl);
      const aclWithKey: AccessControlList = {
        ...mockAcl,
        readAccessKey: "new-key",
      };

      vi.mocked(generateReadAccessKey).mockResolvedValue({
        accessControlList: aclWithKey,
      });

      await updatePrivacyPolicy({
        policy: "link",
        itemId: "item-123",
        currentAcl: mockAcl,
        session: mockSession,
        t: mockT,
        customPersist,
      });

      expect(customPersist).toHaveBeenCalled();
      expect(updateConnectionAndAllReferencedMessageAcl).not.toHaveBeenCalled();
    });
  });
});
