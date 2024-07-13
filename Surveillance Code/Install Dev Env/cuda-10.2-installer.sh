#!/bin/bash

#INSTALL cudnn-10.2-linux-x64-v8.0.4.30.tgz TO YOUR DOWNLOADS DIRECTORY BEFORE RUNNING THIS SCRIPT!
#https://developer.nvidia.com/rdp/cudnn-download


# Install CUDA Toolkit 10.2 & NVIDIA drivers
cd ~
wget https://developer.download.nvidia.com/compute/cuda/repos/ubuntu1804/x86_64/cuda-ubuntu1804.pin
sudo mv cuda-ubuntu1804.pin /etc/apt/preferences.d/cuda-repository-pin-600
wget http://developer.download.nvidia.com/compute/cuda/10.2/Prod/local_installers/cuda-repo-ubuntu1804-10-2-local-10.2.89-440.33.01_1.0-1_amd64.deb
sudo dpkg -i cuda-repo-ubuntu1804-10-2-local-10.2.89-440.33.01_1.0-1_amd64.deb
sudo apt-key add /var/cuda-repo-10-2-local-10.2.89-440.33.01/7fa2af80.pub
sudo apt-get update
sudo apt-get -y install cuda

#Install CUDA 11.1 for Ubuntu 20.04
#wget https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-ubuntu2004.pin
#sudo mv cuda-ubuntu2004.pin /etc/apt/preferences.d/cuda-repository-pin-600
#wget https://developer.download.nvidia.com/compute/cuda/11.1.1/local_installers/cuda-repo-ubuntu2004-11-1-local_11.1.1-455.32.00-1_amd64.deb
#sudo dpkg -i cuda-repo-ubuntu2004-11-1-local_11.1.1-455.32.00-1_amd64.deb
#sudo apt-key add /var/cuda-repo-ubuntu2004-11-1-local/7fa2af80.pub
#sudo apt-get update
#sudo apt-get -y install cuda

# Extract cuDNN v8.0.4 for 10.2 and copy its files into the CUDA Toolkit directory
cd ~/Downloads
rm -rf ~/Downloads/cuda
tar -xzvf cudnn-10.2-linux-x64-v8.0.4.30.tgz
sudo cp ~/Downloads/cuda/include/cudnn*.h /usr/local/cuda-10.2/include
sudo cp ~/Downloads/cuda/lib64/libcudnn* /usr/local/cuda-10.2/lib64
sudo chmod a+r /usr/local/cuda-10.2/include/cudnn*.h /usr/local/cuda-10.2/lib64/libcudnn*

# Extract cuDNN 8.0.4 for 11.1
#cd ~/Downloads
#rm -rf ~/Downloads/cuda
#tar -xzvf cudnn-11.1-linux-x64-v8.0.4.30.tgz
#sudo cp ~/Downloads/cuda/include/cudnn*.h /usr/local/cuda-11.1/include
#sudo cp ~/Downloads/cuda/lib64/libcudnn* /usr/local/cuda-11.1/lib64
#sudo chmod a+r /usr/local/cuda-11.1/include/cudnn*.h /usr/local/cuda-11.1/lib64/libcudnn*

echo 'Process complete. Reboot your computer for NVIDIA drivers to take effect then refer to the README file for cuda post-installation'
