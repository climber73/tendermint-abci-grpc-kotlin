package io.example

import jetbrains.exodus.env.Environments

fun main() {
    Environments.newInstance("tmp/storage").use { env ->
        val app = KVStoreApp(env)
        val server = GrpcServer(app, 26658)
        server.start()
        server.blockUntilShutdown()
    }
}
