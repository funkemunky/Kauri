package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityEffectPacket;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class PotionProcessor {
    private final ObjectData data;

    public List<PotionEffect> potionEffects = new CopyOnWriteArrayList<>();

    public void onFlying(WrappedInFlyingPacket packet) {
        for (PotionEffect effect : potionEffects) {
            if(packet.getPlayer().hasPotionEffect(effect.getType())) continue;

            data.setKeepAliveStamp(d -> {
                d.potionProcessor.potionEffects.remove(effect);
                Bukkit.broadcastMessage("effect remove");
            });
        }
    }

    public void onPotionEffect(WrappedOutEntityEffectPacket packet) {
        data.setKeepAliveStamp(d -> {
            val type = PotionEffectType.getById(packet.effectId);
            d.potionProcessor.potionEffects.stream().filter(pe -> pe.getType().equals(type))
                    .forEach(d.potionProcessor.potionEffects::remove);
            d.potionProcessor.potionEffects
                    .add(new PotionEffect(type, packet.duration, packet.amplifier,
                            (packet.flags & 1) == 1, (packet.flags & 2) == 2));

            Bukkit.broadcastMessage("effect add");
        });
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.stream().anyMatch(effect -> effect.getType().equals(type));
    }

    public PotionEffect getEffectByType(PotionEffectType type) {
        return potionEffects.stream().filter(effect -> effect.getType().equals(type)).findFirst().orElse(null);
    }
}
