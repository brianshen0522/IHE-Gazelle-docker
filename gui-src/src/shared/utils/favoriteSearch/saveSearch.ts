import { toast } from "react-toastify";

interface SaveSearchParams {
  searchName: string;
  url: string;
  filters: Record<string, unknown>;
  storageKey: string;
  successMessage?: string;
  errorMessage?: string;
}

interface SavedSearch {
  name: string;
  url: string;
  filters: Record<string, unknown>;
}

// Checks if localStorage is available and accessible
const isLocalStorageAvailable = (): boolean => {
  try {
    const testKey = "__storage_test__";
    localStorage.setItem(testKey, "test");
    localStorage.removeItem(testKey);
    return true;
  } catch {
    return false;
  }
};

export const saveSearch = ({ searchName, url, filters, storageKey, successMessage, errorMessage }: SaveSearchParams): boolean => {
  try {
    // Validate inputs
    if (!searchName?.trim()) {
      toast.error(errorMessage || "Search name cannot be empty");
      return false;
    }

    if (!url?.trim()) {
      toast.error(errorMessage || "URL cannot be empty");
      return false;
    }

    if (!storageKey?.trim()) {
      toast.error(errorMessage || "Invalid storage configuration");
      return false;
    }

    if (!isLocalStorageAvailable()) {
      toast.error(errorMessage || "Unable to access local storage");
      return false;
    }

    let savedSearches: SavedSearch[] = [];
    const storedData = localStorage.getItem(storageKey);

    if (storedData) {
      try {
        savedSearches = JSON.parse(storedData);
        if (!Array.isArray(savedSearches)) {
          savedSearches = [];
        }
      } catch {
        // If stored data is corrupted, start fresh
        savedSearches = [];
      }
    }

    const isDuplicate = savedSearches.some((search) => search.name.toLowerCase() === searchName.trim().toLowerCase());

    if (isDuplicate) {
      toast.error(errorMessage || "A search with this name already exists");
      return false;
    }

    const newSearch: SavedSearch = {
      name: searchName.trim(),
      url: url.trim(),
      filters,
    };

    savedSearches.push(newSearch);

    localStorage.setItem(storageKey, JSON.stringify(savedSearches));

    // Notify other tabs/windows
    globalThis.dispatchEvent(new Event("storage"));

    toast.success(successMessage || "Search saved successfully");
    return true;
  } catch (error) {
    // Handle quota exceeded or other localStorage errors
    console.error("Failed to save search:", error);
    toast.error(errorMessage || "Failed to save search");
    return false;
  }
};
