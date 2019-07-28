package io.example

import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import jetbrains.exodus.ArrayByteIterable
import jetbrains.exodus.env.Environment
import jetbrains.exodus.env.Store
import jetbrains.exodus.env.StoreConfig
import jetbrains.exodus.env.Transaction
import types.ABCIApplicationGrpc
import types.Types.*

class KVStoreApp(
        private val env: Environment
) : ABCIApplicationGrpc.ABCIApplicationImplBase() {

    private var txn: Transaction? = null
    private var store: Store? = null

    override fun echo(req: RequestEcho, responseObserver: StreamObserver<ResponseEcho>) {
        val resp = ResponseEcho.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun info(req: RequestInfo, responseObserver: StreamObserver<ResponseInfo>) {
        val resp = ResponseInfo.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun setOption(req: RequestSetOption, responseObserver: StreamObserver<ResponseSetOption>) {
        val resp = ResponseSetOption.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun checkTx(req: RequestCheckTx, responseObserver: StreamObserver<ResponseCheckTx>) {
        val code = req.tx.validate()
        val resp = ResponseCheckTx.newBuilder()
                .setCode(code)
                .setGasWanted(1)
                .build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun initChain(req: RequestInitChain, responseObserver: StreamObserver<ResponseInitChain>) {
        val resp = ResponseInitChain.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun beginBlock(req: RequestBeginBlock, responseObserver: StreamObserver<ResponseBeginBlock>) {
        txn = env.beginTransaction()
        store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn!!)
        val resp = ResponseBeginBlock.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun deliverTx(req: RequestDeliverTx, responseObserver: StreamObserver<ResponseDeliverTx>) {
        val code = req.tx.validate()
        if (code == 0) {
            val parts = req.tx.split('=')
            val key = ArrayByteIterable(parts[0])
            val value = ArrayByteIterable(parts[1])
            store!!.put(txn!!, key, value)
        }
        val resp = ResponseDeliverTx.newBuilder()
                .setCode(code)
                .build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun endBlock(req: RequestEndBlock, responseObserver: StreamObserver<ResponseEndBlock>) {
        val resp = ResponseEndBlock.newBuilder().build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun commit(req: RequestCommit, responseObserver: StreamObserver<ResponseCommit>) {
        txn!!.commit()
        val resp = ResponseCommit.newBuilder()
                .setData(ByteString.copyFrom(ByteArray(8)))
                .build()
        responseObserver.onNext(resp)
        responseObserver.onCompleted()
    }

    override fun query(req: RequestQuery, responseObserver: StreamObserver<ResponseQuery>) {
        val k = req.data.toByteArray()
        val v = getPersistedValue(k)
        val builder = ResponseQuery.newBuilder()
        if (v == null) {
            builder.log = "does not exist"
        } else {
            builder.log = "exists"
            builder.key = ByteString.copyFrom(k)
            builder.value = ByteString.copyFrom(v)
        }
        responseObserver.onNext(builder.build())
        responseObserver.onCompleted()
    }

    private fun getPersistedValue(k: ByteArray): ByteArray? {
        return env.computeInReadonlyTransaction { txn ->
            val store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn)
            store.get(txn, ArrayByteIterable(k))?.bytesUnsafe
        }
    }

    private fun ByteString.validate(): Int {
        val parts = this.split('=')
        if (parts.size != 2) {
            return 1
        }
        val key = parts[0]
        val value = parts[1]

        // check if the same key=value already exists
        val stored = getPersistedValue(key)
        if (stored != null && stored.contentEquals(value)) {
            return 2
        }

        return 0
    }

    private fun ByteString.split(separator: Char): List<ByteArray> {
        val arr = this.toByteArray()
        val i = (0 until this.size()).firstOrNull { arr[it] == separator.toByte() }
                ?: return emptyList()
        return listOf(
                this.substring(0, i).toByteArray(),
                this.substring(i + 1).toByteArray()
        )
    }
}
