package dev.brighten.anticheat.utils;

public class GeneralHash {

    /*public static String getSHAHash(byte[] data, SHAType type) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(type.equals(SHAType.SHA1) ? "SHA-1" :"SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash); // make it printable
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String getMD5Hash(byte[] data) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash); // make it printable
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static MessageDigest getMessageDigest(String type) {
        try {
            return MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash).toLowerCase();
    }

    public enum SHAType {
        SHA256, SHA1
    }*/
}
