package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.*;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import lombok.val;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.TimeUnit;

public class    PacketProcessor {

    public void processClient(ObjectData data, Object object, String type, long timeStamp) {
        Kauri.INSTANCE.profiler.start("packet:client:" + getType(type));
        switch(type) {
            case Packet.Client.ABILITIES: {
                WrappedInAbilitiesPacket packet = new WrappedInAbilitiesPacket(object, data.getPlayer());

                data.playerInfo.flying = packet.isFlying();

                if(data.playerInfo.canFly != packet.isAllowedFlight()) {
                    data.playerInfo.lastToggleFlight.reset();
                }

                data.playerInfo.canFly = packet.isAllowedFlight();
                data.playerInfo.creative = packet.isCreativeMode();
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(object, data.getPlayer());

                if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                    data.playerInfo.lastAttack.reset();
                    data.playerInfo.lastAttackTimeStamp = timeStamp;

                    if(packet.getEntity() instanceof LivingEntity) {
                        if(data.target != null && !data.target.getUniqueId().equals(packet.getEntity().getUniqueId())) {
                            //Resetting location to prevent false positives.
                            data.targetPastLocation.previousLocations.clear();
                            data.playerInfo.lastTargetSwitch.reset();
                            data.targetBounds = new SimpleCollisionBox((Object)MinecraftReflection
                                    .getEntityBoundingBox(data.target));
                        }

                        data.target = (LivingEntity) packet.getEntity();
                    }
                    data.predictionService.useSword = false;
                }
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                WrappedInFlyingPacket packet = new WrappedInFlyingPacket(object, data.getPlayer());

                if(timeStamp - data.lagInfo.lastFlying <= 2) {
                    data.lagInfo.lastPacketDrop.reset();
                }

                if(timeStamp - data.creation > 10000L
                        && timeStamp - data.lagInfo.lastTrans > 5000L)
                    RunUtils.task(() -> data.getPlayer().kickPlayer("Lag?"));

                data.lagInfo.lastFlying = timeStamp;

                if(data.playerInfo.serverPos && !data.playerInfo.sentpostofalse) {
                    data.playerInfo.sentpostofalse = true;
                    Atlas.getInstance().getSchedular().schedule(() -> {
                        data.playerInfo.serverPos = data.playerInfo.sentpostofalse = false;
                                MiscUtils.testMessage("set position to false");
                    }, data.lagInfo.transPing, TimeUnit.MILLISECONDS);
                }

                data.moveProcessor.process(packet, timeStamp);

                if(data.playerInfo.posLocs.size() > 0) {
                    val optional = data.playerInfo.posLocs.stream()
                            .filter(loc -> loc.toVector().setY(0)
                                    .distance(data.playerInfo.to.toVector().setY(0)) <= 1E-5
                                    && MathUtils.getDelta(loc.y, data.playerInfo.to.y) < 1)
                            .findFirst();

                    if(optional.isPresent()) {
                        data.playerInfo.serverPos = true;
                        MiscUtils.testMessage("set position to true (1)");
                        data.playerInfo.lastServerPos = timeStamp;
                        data.playerInfo.posLocs.remove(optional.get());
                    }
                }
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.ENTITY_ACTION: {
                WrappedInEntityActionPacket packet = new WrappedInEntityActionPacket(object, data.getPlayer());

                ActionProcessor.process(data, packet);
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.BLOCK_DIG: {
                WrappedInBlockDigPacket packet = new WrappedInBlockDigPacket(object, data.getPlayer());

                switch(packet.getAction()) {
                    case START_DESTROY_BLOCK: {
                        data.playerInfo.breakingBlock = true;
                        break;
                    }
                    case STOP_DESTROY_BLOCK:
                    case ABORT_DESTROY_BLOCK:
                    case DROP_ALL_ITEMS:
                    case DROP_ITEM: {
                        data.playerInfo.breakingBlock = false;
                        break;
                    }
                }
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.BLOCK_PLACE: {
                WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(object, data.getPlayer());

                if(packet.getItemStack() != null && packet.getItemStack().getType().isSolid()) {
                    data.playerInfo.lastBlockPlace.reset();
                }

                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.KEEP_ALIVE: {
                WrappedInKeepAlivePacket packet = new WrappedInKeepAlivePacket(object, data.getPlayer());

                long time = packet.getTime();

                if (time == 101) {
                    data.playerInfo.lastVelocity.reset();
                    data.playerInfo.lastVelocityTimestamp = timeStamp;
                    data.predictionService.velocity = true;
                } else if (time == 100) {
                    data.playerInfo.lastServerPos = timeStamp;
                    data.playerInfo.serverPos = true;
                    data.predictionService.position = true;
                    MiscUtils.testMessage("set position to true (2)");
                } else if (time == 200) {
                    data.playerInfo.worldLoaded = false;
                    data.playerInfo.loadedPacketReceived = true;
                } else if (time == 201) {
                    data.playerInfo.worldLoaded = true;
                    data.playerInfo.loadedPacketReceived = true;
                } else {
                    data.lagInfo.lastPing = data.lagInfo.ping;
                    data.lagInfo.ping = System.currentTimeMillis() - data.lagInfo.lastKeepAlive;
                }

                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.TRANSACTION: {
                WrappedInTransactionPacket packet = new WrappedInTransactionPacket(object, data.getPlayer());

                if (packet.getAction() == (short) 69) {
                    data.lagInfo.lastTransPing = data.lagInfo.transPing;
                    data.lagInfo.transPing = System.currentTimeMillis() - data.lagInfo.lastTrans;
                    data.lagInfo.lastClientTrans = timeStamp;

                    //We use transPing for checking lag since the packet used is little known.
                    //AimE have not seen anyone create a spoof for it or even talk about the possibility of needing one.
                    //Large jumps in latency most of the intervalTime mean lag.
                    if (MathUtils.getDelta(data.lagInfo.lastTransPing, data.lagInfo.transPing) > 40) {
                        data.lagInfo.lastPingDrop.reset();
                    }

                    data.lagInfo.pingAverages.add(data.lagInfo.transPing);
                    data.lagInfo.averagePing = data.lagInfo.pingAverages.getAverage();
                }

                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.ARM_ANIMATION: {
                WrappedInArmAnimationPacket packet = new WrappedInArmAnimationPacket(object, data.getPlayer());

                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.HELD_ITEM_SLOT: {
                WrappedInHeldItemSlotPacket packet = new WrappedInHeldItemSlotPacket(object, data.getPlayer());

                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Client.WINDOW_CLICK: {
                WrappedInWindowClickPacket packet = new WrappedInWindowClickPacket(object, data.getPlayer());

                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
        }
        Kauri.INSTANCE.profiler.stop("packet:client:" + getType(type));
    }

    public void processServer(ObjectData data, Object object, String type, long timeStamp) {
        Kauri.INSTANCE.profiler.start("packet:server:" + type);
        switch(type) {
            case Packet.Server.ABILITIES: {
                WrappedOutAbilitiesPacket packet = new WrappedOutAbilitiesPacket(object, data.getPlayer());

                if(data.playerInfo.canFly != packet.isAllowedFlight()) {
                    data.playerInfo.lastToggleFlight.reset();
                }

                data.playerInfo.canFly = packet.isAllowedFlight();
                data.playerInfo.creative = packet.isCreativeMode();
                data.playerInfo.flying = packet.isFlying();
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket packet = new WrappedOutVelocityPacket(object, data.getPlayer());

                if(packet.getId() == data.getPlayer().getEntityId()) {
                    TinyProtocolHandler.sendPacket(data.getPlayer(), new WrappedOutKeepAlivePacket(101).getObject());
                    data.playerInfo.velocityX = (float) packet.getX();
                    data.playerInfo.velocityY = (float) packet.getY();
                    data.playerInfo.velocityZ = (float) packet.getZ();
                }
                data.checkManager.runPacket(packet, timeStamp);
                break;
            }
            case Packet.Server.KEEP_ALIVE: {
                WrappedOutKeepAlivePacket packet = new WrappedOutKeepAlivePacket(object, data.getPlayer());

                data.lagInfo.lastKeepAlive = System.currentTimeMillis();
                data.checkManager.runPacket(packet, timeStamp);
                TinyProtocolHandler.sendPacket(data.getPlayer(), new WrappedOutTransaction(0, (short)69, false).getObject());
                break;
            }
            case Packet.Server.TRANSACTION: {
                WrappedOutTransaction packet = new WrappedOutTransaction(object, data.getPlayer());

                if (packet.getAction() == (short) 69) {
                    data.lagInfo.lastTrans = System.currentTimeMillis();
                }
                break;
            }
            case Packet.Server.POSITION: {
                WrappedOutPositionPacket packet = new WrappedOutPositionPacket(object, data.getPlayer());

                data.playerInfo.posLocs.add(new KLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch()));
                data.checkManager.runPacket(packet, timeStamp);
                TinyProtocolHandler.sendPacket(data.getPlayer(), new WrappedOutKeepAlivePacket(100).getObject());
                break;
            }
        }
        Kauri.INSTANCE.profiler.stop("packet:server:" + type);
    }

    private static String getType(String type) {
        switch(type) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                return "PacketPlayInFlying";
            }
            default: {
                return type;
            }
        }
    }
}