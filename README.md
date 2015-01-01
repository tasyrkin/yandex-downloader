yandex-downloader
=================

The `yandex-downloader` is a small library that provides functionality for manipulating a download such as:
- starting
- pausing
- resuming
- cancelling
- requesting download's status

A download consists of multiple source urls and a destination files accociated with every url.
A pair of source url and destination is called as download request entry

The content of every source url is downloaded in its own thread and using one of the following standard protocols (specified as part of a url):
`file`, `ftp`, `gopher`, `http`, `https`, `jar`, `mailto` and `netdoc`. It was decided use the standard java implementations and not inroduce own abstraction layer.

In case if some other protocol is desired then the use case must be specified and analysed and the library can be extended appropriately.

A download request entry may be in the states: `INITIAL`, `IN_PROGRESS`, `PAUSED`, `CANCELLED`, `FAILED` and `FINISHED`.
Althrough the standard way of representing this finite state machine is a State design pattern,
it was implemented in a ad hoc way with the following transitions.

| From \ To   | INITIAL | IN_PROGRESS         | PAUSED | CANCELLED | FAILED           | FINISHED           |
| -----------     | --- | -----------         | ------ | --------- | ------           | --------           |
| **INITIAL**     | x   | start               | x      | x         | x                | x                  |  
| **IN_PROGRESS** | x   | x                   | pause  | cancel    | exception thrown | download completed |
| **PAUSED**      | x   | restart/resume      | x      | cancel    | x                | x                  |
|**CANCELLED**    | x   | restart             | x      | x         | x                | x                  | 
|**FAILED**       | x   | restart             | x      | x         | x                | x                  |
|**FINISHED**     | x   | restart             | x      | x         | x                | x                  |

Pausing, resuming and cancellation are requested from the client code and in some circumstances is not possible to perform.
For example, if a download is already finished or failed, then none of the requests may succeed.   

*Possible improvements* The library can be improved in the following ways:
- implement callback for the event when all dowload request entries were `FINISHED`, at least one `FAILED`
