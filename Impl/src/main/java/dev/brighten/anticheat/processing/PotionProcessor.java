package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityEffectPacket;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class PotionProcessor {
    private final ObjectData data;

    public List<PotionEffect> potionEffects = new CopyOnWriteArrayList<>();

    public void onFlying(WrappedInFlyingPacket packet) {
        for (PotionEffect effect : potionEffects) {
            if(packet.getPlayer().hasPotionEffect(effect.getType())) continue;

            data.runKeepaliveAction(d -> data.potionProcessor.potionEffects.remove(effect));
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
        for (PotionEffect potionEffect : potionEffects) {
            if(potionEffect.getType().equals(type))
                return true;
        }
        return false;
    }

    public Optional<PotionEffect> getEffectByType(PotionEffectType type) {
        for (PotionEffect potionEffect : potionEffects) {
            if(potionEffect.getType().equals(type))
                return Optional.of(potionEffect);
        }
        return Optional.empty();
    }
}
