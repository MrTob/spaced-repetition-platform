const API_BASE = "/api";

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export interface CardListParams {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: "asc" | "desc";
    search?: string;
}

export interface Card {
    id: string;
    front: string;
    back: string;
    nextReview: string;
    createdAt: string;
}

export async function fetchCards(params: CardListParams = {}): Promise<PageResponse<Card>> {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set("page", String(params.page));
    if (params.size !== undefined) query.set("size", String(params.size));
    if (params.sortBy) query.set("sortBy", params.sortBy);
    if (params.sortDir) query.set("sortDir", params.sortDir);
    if (params.search) query.set("search", params.search);

    const res = await fetch(`${API_BASE}/cards?${query}`);
    if (!res.ok) throw new Error("Failed to fetch cards");
    return res.json();
}

export async function updateCard(id: string, front: string, back: string): Promise<Card> {
    const res = await fetch(`${API_BASE}/cards/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ front, back }),
    });
    if (!res.ok) throw new Error("Failed to update card");
    return res.json();
}

export async function deleteCard(id: string): Promise<void> {
    const res = await fetch(`${API_BASE}/cards/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error("Failed to delete card");
}

export async function fetchDueCards(): Promise<Card[]> {
    const res = await fetch(`${API_BASE}/cards/due`);
    if (!res.ok) throw new Error("Failed to fetch due cards");
    return res.json();
}

export async function createCard(front: string, back: string): Promise<Card> {
    const res = await fetch(`${API_BASE}/cards`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ front, back }),
    });
    if (!res.ok) throw new Error("Failed to create card");
    return res.json();
}

export async function reviewCard(
    id: string,
    quality: number,
): Promise<Card> {
    const res = await fetch(
        `${API_BASE}/cards/${id}/review?quality=${quality}`,
        { method: "POST" },
    );
    if (!res.ok) throw new Error("Failed to review card");
    return res.json();
}
