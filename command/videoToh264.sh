#!/bin/bash

ffmpeg -i 'file.mp4' -c:v libx264 -pix_fmt yuv420p -g 25 -bf 0 -f h264 output.h264