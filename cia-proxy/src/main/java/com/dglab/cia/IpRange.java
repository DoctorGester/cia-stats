package com.dglab.cia;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author doc
 */
public class IpRange {
	private final long network;
	private final long netmask;

	public IpRange(String networkPart, String cidrPart) throws UnknownHostException {
		long netmask = 0;
		int cidr = cidrPart == null ? 32 : Integer.parseInt(cidrPart);
		for (int pos = 0; pos < 32; ++pos) {
			if (pos >= 32 - cidr) {
				netmask |= (1L << pos);
			}
		}

		this.network = netmask & toMask(InetAddress.getByName(networkPart));
		this.netmask = netmask;
	}

	public boolean isInRange(String ip) {
		try {
			return network == (toMask(InetAddress.getByName(ip)) & netmask);
		} catch (UnknownHostException e) {
			return false;
		}
	}

	private long toMask(InetAddress address) {
		byte[] data = address.getAddress();
		long accum = 0;
		int idx = 3;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= ((long) (data[idx] & 0xff)) << shiftBy;
			idx--;
		}
		return accum;
	}
}

