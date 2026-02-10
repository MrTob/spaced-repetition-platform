import { useState, useEffect, useCallback } from "react";
import { fetchDueCards } from "@/lib/api";
import type { Card as FlashCard } from "@/lib/api";
import { ReviewCard } from "@/components/review-card";
import { CreateCardForm } from "@/components/create-card-form";
import { CardTable } from "@/components/card-table";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { BookOpen, Clock, Layers, Inbox, TableProperties } from "lucide-react";

export function App() {
    const [dueCards, setDueCards] = useState<FlashCard[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [tableRefreshKey, setTableRefreshKey] = useState(0);

    const loadCards = useCallback(async () => {
        try {
            const cards = await fetchDueCards();
            setDueCards(cards);
            setError(null);
        } catch {
            setError("Could not connect to the API.");
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        loadCards();
    }, [loadCards]);

    function handleDataChanged() {
        loadCards();
        setTableRefreshKey((k) => k + 1);
    }

    const dueCount = dueCards.length;

    return (
        <div className="min-h-screen bg-background">
            <header className="border-b border-border bg-card">
                <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
                    <div className="flex items-center gap-3">
                        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary">
                            <Layers className="h-5 w-5 text-primary-foreground" />
                        </div>
                        <div>
                            <h1 className="text-lg font-semibold font-sans text-card-foreground">
                                Flashcard SRS
                            </h1>
                            <p className="text-xs text-muted-foreground font-sans">
                                Spaced Repetition Dashboard
                            </p>
                        </div>
                    </div>
                    <Badge
                        variant={dueCount > 0 ? "default" : "secondary"}
                        className="font-mono text-xs"
                    >
                        {isLoading ? "..." : `${dueCount} due`}
                    </Badge>
                </div>
            </header>

            <main className="mx-auto max-w-5xl px-6 py-8">
                {/* Stats row */}
                <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <Card>
                        <CardContent className="flex items-center gap-4 p-5">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10">
                                <BookOpen className="h-5 w-5 text-primary" />
                            </div>
                            <div>
                                <p className="text-xs text-muted-foreground font-sans">Cards Due</p>
                                <p className="text-2xl font-semibold font-mono text-card-foreground">
                                    {isLoading ? <Skeleton className="h-7 w-10" /> : dueCount}
                                </p>
                            </div>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardContent className="flex items-center gap-4 p-5">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10">
                                <Clock className="h-5 w-5 text-primary" />
                            </div>
                            <div>
                                <p className="text-xs text-muted-foreground font-sans">Status</p>
                                <p className="text-sm font-medium font-sans text-card-foreground">
                                    {isLoading ? (
                                        <Skeleton className="h-5 w-24" />
                                    ) : dueCount > 0 ? (
                                        "Cards to review"
                                    ) : (
                                        "All caught up"
                                    )}
                                </p>
                            </div>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardContent className="flex items-center gap-4 p-5">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10">
                                <Layers className="h-5 w-5 text-primary" />
                            </div>
                            <div className="min-w-0">
                                <p className="text-xs text-muted-foreground font-sans">Next Up</p>
                                <p className="text-sm font-medium font-sans text-card-foreground truncate min-w-0">
                                    {isLoading ? (
                                        <Skeleton className="h-5 w-32" />
                                    ) : dueCards?.[0]?.front ? (
                                        dueCards[0].front
                                    ) : (
                                        "No cards"
                                    )}
                                </p>
                            </div>
                        </CardContent>
                    </Card>
                </div>

                <Tabs defaultValue="review">
                    <TabsList className="mb-6">
                        <TabsTrigger value="review" className="gap-2">
                            <BookOpen className="h-4 w-4" />
                            Review
                        </TabsTrigger>
                        <TabsTrigger value="manage" className="gap-2">
                            <TableProperties className="h-4 w-4" />
                            Manage Cards
                        </TabsTrigger>
                    </TabsList>

                    <TabsContent value="review">
                        <div className="grid grid-cols-1 gap-8 lg:grid-cols-5">
                            {/* Review section */}
                            <div className="lg:col-span-3">
                                <h2 className="mb-4 text-sm font-medium text-muted-foreground font-sans uppercase tracking-wide">
                                    Review
                                </h2>
                                {isLoading ? (
                                    <Card className="flex min-h-[240px] items-center justify-center">
                                        <CardContent className="flex flex-col items-center gap-3 p-8">
                                            <Skeleton className="h-6 w-48" />
                                            <Skeleton className="h-4 w-32" />
                                        </CardContent>
                                    </Card>
                                ) : error ? (
                                    <Card className="flex min-h-[240px] items-center justify-center border-destructive/30">
                                        <CardContent className="flex flex-col items-center gap-3 p-8 text-center">
                                            <p className="text-sm text-destructive font-sans">
                                                Could not connect to the API.
                                            </p>
                                            <p className="text-xs text-muted-foreground font-sans">
                                                Make sure your backend is running at localhost:8080
                                            </p>
                                        </CardContent>
                                    </Card>
                                ) : dueCount === 0 ? (
                                    <Card className="flex min-h-[240px] items-center justify-center">
                                        <CardContent className="flex flex-col items-center gap-3 p-8 text-center">
                                            <Inbox className="h-10 w-10 text-muted-foreground/50" />
                                            <p className="text-sm font-medium text-card-foreground font-sans">
                                                No cards due for review
                                            </p>
                                            <p className="text-xs text-muted-foreground font-sans">
                                                Create new cards or check back later.
                                            </p>
                                        </CardContent>
                                    </Card>
                                ) : (
                                    <ReviewCard card={dueCards[0]} onReviewed={handleDataChanged} />
                                )}
                            </div>

                            {/* Create section */}
                            <div className="lg:col-span-2">
                                <h2 className="mb-4 text-sm font-medium text-muted-foreground font-sans uppercase tracking-wide">
                                    Add Cards
                                </h2>
                                <CreateCardForm onCreated={handleDataChanged} />
                            </div>
                        </div>
                    </TabsContent>

                    <TabsContent value="manage">
                        <h2 className="mb-4 text-sm font-medium text-muted-foreground font-sans uppercase tracking-wide">
                            All Cards
                        </h2>
                        <CardTable refreshKey={tableRefreshKey} />
                    </TabsContent>
                </Tabs>
            </main>
        </div>
    );
}
export default App;