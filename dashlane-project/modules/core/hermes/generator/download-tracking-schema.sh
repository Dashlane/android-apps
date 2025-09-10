#!/usr/bin/env bash

set -e

OUTPUT_DIR=../tracking-schemas
VERSION_FILE=$OUTPUT_DIR/version.txt

rm -rf $OUTPUT_DIR
mkdir $OUTPUT_DIR

wget -O $VERSION_FILE https://track-production.service.kwift.eu/current

TRACKING_SCHEMA_VERSION=`cat $VERSION_FILE`

# man wget
#
# -r,  --recursive                  specify recursive download.
# -np, --no-parent                  don't ascend to the parent directory.
# -R,  --reject=LIST                comma-separated list of rejected extensions.
# -nH, --no-host-directories        don't create host directories.
#      --cut-dirs=NUMBER            ignore NUMBER remote directory components.
# -P,  --directory-prefix=prefix    The directory prefix is the directory where all other files and subdirectories will be saved to

wget -r --no-parent --reject '*index.html*' --cut-dirs=1 -nH -P $OUTPUT_DIR https://track-production.service.kwift.eu/$TRACKING_SCHEMA_VERSION/
