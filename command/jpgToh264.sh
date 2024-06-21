#!/bin/bash

ffmpeg -framerate 24 -pattern_type glob -i 'input/*.jpg' -c:v libx264 -pix_fmt yuv420p -g 25 -bf 0 -f h264 output.h264