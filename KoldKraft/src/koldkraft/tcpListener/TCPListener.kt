package koldkraft.tcpListener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

class TCPListener(private val port : Int) {

    fun start() = runBlocking {
        ServerSocket(port).use { server ->
            println("Server started on port $port. Listening for connections...")

            while (true) {
                val clientSocket = server.accept()
                println("Client connected: $clientSocket and accepting from ${clientSocket.remoteSocketAddress}" )

                launch(context = Dispatchers.Default) {
                    handleClient(clientSocket)
                }
            }
        }
    }

    private suspend fun handleClient(socket : Socket) {
        socket.use { client ->
            val lines = getLinesChannel(client.getInputStream())

            for (line in lines) {
                println("Read: $line")
            }

            println("Connections Closed")
        }
    }


    /**
     * Now the same logic return a channel
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getLinesChannel(inputStream: InputStream): ReceiveChannel<String> = coroutineScope {
        produce(Dispatchers.IO) {
            inputStream.use { input ->
                // This variable persists between loop iterations
                var currentLine = ""

                while (true) {
                    val bytes = input.readNBytes(8)
                    if (bytes.isEmpty()) break

                    // Convert bytes to string and append to our persistent state
                    val chunk = String(bytes)
                    val combined = currentLine + chunk

                    // Split the combined string by newline
                    val parts = combined.split("\n")

                    // Process all parts except the last one
                    // (The last one is incomplete and needs to stay in currentLine)
                    for (i in 0..<parts.size - 1) {
                        println("read: ${parts[i]}")
                    }

                    // The last part becomes the new currentLine
                    currentLine = parts.last()
                }
                // Final flush
                if (currentLine.isNotEmpty()) {
                    send(currentLine)
                }
            }
        }
    }
}