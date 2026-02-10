import {useState, useEffect, useCallback} from "react";
import {fetchCards, updateCard, deleteCard} from "@/lib/api";
import type {Card as FlashCard} from "@/lib/api";
import {
    Table,
    TableHeader,
    TableBody,
    TableHead,
    TableRow,
    TableCell,
} from "@/components/ui/table";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Skeleton} from "@/components/ui/skeleton";
import {Pencil, Trash2, Check, X, Inbox, ArrowUpDown, ArrowUp, ArrowDown} from "lucide-react";

interface CardTableProps {
    refreshKey: number;
}


type SortDirection = "asc" | "desc";

interface SortConfig {
    key: string;
    direction: SortDirection;
}

export function CardTable({refreshKey}: CardTableProps) {
    const [cards, setCards] = useState<FlashCard[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [sortConfig, setSortConfig] = useState<SortConfig>({
        key: "nextReview",
        direction: "asc"
    });

    const [editingId, setEditingId] = useState<string | null>(null);
    const [editFront, setEditFront] = useState("");
    const [editBack, setEditBack] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    const loadCards = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await fetchCards({
                sortBy: sortConfig.key,
                sortDir: sortConfig.direction,
            });
            setCards(data.content);
            setError(null);
        } catch {
            setError("Could not load cards.");
        } finally {
            setIsLoading(false);
        }
    }, [sortConfig]);

    useEffect(() => {
        loadCards();
    }, [loadCards, refreshKey]);

    function handleSort(key: string) {
        setSortConfig((current) => ({
            key,
            direction:
                current.key === key && current.direction === "asc"
                    ? "desc"
                    : "asc",
        }));
    }

    function SortIcon({columnKey}: { columnKey: string }) {
        if (sortConfig.key !== columnKey) {
            return <ArrowUpDown className="ml-2 h-4 w-4 text-muted-foreground/30"/>;
        }
        return sortConfig.direction === "asc" ? (
            <ArrowUp className="ml-2 h-4 w-4 text-primary"/>
        ) : (
            <ArrowDown className="ml-2 h-4 w-4 text-primary"/>
        );
    }

    function startEdit(card: FlashCard) {
        setEditingId(card.id);
        setEditFront(card.front);
        setEditBack(card.back);
    }

    function cancelEdit() {
        setEditingId(null);
        setEditFront("");
        setEditBack("");
    }

    async function saveEdit(id: string) {
        if (!editFront.trim() || !editBack.trim()) return;
        setIsSaving(true);
        try {
            await updateCard(id, editFront.trim(), editBack.trim());
            setEditingId(null);
            loadCards();
        } catch (err) {
            console.error("Failed to update card:", err);
        } finally {
            setIsSaving(false);
        }
    }

    async function handleDelete(id: string) {
        try {
            await deleteCard(id);
            loadCards();
        } catch (err) {
            console.error("Failed to delete card:", err);
        }
    }

    if (isLoading && cards.length === 0) {
        return (
            <Card>
                <CardContent className="p-6">
                    <div className="flex flex-col gap-3">
                        <Skeleton className="h-8 w-full"/>
                        <Skeleton className="h-8 w-full"/>
                        <Skeleton className="h-8 w-full"/>
                    </div>
                </CardContent>
            </Card>
        );
    }

    if (error) {
        return (
            <Card className="border-destructive/30">
                <CardContent className="flex flex-col items-center gap-3 p-8 text-center">
                    <p className="text-sm text-destructive font-sans">{error}</p>
                    <p className="text-xs text-muted-foreground font-sans">
                        Make sure your backend is running at localhost:8080
                    </p>
                </CardContent>
            </Card>
        );
    }

    if (cards.length === 0 && !isLoading) {
        return (
            <Card>
                <CardContent className="flex flex-col items-center gap-3 p-8 text-center">
                    <Inbox className="h-10 w-10 text-muted-foreground/50"/>
                    <p className="text-sm font-medium text-card-foreground font-sans">
                        No cards yet
                    </p>
                    <p className="text-xs text-muted-foreground font-sans">
                        Create some cards in the Review tab to get started.
                    </p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardContent className="p-0">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[30%]">
                                <Button
                                    variant="ghost"
                                    className="-ml-3 h-8 text-sm font-medium hover:text-primary"
                                    onClick={() => handleSort("front")}
                                >
                                    Front
                                    <SortIcon columnKey="front"/>
                                </Button>
                            </TableHead>
                            <TableHead className="w-[30%]">
                                <Button
                                    variant="ghost"
                                    className="-ml-3 h-8 text-sm font-medium hover:text-primary"
                                    onClick={() => handleSort("back")}
                                >
                                    Back
                                    <SortIcon columnKey="back"/>
                                </Button>
                            </TableHead>
                            <TableHead className="w-[20%]">
                                <Button
                                    variant="ghost"
                                    className="-ml-3 h-8 text-sm font-medium hover:text-primary"
                                    onClick={() => handleSort("nextReview")}
                                >
                                    Next Review
                                    <SortIcon columnKey="nextReview"/>
                                </Button>
                            </TableHead>
                            <TableHead className="w-[20%] text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {cards.map((card) => (
                            <TableRow key={card.id}>
                                {editingId === card.id ? (
                                    <>
                                        <TableCell>
                                            <Input
                                                value={editFront}
                                                onChange={(e) => setEditFront(e.target.value)}
                                                className="h-8 text-sm"
                                                autoFocus
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <Input
                                                value={editBack}
                                                onChange={(e) => setEditBack(e.target.value)}
                                                className="h-8 text-sm"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            {/* Date is not editable, show original */}
                                            <span
                                                className="text-muted-foreground text-sm opacity-50 cursor-not-allowed">
                                                {new Date(card.nextReview).toLocaleDateString()}
                                            </span>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center justify-end gap-1">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    className="h-8 w-8 text-primary"
                                                    disabled={isSaving || !editFront.trim() || !editBack.trim()}
                                                    onClick={() => saveEdit(card.id)}
                                                >
                                                    <Check className="h-4 w-4"/>
                                                    <span className="sr-only">Save</span>
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    className="h-8 w-8 text-muted-foreground"
                                                    onClick={cancelEdit}
                                                >
                                                    <X className="h-4 w-4"/>
                                                    <span className="sr-only">Cancel</span>
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </>
                                ) : (
                                    <>
                                        <TableCell className="font-sans text-sm text-card-foreground max-w-0 truncate">
                                            {card.front}
                                        </TableCell>
                                        <TableCell className="font-sans text-sm text-muted-foreground max-w-0 truncate">
                                            {card.back}
                                        </TableCell>
                                        <TableCell className="font-sans text-sm text-muted-foreground max-w-0 truncate">
                                            {new Date(card.nextReview).toLocaleString(undefined, {
                                                year: "numeric",
                                                month: "short",
                                                day: "2-digit",
                                                hour: "2-digit",
                                                minute: "2-digit",
                                            })}
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center justify-end gap-1">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    className="h-8 w-8 text-muted-foreground hover:text-card-foreground"
                                                    onClick={() => startEdit(card)}
                                                >
                                                    <Pencil className="h-4 w-4"/>
                                                    <span className="sr-only">Edit</span>
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    className="h-8 w-8 text-muted-foreground hover:text-destructive"
                                                    onClick={() => handleDelete(card.id)}
                                                >
                                                    <Trash2 className="h-4 w-4"/>
                                                    <span className="sr-only">Delete</span>
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </>
                                )}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
}