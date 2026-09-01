// Utility functions for handling file operations in the browser
import { base64Encode } from "../fileInspection/base64";

export interface FileWithContent {
  file: File;
  content: string;
}

export async function readFileAsText(file: File): Promise<string> {
  return await file.text();
}

export async function readFileWithContent(file: File): Promise<FileWithContent> {
  if (typeof file.arrayBuffer === "function") {
    const arrayBuffer = await file.arrayBuffer();
    const bytes = new Uint8Array(arrayBuffer);
    let binary = "";
    const chunkSize = 0x8000;
    for (let i = 0; i < bytes.length; i += chunkSize) {
      binary += String.fromCodePoint(...bytes.subarray(i, i + chunkSize));
    }
    const content = btoa(binary);
    return { file, content };
  } else {
    const text = await readFileAsText(file);
    const content = base64Encode(text);
    return { file, content };
  }
}

// Creates a Map from an existing Map, useful for immutable updates
export function cloneMap<K, V>(original: Map<K, V>): Map<K, V> {
  return new Map(original);
}

// Creates a Set from an existing Set, useful for immutable updates
export function cloneSet<T>(original: Set<T>): Set<T> {
  return new Set(original);
}

export function addToSet<T>(set: Set<T>, item: T): Set<T> {
  const newSet = cloneSet(set);
  newSet.add(item);
  return newSet;
}

export function removeFromSet<T>(set: Set<T>, item: T): Set<T> {
  const newSet = cloneSet(set);
  newSet.delete(item);
  return newSet;
}

export function handleFileError(error: unknown, context?: string): void {
  const message = context ? `${context}: ` : "";
  console.error(`${message}Error reading file:`, error);
}
