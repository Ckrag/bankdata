#!/bin/bash

SOURCE_DIR="build/generated/jooq/dk/ckrag/jooq"
DEST_DIR="src/main/java/dk/ckrag/jooq"

# Create the destination directory if it doesn't exist
mkdir -p "$DEST_DIR"

# Remove existing files in the destination directory
rm -rf "$DEST_DIR"/*

# Move the generated files
mv "$SOURCE_DIR"/* "$DEST_DIR"/

echo "Moved jOOQ generated files to $DEST_DIR"
