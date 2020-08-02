package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityEffectPacket;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PotionProcessor {
    private final ObjectData data;

    public List<PotionEffect> potionEffects = new ArrayList<>();

    public void onFlying(WrappedInFlyingPacket packet) {
        for (PotionEffect effect : potionEffects) {
            if(packet.getPlayer().hasPotionEffect(effect.getType())) continue;

            data.setKeepAliveStamp(d -> d.potionProcessor.potionEffects.remove(effect));
        }
    }

    public void onPotionEffect(WrappedOutEntityEffectPacket packet) {
        data.setKeepAliveStamp(d -> d.potionProcessor.potionEffects
                .add(new PotionEffect(PotionEffectType.getById(packet.effectId),
                        packet.duration, packet.amplifier,
                        (packet.flags & 1) == 1, (packet.flags & 2) == 2)));
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.stream().anyMatch(effect -> effect.getType() == type);
    }
}
