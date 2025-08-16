package com.faskin.auth.premium;

import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public interface PremiumDetector {
    PremiumEvaluation evaluate(AsyncPlayerPreLoginEvent e);
}
