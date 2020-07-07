package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityEffectPacket;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class PotionProcessor {
    private final ObjectData data;

    public List<PotionEffect> potionEffects = new CopyOnWriteArrayList<>();

    public void onFlying(WrappedInFlyingPacket packet) {
        for (PotionEffect effect : potionEffects) {
            if(packet.getPlayer().hasPotionEffect(effect.getType())) continue;

            data.runKeepaliveAction(d -> {
                data.potionProcessor.potionEffects.remove(effect);
            });
        }
    }

    public void onPotionEffect(WrappedOutEntityEffectPacket packet) {
        data.runKeepaliveAction(d -> {
            val type = PotionEffectType.getById(packet.effectId);
            data.potionProcessor.potionEffects.stream().filter(pe -> pe.getType().equals(type))
                    .forEach(data.potionProcessor.potionEffects::remove);
            data.potionProcessor.potionEffects
                    .add(new PotionEffect(type, packet.duration, packet.amplifier,
                            (packet.flags & 1) == 1, (packet.flags & 2) == 2));
        });
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.stream().anyMatch(effect -> effect.getType().equals(type));
    }

    public PotionEffect getEffectByType(PotionEffectType type) {
        return potionEffects.stream().filter(effect -> effect.getType().equals(type)).findFirst().orElse(null);
    }
}
