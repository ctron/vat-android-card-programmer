# Format

## Sector 0

Sector 0 is mainly controlled by the Mifare Classic chip and contains the
Mifare Application Directory (MAD).

## Sector 1

Sector 1 is used by VAT only.

To be checked: Other users can use other sectors, as defined by the MAD in sector 0. But for now,
VAT is expected to be present in sector 1.

### Data

Rendered with: http://corkami.github.io/sbud/hexii.html

```
hexii: [].concat(
    [
        0x00, 0x0F, 0x42, 0x3F,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    ],
    Array(32).fill(0)
),
descriptions: [
    [4, "Member ID", "999999"],
    [7, "Tag UID", ""],
    [5, "Reserved", ""],
    [32, "Future use", ""],
],
```

![Visual representation of sector 1, blocks 0, 1, 2](sector1.svg)

### Access control

**Key A:** read-only, used by card reader
**Key B:** read-write, used by provisioning application (android app)

```
Block0: C1=1, C2=0, C3=0: read: A, B - write: B
Block1: C1=0, C2=0, C3=0: read: A, B - write: A, B
Block2: C1=0, C2=0, C3=0: read: A, B - write: A, B

Block3: C1=0, C2=1, C3=1: write key A: B, write key B: B, write access bits: B
# Block3: C1=1, C2=0, C3=0: write key A: B, write key B: B
```