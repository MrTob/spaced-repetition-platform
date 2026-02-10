"use client"

import { useState } from "react"
import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import type { Card as FlashCard } from "@/lib/api"
import { reviewCard } from "@/lib/api"
import { RotateCcw } from "lucide-react"

const RATINGS = [
    { quality: 0, label: "Again", className: "bg-destructive text-destructive-foreground hover:bg-destructive/90" },
    { quality: 2, label: "Hard", className: "bg-secondary text-secondary-foreground hover:bg-secondary/80" },
    { quality: 3, label: "Good", className: "bg-primary text-primary-foreground hover:bg-primary/90" },
    { quality: 5, label: "Easy", className: "bg-primary/70 text-primary-foreground hover:bg-primary/60" },
]

interface ReviewCardProps {
    card: FlashCard
    onReviewed: () => void
}

export function ReviewCard({ card, onReviewed }: ReviewCardProps) {
    const [flipped, setFlipped] = useState(false)
    const [isSubmitting, setIsSubmitting] = useState(false)

    async function handleRate(quality: number) {
        setIsSubmitting(true)
        try {
            await reviewCard(card.id, quality)
            setFlipped(false)
            onReviewed()
        } catch (err) {
            console.error("Failed to review card:", err)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="flex flex-col items-center gap-6">

            {/* 3D perspective wrapper */}
            <div
                className="w-full max-w-lg perspective-distant"
                onClick={() => setFlipped((f) => !f)}
            >
                <motion.div
                    animate={{ rotateY: flipped ? 180 : 0 }}
                    transition={{ duration: 0.6, ease: "easeInOut" }}
                    className="relative min-h-60 w-full"
                    style={{ transformStyle: "preserve-3d" }}
                >
                    {/* FRONT */}
                    <Card className="absolute inset-0 flex items-center justify-center backface-hidden bg-gray-100">
                        <CardContent className="p-8 text-center">
                            <p className="text-lg font-sans leading-relaxed">
                                {card.front}
                            </p>
                            <span className="text-xs text-muted-foreground">
                Click to reveal answer
              </span>
                        </CardContent>
                    </Card>

                    {/* BACK */}
                    <Card
                        className="absolute inset-0 flex items-center justify-center backface-hidden bg-gray-100"
                        style={{ transform: "rotateY(180deg)" }}
                    >
                        <CardContent className="p-8 text-center">
                            <p className="text-lg font-sans leading-relaxed">
                                {card.back}
                            </p>
                            <span className="text-xs text-muted-foreground">
                Answer
              </span>
                        </CardContent>
                    </Card>
                </motion.div>
            </div>

            {/* Rating buttons (only when flipped) */}
            {flipped && (
                <div className="flex flex-wrap items-center justify-center gap-3">
                    {RATINGS.map((r) => (
                        <Button
                            key={r.quality}
                            disabled={isSubmitting}
                            className={r.className}
                            onClick={() => handleRate(r.quality)}
                        >
                            {r.label}
                        </Button>
                    ))}
                </div>
            )}

            {/* Flip button */}
            {!flipped && (
                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setFlipped(true)}
                    className="gap-2"
                >
                    <RotateCcw className="h-4 w-4" />
                    Flip Card
                </Button>
            )}
        </div>
    )
}
