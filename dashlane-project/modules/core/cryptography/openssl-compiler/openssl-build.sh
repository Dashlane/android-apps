#!/bin/bash
set -e
set -x

source openssl.config
###
### Processor architecture Loop
###
for i in "${architectures[@]}"
do
echo "processing $i"
./openssl-build-arch-specific.sh ${i}
done

echo "all done for : ${architectures[@]}" | tee -a ${OPENSSL_BUILD_LOG}