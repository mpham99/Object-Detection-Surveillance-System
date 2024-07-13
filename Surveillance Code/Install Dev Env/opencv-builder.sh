#!/bin/bash

#Find your compute capability from either link
#https://developer.nvidia.com/cuda-gpus
#https://en.wikipedia.org/wiki/CUDA#GPUs_supported
#Your CC MUST match your architecture or you'll need to rebuild
ComputeCapability=5.2

#cmake
cd ~
rm -rf ~/opencv/build
mkdir ~/opencv/build
cd ~/opencv/build
cmake -D CMAKE_BUILD_TYPE=RELEASE \
	-D CMAKE_INSTALL_PREFIX=/usr/local \
	-D INSTALL_PYTHON_EXAMPLES=ON \
	-D INSTALL_C_EXAMPLES=OFF \
	-D OPENCV_ENABLE_NONFREE=ON \
	-D WITH_CUDA=ON \
	-D WITH_CUDNN=ON \
	-D OPENCV_DNN_CUDA=ON \
	-D ENABLE_FAST_MATH=1 \
	-D CUDA_FAST_MATH=1 \
	-D CUDA_ARCH_BIN=$ComputeCapability \
	-D WITH_CUBLAS=1 \
	-D OPENCV_EXTRA_MODULES_PATH=~/opencv_contrib/modules \
	-D HAVE_opencv_python3=ON \
	-D BUILD_EXAMPLES=ON ..

read -p "Press enter to continue"

#make & install opencv binaries
#these last steps take a very long time. you may wish to execute each command in the script individually to ensure nothing catastrophically fails
make -j8
sudo make install
sudo ldconfig
