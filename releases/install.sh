#!/bin/bash

mkdir -p ~/.local/bin

cp yen-linux-x86_64 ~/.local/bin/yen
chmod +x ~/.local/bin/yen

echo "Yen installed."
echo "Run:"
echo "     yen program.yen"
