import React from "react"

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { createCard } from "@/lib/api";
import { Plus, Check } from "lucide-react";

interface CreateCardFormProps {
    onCreated: () => void;
}

export function CreateCardForm({ onCreated }: CreateCardFormProps) {
    const [front, setFront] = useState("");
    const [back, setBack] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        if (!front.trim() || !back.trim()) return;

        setIsSubmitting(true);
        try {
            await createCard(front.trim(), back.trim());
            setFront("");
            setBack("");
            setSuccess(true);
            setTimeout(() => setSuccess(false), 2000);
            onCreated();
        } catch (err) {
            console.error("Failed to create card:", err);
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base font-sans">
                    <Plus className="h-4 w-4 text-primary" />
                    New Card
                </CardTitle>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <div className="flex flex-col gap-2">
                        <Label htmlFor="front" className="text-sm font-sans">
                            Front (Question)
                        </Label>
                        <Input
                            id="front"
                            placeholder="e.g. What is spaced repetition?"
                            value={front}
                            onChange={(e) => setFront(e.target.value)}
                        />
                    </div>
                    <div className="flex flex-col gap-2">
                        <Label htmlFor="back" className="text-sm font-sans">
                            Back (Answer)
                        </Label>
                        <Textarea
                            id="back"
                            placeholder="e.g. A learning technique that reviews material at increasing intervals."
                            value={back}
                            onChange={(e) => setBack(e.target.value)}
                            rows={3}
                        />
                    </div>
                    <Button type="submit" disabled={isSubmitting || !front.trim() || !back.trim()}>
                        {success ? (
                            <>
                                <Check className="mr-2 h-4 w-4" />
                                Card Created
                            </>
                        ) : (
                            <>
                                <Plus className="mr-2 h-4 w-4" />
                                {isSubmitting ? "Creating..." : "Add Card"}
                            </>
                        )}
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
}
