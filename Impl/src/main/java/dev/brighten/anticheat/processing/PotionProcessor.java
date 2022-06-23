package dev.brighten.anticheat.processing;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityEffectPacket;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
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

    public void onFlying(WrapperPlayClientPlayerFlying packet) {
        for (PotionEffect effect : potionEffects) {
            if(data.getPlayer().hasPotionEffect(effect.getType())) continue;

            data.runKeepaliveAction(d -> data.potionProcessor.potionEffects.remove(effect));
        }
    }

    public void onPotionEffect(WrapperPlayServerEntityEffect packet) {
        data.runKeepaliveAction(d -> {
            val type = PotionEffectType.getById(packet.getEntityId());
            data.potionProcessor.potionEffects.stream().filter(pe -> pe.getType().equals(type))
                    .forEach(data.potionProcessor.potionEffects::remove);
            data.potionProcessor.potionEffects
                    .add(new PotionEffect(type, packet.getEffectDurationTicks(), packet.getEffectAmplifier(),
                            (packet.isAmbient())));
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
