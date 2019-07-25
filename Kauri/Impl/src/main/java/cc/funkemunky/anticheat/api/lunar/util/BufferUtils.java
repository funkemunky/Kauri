package cc.funkemunky.anticheat.api.lunar.util;

import com.google.common.base.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

public class BufferUtils {

    public static byte[] writeVarInt(int value) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        while ((value & -128) != 0) {
            os.write(value & 127 | 128);
            value >>>= 7;
        }

        os.write(value);

        os.close();

        return os.toByteArray();
    }

    public static byte[] writeString(String value) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] bytes = value.getBytes(Charsets.UTF_8);
        os.write(writeVarInt(bytes.length));
        os.write(bytes);

        os.close();

        return os.toByteArray();
    }

    public static byte[] writeDouble(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static byte[] writeLong(long value) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(os);

        out.writeLong(value);

        out.close();
        os.close();

        return os.toByteArray();
    }

    public static byte[] writeFloat(float value) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(os);

        out.writeFloat(value);

        out.close();
        os.close();

        return os.toByteArray();
    }

    public static byte[] writeMap(Map<UUID, String> players) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(writeVarInt(players.size()));
        for (Map.Entry<UUID, String> player : players.entrySet()) {
            os.write(getBytesFromUUID(player.getKey()));
            os.write(writeString(player.getValue()));
        }

        os.close();

        return os.toByteArray();
    }

    public static byte[] writeBoolean(boolean value) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(os);

        out.writeBoolean(value);

        out.close();
        os.close();

        return os.toByteArray();
    }

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    public static byte[] writeInt(int value) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(os);

        out.writeInt(value);

        out.close();
        os.close();

        return os.toByteArray();
    }

    public static byte[] writeRGB(int r, int g, int b) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(255);
        os.write(r);
        os.write(g);
        os.write(b);

        os.close();
        return os.toByteArray();
    }

}
