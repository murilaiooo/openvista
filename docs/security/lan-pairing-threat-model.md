# Local pairing threat model

Status: implementation design, **independent security review pending**. Issue [#8](https://github.com/murilaiooo/openvista/issues/8) and G-08/G-06 remain open. `PairingSecurity.REVIEWED` is a compile-time `false` gate. The public coordinator returns a review-pending explanation before enumerating interfaces or creating a socket. Writing this document does not approve that gate.

## Trust boundaries

The owner starts pairing on the TV. The phone, browser, Wi-Fi network, other LAN clients, submitted text and HTTP framing are untrusted. A QR fragment carries a random 256-bit capability for a two-minute session. The phone sends one URL, title and explicit HLS/DASH choice. The TV shows a pending request; only a subsequent TV approval returns that source to the library UI. Pairing does not fetch the source, start playback, import lists, change settings or write the library itself.

The listener binds one unambiguous RFC1918 IPv4 interface and an ephemeral port, never a wildcard interface. Ambiguous/no private addresses fail closed. Peers must belong to that interface's subnet. There is no discovery broadcast, cloud relay, account, persistent pairing, port forwarding or automatic startup. TV screen closure, denial, approval, expiry and coordinator closure end the socket session. Network changes are handled by loss of the bound interface, followed by expiry; reconnect requires a fresh TV action.

## Threats and controls

| Threat | Implemented boundary | Remaining review work |
| --- | --- | --- |
| Guessing/replay | SecureRandom 32-byte nonce, constant-time comparison, monotonic two-minute expiry, single valid submission, random request ID and one TV approval | Inspect entropy and race behavior; exercise stale queued callbacks and approval at expiry |
| CSRF and DNS rebinding | Exact numeric Host plus port, exact Origin on POST, JSON and Authorization headers, no OPTIONS/CORS response, no cookies; URL token is a fragment removed from visible history by the page | Browser matrix and hostile-origin cases; consider shared-LAN proxies |
| Unauthorized library writes | Parser validates URL/kind; incoming request remains in memory and requires TV approval; only `approve(id)` returns accepted data | Inspect every caller to ensure it mutates only after approval and previews without exposing URL credentials |
| SSRF and executable input | UserSourceParser, bounded URL/title/JSON, no URL requests during pairing, no HTML insertion from submitted input | Playback has a separate user-source network boundary; pairing is not proof of safe playback |
| Slow clients and memory exhaustion | One client at a time, backlog 2, 1.5-second socket timeout, two-second request deadline, 8 KiB headers, 6 KiB body, at most 24 connections per session | Fuzz HTTP framing and run abuse cases on target hardware |
| XSS, remote assets and token leakage | App-generated page, escaped localized labels, nonce CSP, no external assets, no URL/referrer token, no-store, no-referrer, frame denial, no logs/persistence | Browser history, keyboard/browser autofill and phone screenshot behavior remain outside TV control |
| Interface exposure | Private IPv4 binding, subnet restriction, no listener while gate is false, explicit TV start and bounded lifetime | Inspect VPN/hotspot/guest network topology and client-isolation behavior |
| LAN interception | Warning explains HTTP before sharing private source URLs; no claim of authenticated/encrypted transport | **Unresolved confidentiality limitation:** a malicious LAN observer can read the token and URL or alter the page. TV confirmation limits writes but does not protect URL secrecy. A reviewed authenticated transport/pairing design or an explicit accepted residual-risk decision is required before enablement |

The HTTP scheme does not provide TLS protection; the [HTTP semantics standard](https://www.rfc-editor.org/rfc/rfc9110.html#section-4.2.1) defines its origin semantics separately from HTTPS. Browser CORS controls do not protect against a malicious native LAN client; see the [Fetch CORS protocol](https://fetch.spec.whatwg.org/#http-cors-protocol). Binding and backlog limit follow [ServerSocket.bind](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/ServerSocket.html#bind(java.net.SocketAddress,int)); backlog is an implementation-dependent hint, so application time/body/session bounds remain necessary.

## Enablement record required

An independent reviewer must record name/date, reviewed commit, findings and fixes, residual risks and owner decision. Include protocol abuse cases, browser behavior, target Fire OS listener lifecycle, network switching and background shutdown. Resolve the HTTP confidentiality risk before changing the compile gate. Permission/platform behavior on newer Android versions needs a separate check. No device or security acceptance is recorded by this implementation.
