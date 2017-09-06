package com.dglab.cia;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * @author doc
 */
public class WhitelistHandler {
    private static final Logger log = LoggerFactory.getLogger(WhitelistHandler.class);

    private long convertIpToDecimal(String ip) {
        String[] addressParts = ip.split("\\.");

        long result = 0;

        for (int i = 0; i < addressParts.length; i++) {
            long partDecimalValue = Long.parseLong(addressParts[3 - i]);
            result |= partDecimalValue << (i * 8);
        }

        return result;
    }

    private String convertDecimalToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    public Optional<Collection<IpRange>> downloadAndParse(String whiteListUrl) {
        try {
            log.info("Downloading IP white-list");

            HttpResponse<String> response = Unirest.get(whiteListUrl).asString();

            if (response == null) {
                throw new UnirestException("No response");
            }

            if (response.getStatus() != 200) {
                throw new UnirestException("Server answered with code " + response.getStatus());
            }

            String body = response.getBody();
            JSONObject jsonRoot = new JSONObject(body);
            JSONObject dataCenters = jsonRoot.getJSONObject("data_centers");

            Collection<IpRange> result = new HashSet<>();

            dataCenters.keys().forEachRemaining(dataCenterKey -> {
                JSONObject dataCenter = dataCenters.getJSONObject(dataCenterKey);
                JSONArray addressRanges = dataCenter.getJSONArray("address_ranges");

                if (addressRanges != null) {
                    for (Object addressRange : addressRanges) {
                        String addressRangeAsString = addressRange.toString();

                        // The only possible formats should be either
                        // xxx.xxx.xxx.xxx-xxx.xxx.xxx.xxx
                        // or
                        // xxx.xxx.xxx.xxx/xxx

                        int indexOfDash = addressRangeAsString.indexOf('-');
                        if (indexOfDash != -1) {
                            String ipFrom = addressRangeAsString.substring(0, indexOfDash);
                            String ipTo = addressRangeAsString.substring(indexOfDash + 1, addressRangeAsString.length());

                            long ipFromInDecimalForm = convertIpToDecimal(ipFrom);
                            long ipToInDecimalForm = convertIpToDecimal(ipTo);

                            assert ipFromInDecimalForm < ipToInDecimalForm;

                            for (; ipFromInDecimalForm <= ipToInDecimalForm; ipFromInDecimalForm++) {
                                result.add(new IpRange(convertDecimalToIp(ipFromInDecimalForm), null));
                            }
                        } else {
                            int indexOfSlash = addressRangeAsString.indexOf('/');

                            String ipBody = addressRangeAsString.substring(0, indexOfSlash);
                            String subnetMask = addressRangeAsString.substring(indexOfSlash + 1, addressRangeAsString.length());

                            result.add(new IpRange(ipBody, subnetMask));
                        }
                    }
                }
            });

            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Could not obtain IP white-list:" + e.getMessage());
            return Optional.empty();
        }
    }

}
