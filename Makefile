copy_proto_files:
	mkdir -p \
	  src/main/proto/github.com/tendermint/tendermint/abci/types \
	  src/main/proto/github.com/tendermint/tendermint/crypto/merkle \
	  src/main/proto/github.com/tendermint/tendermint/libs/common \
          src/main/proto/github.com/gogo/protobuf/gogoproto && \
	cp ${GOPATH}/src/github.com/tendermint/tendermint/abci/types/types.proto \
	  src/main/proto/github.com/tendermint/tendermint/abci/types/types.proto && \
	cp ${GOPATH}/src/github.com/tendermint/tendermint/crypto/merkle/merkle.proto \
	  src/main/proto/github.com/tendermint/tendermint/crypto/merkle/merkle.proto && \
	cp ${GOPATH}/src/github.com/tendermint/tendermint/libs/common/types.proto \
	  src/main/proto/github.com/tendermint/tendermint/libs/common/types.proto && \
	cp ${GOPATH}/src/github.com/gogo/protobuf/gogoproto/gogo.proto \
	  src/main/proto/github.com/gogo/protobuf/gogoproto/gogo.proto
