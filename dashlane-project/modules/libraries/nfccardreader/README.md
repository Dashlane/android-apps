# NFC Card Reader Module

This module is a fork of an existing library that can be found here [EMV NFC Paycard Enrollment](https://github.com/devnied/EMV-NFC-Paycard-Enrollment)
We are currently using version `3.0.0` of this project (commit `bfbd3960708689154a7a75c8a9a934197d738a5b`)

As of now, we use this module in order to be able to read Credit Card information using NFC. It 
is then way easier to add a Credit Card into Dashlane (only security code remains to be added 
manually).

## Why not using the dependency?

Due to the way this library has been developed, it included some dependencies that we did not 
wanted to have.

For instance, it has originally a dependency to `SLF4J Logger`, and a LGPL library know as `net.sf.scuba`

Some utilities classes such as `BitUtils` and `BytesUtils` have also been forked from [Bit Lib4J](https://github.com/devnied/Bit-lib4j)

Finally, `TlvInputStream` is inspired but rewrote from the original one embedded into the removed
 `net.sf.scuba` library
