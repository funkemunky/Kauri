package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.*;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.TimeUnit;

public class PacketProcessor {

    public void processClient(ObjectData data, Object object, String type) {
        switch(type) {
            case Packet.Client.ABILITIES: {
                WrappedInAbilitiesPacket packet = new WrappedInAbilitiesPacket(object, data.getPlayer());

                data.playerInfo.isFlying = packet.isFlying();
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(object, data.getPlayer());

                if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                    data.playerInfo.lastAttack.reset();

                    if(packet.getEntity() instanceof LivingEntity) {
                        if(data.target !=null && data.target.getUniqueId() != packet.getEntity().getUniqueId()) {
                            //Resetting location to prevent false positives.
                            data.targetPastLocation.previousLocations.clear();
                            data.targetBounds = ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(data.target));
                        }

                        data.target = (LivingEntity) packet.getEntity();
                    }
                }
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                WrappedInFlyingPacket packet = new WrappedInFlyingPacket(object, data.getPlayer());

                long currentTime = System.currentTimeMillis();
                if(currentTime - data.lagInfo.lastFlying <= 2) {
                    data.lagInfo.lastPacketDrop.reset();
                }
                data.lagInfo.lastFlying = currentTime;
                Kauri.INSTANCE.profiler.start("flying:process:pre");
                data.predictionService.pre(packet);
                Kauri.INSTANCE.profiler.stop("flying:process:pre");
                Kauri.INSTANCE.profiler.start("flying:process:present");
                MovementProcessor.process(data, packet);
                if(packet.isPos()) {
                    if(data.box != null) {
                        data.predictionService.move(packet);
                    } else {
                        data.predictionService.posX = packet.getX();
                        data.predictionService.posY = packet.getY();
                        data.predictionService.posZ = packet.getZ();
                    }
                }
                Kauri.INSTANCE.profiler.stop("flying:process:present");
                data.checkManager.runPacket(packet);
                data.predictionService.post(packet);
                break;
            }
            case Packet.Client.ENTITY_ACTION: {
                WrappedInEntityActionPacket packet = new WrappedInEntityActionPacket(object, data.getPlayer());

                ActionProcessor.process(data, packet);
                data.checkManager.runPacket(packet);
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
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Client.BLOCK_PLACE: {
                WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(object, data.getPlayer());

                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Client.KEEP_ALIVE: {
                WrappedInKeepAlivePacket packet = new WrappedInKeepAlivePacket(object, data.getPlayer());

                data.lagInfo.lastPing = data.lagInfo.ping;
                data.lagInfo.ping = System.currentTimeMillis() - data.lagInfo.lastKeepAlive;
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Client.TRANSACTION: {
                WrappedInTransactionPacket packet = new WrappedInTransactionPacket(object, data.getPlayer());

                if (packet.getAction() == (short) 69) {
                    data.lagInfo.lastTransPing = data.lagInfo.transPing;
                    data.lagInfo.transPing = System.currentTimeMillis() - data.lagInfo.lastTrans;

                    //We use transPing for checking lag since the packet used is little known.
                    //AimE have not seen anyone create a spoof for it or even talk about the possibility of needing one.
                    //Large jumps in latency most of the intervalTime mean lag.
                    if(MathUtils.getDelta(data.lagInfo.lastTransPing, data.lagInfo.transPing) > 40) {
                        data.lagInfo.lastPingDrop.reset();
                    }
                }
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Client.ARM_ANIMATION: {
                WrappedInArmAnimationPacket packet = new WrappedInArmAnimationPacket(object, data.getPlayer());

                data.checkManager.runPacket(packet);
                break;
            }
        }
    }

    public void processServer(ObjectData data, Object object, String type) {
        switch(type) {
            case Packet.Server.ABILITIES: {
                WrappedOutAbilitiesPacket packet = new WrappedOutAbilitiesPacket(object, data.getPlayer());

                data.playerInfo.canFly = packet.isAllowedFlight();
                data.playerInfo.inCreative = packet.isCreativeMode();
                data.playerInfo.isFlying = packet.isFlying();
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket packet = new WrappedOutVelocityPacket(object, data.getPlayer());

                if(packet.getId() == data.getPlayer().getEntityId()) {
                    data.playerInfo.lastVelocity.reset();

                    Atlas.getInstance().getSchedular().schedule(() -> data.playerInfo.pDeltaY = (float)packet.getY(), data.lagInfo.transPing, TimeUnit.MILLISECONDS);
                }
                data.checkManager.runPacket(packet);
                break;
            }
            case Packet.Server.KEEP_ALIVE: {
                WrappedOutKeepAlivePacket packet = new WrappedOutKeepAlivePacket(object, data.getPlayer());

                data.lagInfo.lastKeepAlive = System.currentTimeMillis();
                data.checkManager.runPacket(packet);
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
                data.playerInfo.lastServerPos = System.currentTimeMillis();
                data.checkManager.runPacket(packet);
                break;
            }
        }
    }
}