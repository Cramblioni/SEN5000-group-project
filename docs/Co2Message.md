# Co2Message & TimestampedCo2Record

The in-memory representation of both the network format and the on-disk format.
The primary difference between the two is the timestamp and the target format.


## Co2Message network format

Each field is serialised in the order of `ID`, `Postcode`, then `Reading`.
Strings are stored as a big-endian 16 bit integer (string length in bytes)
followed the bytes in the string. Both `ID` and `Postcode` follow this format.
The `Reading` is stored as a 32-bit big endian IEEE 754 single precision
floating point number. Each of these values is packed one after the other.

The object `Co2Message("ST12345678", "POST 1OL", 13.37)` will be serialised as:

```
00000000: 000a 5354 3132 3334 3536 3738 0008 504f  ..ST12345678..PO
00000010: 5354 2031 4f4c 4155 eb85                 ST 1OLAU..
```
(hexdump created by XXD)

## TimestampedCo2Record on-disk format

This class is used to create the required `.csv` line to store the reading along
with a the time the reading was recieved. The produced `.csv` file is updated
with each new reading recieved.

Each line in the produced `.csv` file follows the order `Time`, `ID`,
`Postcode`, `Reading`. `ID` and `Postcode` are written into the file **AS IS**
without further formatting. `Reading` is written as a valid Java double literal.
This can be read as-is and avoids the `f` suffix. `Time` is written as a ISO
8601 local date-time interchange format, using "T" as a seperator. It does not
account for timezones. The result formatted like
`YYYY-MM-DDTHH:MM:SS.s` (where the length of "s" varies depending on sub-second
truncation).

Using the example from `Co2Message`, if that was recieved on 2025/12/05 at
exactly 21:32:11.5 (on the server), the resulting `.csv` record will look like:

```
2025-12-05%21:32:11.5,ST12345678,POST 1OL,13.37
```
