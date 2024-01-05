## Security considerations

Some thoughts on the security on all of this.

### Mifare Classic

Mifare Classic encryption is considered *insecure* at this point. Why was it chosen anyway?

The information stored on the cards is not considered a secret. It is more of an identification,
which allows the controller in the cars to decide if the car should be unlocked. However, this is
only one part of the unlock process. A second part is having an active booking of the car in the
backend system.

Therefore, being able to read the card information, one would be able to clone a card, but it would
still be required to creating make a reservation in the booking system.

So why bother with encryption anyway?

It raises the bar for tampering with the content of the car. That might happen by accident, as NFC
is a technology in a lot of consumer hardware. Using any form of access control should prevent a
good amount of issues which happened by accident.

Aren't you afraid that a car could get stolen?

There seem to be easier ways to steal a car.
