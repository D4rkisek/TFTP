The Trivial File Transfer Protocol (TFTP) is an Internet software utility for transferring files that is simpler to use than the File Transfer Protocol (FTP) but less capable. It is used where user authentication and directory visibility are not required. For example, it is used by Cisco routers and switches to transfer images of the operating system from/to the devices.

The implementaiton was following the [RFC1350](https://www.ietf.org/rfc/rfc1350.txt) specification, and is written in java.

Supports for octet mode only. The files are transferred as a raw sequence of bytes. Does not read, write or transfer files as characters.
Supports only for error handling when the server is unable to satisfy the request because the file cannot be found.
