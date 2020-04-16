package dev.brighten.anticheat.utils.mojang;

public class URLHandler {

	public static final String UUID_GETTER = "https://api.mojang.com/users/profiles/minecraft/%s";
	public static final String USERNAME_GETTER = "https://api.mojang.com/user/profiles/%s/names";
	
	public static String formatAPI(String API, String value) {
		return String.format(API, value);
	}		
}