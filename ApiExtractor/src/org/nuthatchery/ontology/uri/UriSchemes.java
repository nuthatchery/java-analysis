package org.nuthatchery.ontology.uri;

import java.util.HashMap;
import java.util.Map;

/**
 * A mapping from URI schemes to port numbers.
 *
 * <p>
 * Collected from <a href=
 * "https://gist.github.com/mahmoud/2fe281a8daaff26cfe9c15d2c5bf5c8b">scheme_port_map.json</a>
 * by <a href="https://github.com/mahmoud">Mahmoud Hashemi</a>:
 *
 * <blockquote style="font-style: italic"> A big mapping url schemes to their
 * protocols' default ports. See comments below for notes. Painstakingly
 * assembled by crossreferencing <a href=
 * "https://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml">IANA's URI
 * Schemess</a> and <a href=
 * "https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml">IANA's
 * Service Names and Port Numbers</a>
 * <p>
 * A null in the scheme_port_map indicates a protocol that uses a "//" but does
 * not use a (known) port.
 * <p>
 * The schemes_without_netloc.json file is a short list of common URL schemes
 * that affirmatively do not use network location (no // in the URL).
 * <p>
 * Note that the list is hand assembled, and not automatically generatable, as
 * the name in the IANA scheme registry does not always map well to the IANA
 * port registry. So this made mostly by hand looking up in the port registry,
 * or the linked RFC. Some schemes do not have RFCs linked, and some RFCs do not
 * mention the default port, so some independent research was also necessary.
 * <p>
 * Anyways, hope I saved someone else the trouble! (Python versions of the above
 * can be imported from
 * <a href="https://pypi.python.org/pypi/boltons">boltons</a>'s urlutils
 * submodule). </blockquote>
 */
public class UriSchemes {
	public static final int NO_NETWORK_LOCATION = -1;
	/**
	 * A map from URI scheme names to known default ports.
	 *
	 * <p>
	 * Lookup returns {@link #UNKNOWN_PORT} ({@value #UNKNOWN_PORT}) if the scheme
	 * is known and uses a network location and a <code>//</code> prefix but does
	 * not use a known port.
	 * <p>
	 * Lookup returns {@link #NO_NETWORK_LOCATION} ({@value #NO_NETWORK_LOCATION})
	 * if the scheme is known not to use a network location (no <code>//</code> in
	 * URI).
	 * <p>
	 * Lookup return <code>null</code> if the scheme is unknown.
	 */
	public static final Map<String, Integer> SCHEME_MAP = new HashMap<>();
	public static final int UNKNOWN_PORT = 0;

	static {
		SCHEME_MAP.put("acap", 674);
		SCHEME_MAP.put("afp", 548);
		SCHEME_MAP.put("dict", 2628);
		SCHEME_MAP.put("dns", 53);
		SCHEME_MAP.put("file", 0);
		SCHEME_MAP.put("ftp", 21);
		SCHEME_MAP.put("git", 9418);
		SCHEME_MAP.put("gopher", 70);
		SCHEME_MAP.put("http", 80);
		SCHEME_MAP.put("https", 443);
		SCHEME_MAP.put("imap", 143);
		SCHEME_MAP.put("ipp", 631);
		SCHEME_MAP.put("ipps", 631);
		SCHEME_MAP.put("irc", 194);
		SCHEME_MAP.put("ircs", 6697);
		SCHEME_MAP.put("ldap", 389);
		SCHEME_MAP.put("ldaps", 636);
		SCHEME_MAP.put("mms", 1755);
		SCHEME_MAP.put("msrp", 2855);
		SCHEME_MAP.put("msrps", 0);
		SCHEME_MAP.put("mtqp", 1038);
		SCHEME_MAP.put("nfs", 111);
		SCHEME_MAP.put("nntp", 119);
		SCHEME_MAP.put("nntps", 563);
		SCHEME_MAP.put("pop", 110);
		SCHEME_MAP.put("prospero", 1525);
		SCHEME_MAP.put("redis", 6379);
		SCHEME_MAP.put("rsync", 873);
		SCHEME_MAP.put("rtsp", 554);
		SCHEME_MAP.put("rtsps", 322);
		SCHEME_MAP.put("rtspu", 5005);
		SCHEME_MAP.put("sftp", 22);
		SCHEME_MAP.put("smb", 445);
		SCHEME_MAP.put("snmp", 161);
		SCHEME_MAP.put("ssh", 22);
		SCHEME_MAP.put("steam", 0);
		SCHEME_MAP.put("svn", 3690);
		SCHEME_MAP.put("telnet", 23);
		SCHEME_MAP.put("ventrilo", 3784);
		SCHEME_MAP.put("vnc", 5900);
		SCHEME_MAP.put("wais", 210);
		SCHEME_MAP.put("ws", 80);
		SCHEME_MAP.put("wss", 443);
		SCHEME_MAP.put("xmpp", 0);

		SCHEME_MAP.put("mailto", -1);
		SCHEME_MAP.put("about", -1);
		SCHEME_MAP.put("sip", -1);
		SCHEME_MAP.put("tel", -1);
		SCHEME_MAP.put("data", -1);
		SCHEME_MAP.put("urn", -1);
		SCHEME_MAP.put("bitcoin", -1);
		SCHEME_MAP.put("pkcs11", -1);
		SCHEME_MAP.put("magnet", -1);
		SCHEME_MAP.put("blob", -1);
		SCHEME_MAP.put("sips", -1);
		SCHEME_MAP.put("news", -1);
		SCHEME_MAP.put("geo", -1);
	}

	/**
	 * Check if the scheme has the given port as the default port
	 *
	 * @param scheme
	 *            A scheme, in lowercase letters
	 * @param portNum
	 *            Port number as an integer
	 * @return True if portNum is default port for scheme
	 */
	public static boolean hasPort(String scheme, int portNum) {
		return SCHEME_MAP.get(scheme) == portNum;
	}

	/**
	 * Check if the scheme has the given port as the default port
	 *
	 * @param scheme
	 *            A scheme, in lowercase letters
	 * @param portNum
	 *            Port number as a string; must be a valid integer, may be prefixed
	 *            with ":"
	 * @return True if portNum is default port for scheme
	 */
	public static boolean hasPort(String scheme, String portNum) {
		if (portNum.startsWith(":")) {
			return hasPort(scheme, portNum.substring(1));
		} else if (portNum.equals("")) {
			return false;
		} else {
			return hasPort(scheme, Integer.valueOf(portNum));
		}
	}

	/**
	 * Returns "" or "//" for putting after the scheme in a URI.
	 *
	 * @param scheme
	 * @return "//" if the scheme is unknown or is known to use a network location
	 */
	public static String hierPartPrefix(String scheme) {
		if (SCHEME_MAP.getOrDefault(scheme, UNKNOWN_PORT) == NO_NETWORK_LOCATION) {
			return "";
		} else {
			return "//";
		}
	}
}
