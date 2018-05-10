# Contributions

## Networking
The networking package was designed and programmed by Joshua Karns.

## Client
The client was designed and programmed by Ian Ramazan.

## Server
The server was designed and programmed by Benjamin Valentino.

## Bug fixing
Bug fixing was done by everyone mentioned above.


# Protocol
`psc` uses a simple protocol on top of `UDP`, using `multicast`.

# Packet Types
There are 6 types of packets used by `psc` to relay chat information.
Each packet begins with a one byte opcode which determines the type of packet.

## Write Packet

| 1 byte | 8 bytes     | 8 bytes | 8 bytes | 2 bytes | `msg len` bytes  | 1 byte       | `username len` bytes |
| ------ | ----------- | ------- | ------- | ------- | ---------------- | ------------ | -------------------- |
| 0x00   | Channel ID# | Msg ID# | magic   | Msg Len | Message Contents | Username Len | Username             |

`Write packets` are used by clients to send chat messages to the server, and by
the server to send chat messages to clients.

When a client sends a `write packet` to a server, the `Msg ID#` field should be
set to `-1`. The client should not display the message; it should wait until it
receives the corresponding `write packet` from the server, as to maintain chat
order.

If a client sends a `write packet` to the server but there in no channel that
corresponds to `Channel ID#`, then an error packet will be sent back with
error number `No Such Channel`.

When a client receives a write header from the server, it should display the
corresponding message in the proper order, where messages with a lower ID# are
displayed first.

The `magic` field is used to differentiate between messages sent by a client
that have the same contents.



## Join Packet

| 1 byte | 1 byte               | `desired username len` bytes | 1 byte           | `channel name len` bytes |
| ------ | -------------------- | ---------------------------- | ---------------- | ------------------------ |
| 0x01   | Desired Username Len | Username                     | Channel Name Len | Channel Name             |

`Join packets` are used by clients to initially connect to a server. The packet
contains the desired username, however there is no guarantee you will receive
that username; if there is a repeat, `#N` will be appended to the end of it
where `N` is the number of username repeats there have been.

In response to a `join packet`, the server should send a `source packet`.
This response functions as a way for the client to, naturally, receive the stream
source, but also serves as an acknowledgement to the users connection

In the event that a client attempted to join a channel that does not exist, the
channel will be created by the server, and the first person to join that
channel will be able to set an `operator` password.



## Leave Packet

| 1 byte | 8 bytes     |
| ------ | ----------- |
| 0x02   | Channel ID# |

`Leave packets` are used by clients to signal that they're disconnecting from the
chat. The server should send an `error packet` signaling the end of the session.



## Source Packet

| 1 byte | 8 bytes     | 1 byte           | `channel name len` bytes | 1 byte                | `assigned username len` bytes |
| ------ | ----------- | ---------------- | ------------------------ | --------------------- | ----------------------------- |
| 0x03   | Channel ID# | Channel Name Len | Channel Name             | Assigned Username Len | Assigned Username             |

`Source packets` are used by the server to tell the client what their assigned
username is, and to acknowledge the receipt of a `join packet`.


## Negative Acknowledgement Packet (NAK Packet)

| 1 byte | 8 bytes                   | 8 bytes                   | 8 bytes     |
| ------ | ------------------------- | ------------------------- | ----------- |
| 0x04   | Lower Msg ID# (inclusive) | Upper Msg ID# (exclusive) | Channel ID# |

`Negative acknowledgement packets` are sent by clients when a missing message is
noticed. For example, if a client receives messages 0, 1, and 3, a `NAK packet`
for 2 will be sent (Lower Msg ID# will be 2, upper Msg ID# will be 3).



## Error Packet

| 1 byte | 1 byte     | 2 bytes       | `error msg len` bytes |
| ------ | ---------- | ------------- | --------------------- |
| 0x05   | Error Code | Error Msg Len | Error Msg             |

`Error packets` are used by both clients and the server to signal when something
went wrong. They also serve as an acknowledgement for a client disconnecting.

The following table contains all valid error codes and their meanings.

| Error Code | Meaning                  |
| ---------- | ------------------------ |
| 0x00       | Connection Closed        |
| 0x01       | Invalid Unicode          |
| 0x02       | No Such Channel          |
| 0x03       | Failed channel creation  |
| 0x04       | Failed channel join      |
| 0x05       | No such user             |
(more to come)



## Heartbeat Packet

| 1 byte | 8 bytes     |
| ------ | ----------- |
| 0x06   | Channel ID# |

`Heartbeat packets` are sent by both the client and server to signal that
they're still alive and listening. A heartbeat packet should be sent at least
every 20 seconds.



## Info Packet

| 1 byte | 8 bytes     | 1 byte     | 8 bytes     | 2 bytes     | `message len` bytes |
| ------ | ----------  | ---------- | ----------- | ----------- | ------------------- |
| 0x07   | Channel ID# | Info Code  | Message ID# | Message Len | Message             |

`Info packets` are used to update clients about the status of a channel in the
event of an exceptional event. For example, if a client is kicked by the op of
a channel, the client will receive an `info packet` that has the corresponding
channel id# in it, and info code set to Kicked.

If message id is -1, that means the info packet was sent as a private server message.

| Info Code | Meaning             | Usage |
| --------- | ------------------- | ----- |
| 0x00      | Kicked from channel | Sent to the user that was removed from the channel |
| 0x01      | Muted in channel    | Sent to the user that was muted in the channel |
| 0x02      | Unmuted in channel  | Sent to the user that was unmuted in the channel |
| 0x03      | Server message      | Sent to relay any message  |
| 0x04      | Connection Closed   | Sent to a user to acknowledge a leave packet |



## Command Packet

| 1 byte | 8 bytes     | 2 bytes     | `command len` bytes |
| ------ | ----------- | ----------- | ------------------- |
| 0x08   | Channel ID# | Command Len | Command             |

`Command Packets` are a way for clients to run commands that will change the
state of a channel.

Commands are typed into the chat like normal, except they begin with a '/', for
example, if your username is john: "/me jumps!" would put "john jumps!" in the chat.

### Normal Commands
| Command | Arguments           | Effect       |
| ------- | ------------------- | ------------ |
| me      | `[sentence]`          | server will send a server message containing "`[username] [sentence]`" |
| op      | `password [op command]` | will run the specified op command if the password matches |
| whois   | `[a username]` | will |

TODO: Add more commands?

### Op Comands
| Command | Arguments           | Effect        |
| ------- | ------------------- | ------------- |
| kick    | `[user to kick]`    | Removes specified user from the channel |
| mute    | `[user to mute]`    | Mutes the specified user |
| unmute  | `[user to unmute]`  | Unmutes the specified user |



## Ack Packet

| 1 byte | 1 byte          | `n` bytes               |
| ------ | --------------- | ----------------------- |
| 0x09   | Received Opcode | Body of Received Header |

`Ack packets` are sent in response to `message packets` and `info packets`
by both

## Conglomerate Packet

| 1 byte | 4 bytes            |
| ------ | ------------------ |
| 0xFF   | Number of Packets  |

Following this header, for each packet `n` in 0 until Number of Packets:

| 4 bytes         | `packet len` bytes |
| --------------- | ------------------ |
| Packet `n` Len  | Packet Data        |

This is to be used as a way to piggyback packets.
