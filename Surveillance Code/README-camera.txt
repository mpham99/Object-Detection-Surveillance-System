# The camera being used may have some problems due to UVC and codec files
# Run these command to fix those functionalities

# 1. UVC camera compatible
sudo rmmod uvcvideo
sudo modprobe uvcvideo quirks=4 nodrop=128 timeout=6000

# 2. MJPEG output for camera output

# Create a dummy camera (One time only)
v4l2-ctl --list-devices # View connected camera
ffmpeg -f v4l2 -list_formats all -i /dev/video0  #use your camera here from step 1

# Output to to the dummy camera (Before running the code)
ffmpeg -f v4l2               \      
	-input_format mjpeg  \      
	-framerate 30        \       
	-video_size 640x480  \      
	-i /dev/video0       \       
	-pix_fmt yuyv422     \       
	-f v4l2 /dev/video2

