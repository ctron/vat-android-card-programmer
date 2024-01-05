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
