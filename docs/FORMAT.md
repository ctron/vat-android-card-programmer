# Format

## Sector 0

## Sector 1

Sector 1 is used by VAT only.

To be checked: Other users can use other sectors, as defined by the MAD in sector 0. But for now,
VAT is expected to be present in sector 1.

### Access control

**Key A:** read-only, used by card reader
**Key B:** read-write, used by provisioning application (android app)

Block0: C1=1, C2=0, C3=0: read: A, B - write: B
Block1: C1=0, C2=0, C3=0: read: A, B - write: A, B
Block2: C1=0, C2=0, C3=0: read: A, B - write: A, B

? Block3: C1=1, C2=0, C3=0: write key A: B, write key B: B
? Block3: C1=0, C2=1, C3=1: write key A: B, write key B: B, write access bits: B
