package com.dglab.cia;

/**
 * @author doc
 */
public class IpRange {
	private final long network;
	private final long netmask;

	public IpRange(String networkPart, String cidrPart) {
		long netmask = 0;
		int cidr = cidrPart == null ? 32 : Integer.parseInt(cidrPart);
		for (int pos = 0; pos < 32; ++pos) {
			if (pos >= 32 - cidr) {
				netmask |= (1L << pos);
			}
		}

		this.network = netmask & toMask(networkPart);
		this.netmask = netmask;
	}

	public boolean isInRange(String ip) {
        return network == (toMask(ip) & netmask);
	}

	private long toMask(String ip) {
        int ipInt = 0;

        for (String value : ip.split("\\.")) {
            ipInt = (ipInt << 8) + Integer.valueOf(value);
        }

        byte[] data = new byte[4];

        data[3] = (byte) (ipInt & 0xFF);
        data[2] = (byte) ((ipInt >> 8) & 0xFF);
        data[1] = (byte) ((ipInt >> 16) & 0xFF);
        data[0] = (byte) ((ipInt >> 24) & 0xFF);

		long accum = 0;
		int idx = 3;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= ((long) (data[idx] & 0xff)) << shiftBy;
			idx--;
		}
		return accum;
	}
}

