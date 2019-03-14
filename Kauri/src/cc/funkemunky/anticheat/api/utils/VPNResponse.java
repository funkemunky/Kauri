package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;

import java.util.Map;

@Getter
public class VPNResponse {

    private boolean status, usingProxy;
    private String ip, hostName, countryCode, countryName, ISP, city;

    public VPNResponse(Map<String, String> response) {
        if (this.status = response.get("status").equals("success")) {
            this.usingProxy = Boolean.parseBoolean(response.get("hostIP"));
            this.hostName = response.get("hostname");
            this.ISP = response.get("org");
            this.ip = response.get("ip");
            this.city = response.get("city");
            this.countryName = response.get("countryName");
            this.countryCode = response.get("countryCode");
        } else {
            usingProxy = false;
            ip = hostName = countryName = countryCode = ISP = city = "N/A";
        }
    }
}
