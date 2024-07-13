#!/bin/bash

sudo apt-get -y update
sudo apt-get -y upgrade

# Purge existing nvidia drivers & cuda
# cuda toolkit will install the appropriate driver
sudo apt-get --purge remove -y "cublas*" "cuda*" "nsight*"
sudo apt-get --purge remove -y "nvidia*"
sudo rm -rf /usr/local/cuda*

#Dependencies
sudo apt-get install build-essential cmake unzip pkg-config
sudo apt-get install libjpeg-dev libpng-dev libtiff-dev
sudo apt-get install libavcodec-dev libavformat-dev libswscale-dev
sudo apt-get install libv4l-dev libxvidcore-dev libx264-dev
sudo apt-get install libgtk-3-dev
sudo apt-get install libatlas-base-dev gfortran
sudo apt-get install python3-dev
sudo apt-get -y install python3-numpy

#OpenCV 4.2
#cd ~
#wget -O opencv.zip https://github.com/opencv/opencv/archive/4.2.0.zip
#wget -O opencv_contrib.zip https://github.com/opencv/opencv_contrib/archive/4.2.0.zip
#unzip opencv.zip
#unzip opencv_contrib.zip
#mv opencv-4.2.0 opencv
#mv opencv_contrib-4.2.0 opencv_contrib

#OpenCV 4.5
cd ~
wget -O opencv.zip https://github.com/opencv/opencv/archive/4.5.0.zip
wget -O opencv_contrib.zip https://github.com/opencv/opencv_contrib/archive/4.5.0.zip
unzip opencv.zip
unzip opencv_contrib.zip
mv opencv-4.5.0 opencv
mv opencv_contrib-4.5.0 opencv_contrib
mkdir ~/opencv/build
cd ~/opencv/build
