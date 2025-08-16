package com.faskin.auth.premium;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PremiumEvaluatedEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final PremiumEvaluation evaluation;

    public PremiumEvaluatedEvent(Player who, PremiumEvaluation evaluation) {
        super(who);
        this.evaluation = evaluation;
    }

    public PremiumEvaluation getEvaluation() {
        return evaluation;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

