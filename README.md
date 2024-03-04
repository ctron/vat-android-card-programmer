# Access card programmer for VAT access cards

[![CI](https://github.com/ctron/vat-android-card-programmer/actions/workflows/ci.yaml/badge.svg)](https://github.com/ctron/vat-android-card-programmer/actions/workflows/ci.yaml)

Program access cards for the VAT car sharing community.

## Card format

See: [docs/FORMAT.md](docs/FORMAT.md)

## Configuration

The application requires a configuration to run. The configuration needs to be imported from a
JSON configuration file. The application will give the user to choose which file to import, either
from the local mobile phone storage, or any other source the mobile phone supports
(like Google Drive).

> [!CAUTION]
> The configuration file contains the encryption keys. Leaking this information puts your security
> at risk.

## Prefill write data

It is possible to prefill the write fragment text entries with data, e.g. through a QR code.

For that, the application registers a URL handler for the scheme `vat-card` and requires the host
`write`. The actual data to prefill is provided through the URL query.

The following query parameters can be used:

<dl>
<dt><code>mid</code><dt><dd>Member ID</dd>
<dt><code>hn</code><dt><dd>Holder Name</dd>
<dt><code>hid</code><dt><dd>Holder ID</dd>
<dt><code>hidt</code><dt><dd>Holder Type (any of <code>card_number</code>, <code>drivers_license</code>, or just leaving it empty)</dd>
</dl>

> [!NOTE]
> Values need to URL encoded.

For example:

```
vat-card://write?mid=1234&hn=Eva+Mustermann&hid=42&hidt=card_number
```