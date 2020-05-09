package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityEffectPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PotionProcessor {
    private final ObjectData data;

    private List<PotionEffectType> potionEffects = new ArrayList<>();
    private Map<Short, PotionEffectType> effectsToAdd = new HashMap<>(), effectsToRemove = new HashMap<>();

    public void onFlying(WrappedInFlyingPacket packet) {
        for (PotionEffectType type : potionEffects) {
            if(packet.getPlayer().hasPotionEffect(type)) continue;

            String transId = "potion-effect-remove-" + type.getId();
            short action = data.setTransactionAction(transId);
            effectsToRemove.put(action, type);
            TinyProtocolHandler.sendPacket(packet.getPlayer(), new WrappedOutTransaction(0, action, false));
        }
    }

    public void onPotionEffect(WrappedOutEntityEffectPacket packet) {
        String transId = "potion-effect-add-" + packet.effectId;
        short action = data.setTransactionAction(transId);
        effectsToAdd.put(action,
                PotionEffectType.getById(packet.effectId));

        TinyProtocolHandler.sendPacket(packet.getPlayer(), new WrappedOutTransaction(0, action, false));
    }

    public void onTransaction(WrappedInTransactionPacket packet) {
        if(effectsToAdd.containsKey(packet.getAction())) {
            potionEffects.add(effectsToAdd.get(packet.getAction()));
            effectsToAdd.remove(packet.getAction());
        } else if(effectsToRemove.containsKey(packet.getAction())) {
            potionEffects.remove(effectsToRemove.get(packet.getAction()));
            effectsToRemove.remove(packet.getAction());
        }
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return potionEffects.contains(type);
    }
}
