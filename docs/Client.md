# Client

rough documentation on how the client operates

## General flow

1) Gathering the address
2) Establishing a connection to the server
3) Gathering message data
4) Sending the message
5) Closing the connection

## Input Gathering

By default, the client expects five CLI arguments in the following order:

1) Server address (as a string, preferably a IPv4 address)
2) Server port (truncated to fit in a 16-bit unsigned integer)
3) The ID of the user
4) The postcode of the reading
5) The reading (formatted as a valid Java floating point literal)

Arguments **MUST BE** in order. The arguments are split into two groups, the
address group (1, 2) and the message group (3, 4, 5). If an argument from a
group is not provided, the whole group must not be provided either.

The client allows each group to be provided at runtime. The client will tell the
client which field it wants, and the user types in the expected field.
