@echo off

SET dst=../../src/main/java/

echo Building TF2 GC base...
protoc steammessages.proto --java_out=%dst%
protoc gcsystemmsgs.proto --java_out=%dst%
protoc base_gcmessages.proto --java_out=%dst%
protoc gcsdk_gcmessages.proto --java_out=%dst%
protoc econ_gcmessages.proto --java_out=%dst%

echo Building TF2 GC messages
protoc tf_gcmessages.proto --java_out=%dst%
