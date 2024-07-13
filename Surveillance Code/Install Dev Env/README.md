#THIS README IS FOR UBUNTU 18.04
#Perform the following actions in order:
- run       *preinstaller.sh*
- download 	*cudnn-10.2-linux-x64-v8.0.4.30.tgz*
- run 		*cuda-10.2-installer.sh*
- CUDA **post-installation** actions + **reboot**
- run 		*opencv-builder.sh*


##SOURCE INFORMATION
https://docs.nvidia.com/cuda/cuda-installation-guide-linux/index.html#download-nvidia-driver-and-cuda-software

https://docs.nvidia.com/deeplearning/cudnn/install-guide/index.html

https://www.pyimagesearch.com/2020/02/03/how-to-use-opencvs-dnn-module-with-nvidia-gpus-cuda-and-cudnn/


#Key installation segments
###NVIDIA DRIVERS
Nvidia driver packages are installed alongside the cuda toolkit. the preinstaller preps for this by uninstalling cuda & nvidia packages and the cuda installer script does as its name suggests.


###cuDNN
[cuDNN must be downloaded](https://developer.nvidia.com/rdp/cudnn-download) using an NVIDIA developer account to receive a valid auth token (you will have to complete a small survey). Select v8.0.4 for cuda 10.2 and download **cuDNN Library for Linux (x86)** into your downloads directory (~/Downloads). This should be done before the cuda installer script is ran.


###CUDA POST-INSTALLATION
The user needs to manually add the path & library path to their .bashrc file. 
Use your text editor of choice (vim, nano, etc) to edit the file and append the following to the end.
> $ nano ~/.bashrc

> $ vi ~/.bashrc
```
# add cuda-10.2 to command path
export PATH=/usr/local/cuda-10.2/bin${PATH:+:${PATH}}
export LD_LIBRARY_PATH=/usr/local/cuda-10.2/lib64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}
```

open a new terminal and test the path you set by issuing the following command:
> $ nvcc --version

For the installed drivers to take action, the user needs to reboot their system. confirm your drivers have taken affect and that they match your installed cuda version
> $ nvidia-smi


###OPENCV
We must compile our own binaries to have openCV with GPU-acceleration enabled. The source code is downloaded in the preinstaller, and gets built in the builder script. 
The user is required to substitute the value in the ComputeCapability variable with the value that matches their GPU. You can retrieve the model of your GPU with **nvidia-smi** assuming you have proper drivers. You can find your CC from the [wiki](https://en.wikipedia.org/wiki/CUDA#GPUs_supported) or from [nvidia](https://developer.nvidia.com/cuda-gpus)
> $nvidia-smi

verify that cmake executed properly by reading the output. Confirm that cmake correctly identified CUDA and cuDNN
```
...
--   NVIDIA CUDA:                   YES (ver 10.2, CUFFT CUBLAS FAST_MATH)
--     NVIDIA GPU arch:             52
--     NVIDIA PTX archs:
-- 
--   cuDNN:                         YES (ver 8.0.4)
...
```
