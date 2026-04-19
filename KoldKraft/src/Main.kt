import koldkraft.tcpListener.TCPListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.io.File
import java.io.InputStream

fun main() = runBlocking {
    val server = TCPListener(42069)
    server.start()
}

