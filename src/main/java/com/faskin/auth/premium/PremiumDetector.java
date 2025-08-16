package com.faskin.auth.premium;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public interface PremiumDetector {
    PremiumEvaluation evaluatePreLogin(AsyncPlayerPreLoginEvent e);
    PremiumEvaluation evaluateJoin(Player player);
}
