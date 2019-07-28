package io.example

import io.grpc.BindableService
import io.grpc.ServerBuilder

class GrpcServer(
        private val service: BindableService,
        private val port: Int
) {
    private val server = ServerBuilder
            .forPort(port)
            .addService(service)
            .build()

    fun start() {
        server.start()
        println("gRPC server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("shutting down gRPC server since JVM is shutting down")
                this@GrpcServer.stop()
                println("server shut down")
            }
        })
    }

    fun stop() {
        server.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    fun blockUntilShutdown() {
        server.awaitTermination()
    }

}
