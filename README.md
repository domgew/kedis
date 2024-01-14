# Kedis

Kedis is a Redis client library for Kotlin Multiplatform (JVM + Native). This is possible via Ktor Network, which
provides native and JVM sockets with a unified interface.

* [Targets](#targets)
* [Library Comparison](#library-comparison)

## Targets

**Supported Targets**:

* JVM
* Native: Linux X64
* Native: Linux ARM64
* Native: macOS X64
* Native: macOS ARM64

**Potential Future Targets** (currently no Ktor Network support):

* Native: MinGW X64
* JS: NodeJS

**Non-Targets - Never Coming**:

* Native 32 bit targets
* Native consumer targets (android, iOS, tvOS, watchOS, ...)
* JS: Browser

## Library Comparison

|                               |               Kedis                | [Kreds](https://github.com/crackthecodeabhi/kreds) |
|:------------------------------|:----------------------------------:|:--------------------------------------------------:|
| Automatic Integreation Tests  |              &check;               |                      &check;                       |
| JVM Support                   |              &check;               |                      &check;                       |
| Native Linux X64 Support      |              &check;               |                      &cross;                       |
| Native Linux ARM64 Support    |              &check;               |                      &cross;                       |
| Native macOS X64 Support      |              &check;               |                      &cross;                       |
| Native macOS ARM64 Support    |              &check;               |                      &cross;                       |
| Mature                        |              &cross;               |                      &check;                       |
| Full-Featured                 |              &cross;               |                      &check;                       |
| Pub-Sub Support               |              &cross;               |                      &check;                       |
| Exclusive Configuration       | Compile Time / Sealed Polymorthism |            Run Time / Builder Exception            |
| Responses                     |           Strictly Typed           |                      Semi-Raw                      |
| Networking                    |       Ktor Network (Kotlin)        |                    Netty (Java)                    |
| Redis Protocol (En-/Decoding) |          Custom (Kotlin)           |                    Netty (Java)                    |
| Redis Protocol (Interfacing)  |          Custom (Kotlin)           |                  Custom (Kotlin)                   |
