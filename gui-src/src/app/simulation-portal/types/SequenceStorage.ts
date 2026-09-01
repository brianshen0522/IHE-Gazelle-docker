const STORAGE_PREFIX = "simulation.formData.";
const TTL_DAYS = 30;

type SequenceStorage = {
    expiresAt: number;
    params: Record<string, string>;
};

function getKey(sequenceId: string) {
    return STORAGE_PREFIX + sequenceId;
}

function loadSequenceStorage(sequenceId: string): SequenceStorage | null {
    const raw = localStorage.getItem(getKey(sequenceId));
    if (!raw) return null;

    try {
        const parsed: SequenceStorage = JSON.parse(raw);

        // expired → delete
        if (Date.now() > parsed.expiresAt) {
            localStorage.removeItem(getKey(sequenceId));
            return null;
        }

        return parsed;
    } catch {
        localStorage.removeItem(getKey(sequenceId));
        return null;
    }
}

export function saveParam(sequenceId: string, name: string, value: string) {
    const existing = loadSequenceStorage(sequenceId);

    const next: SequenceStorage = {
        expiresAt: Date.now() + TTL_DAYS * 24 * 60 * 60 * 1000,
        params: {
            ...existing?.params,
            [name]: value
        }
    };

    localStorage.setItem(getKey(sequenceId), JSON.stringify(next));
}

export function removeParam(sequenceId: string, name: string) {
    const existing = loadSequenceStorage(sequenceId);
    if (!existing) return;

    delete existing.params[name];

    if (Object.keys(existing.params).length === 0) {
        localStorage.removeItem(getKey(sequenceId));
        return;
    }

    existing.expiresAt = Date.now() + TTL_DAYS * 24 * 60 * 60 * 1000;

    localStorage.setItem(getKey(sequenceId), JSON.stringify(existing));
}

export function readParam(sequenceId: string, name: string): string | null {
    return loadSequenceStorage(sequenceId)?.params?.[name] ?? null;
}