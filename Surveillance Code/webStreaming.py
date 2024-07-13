# Initialize Flask web server
# Detect and stream video output

# Import
import atexit
import json
from Detector.objectDetector import ObjectDetector
from RestAPI.RestAPI import RestAPI
import threading
import datetime
import subprocess
import string
import random
import time
import cv2
import os

# Disable ssl warnings
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Initialize output frame for streaming and thread-safe
outputFrame = None
lock = threading.Lock()

# GLOBALS
with open("api_config.json", "r") as json_data_file:
    config = json.load(json_data_file)
rtmp_url = config['rtmp_url']
fps = 20
width = None
height = None
command = None
pipe = None
has_updated = False

# Use different set up for Windows and Ubuntu VM
# Check OS type to set the right options
if os.name == 'nt':
    # For Windows
    stream = cv2.VideoCapture(0)
else:
    # For Linux
    stream = cv2.VideoCapture(0, cv2.CAP_ANY)
    stream.set(3, 640);
    stream.set(4, 480);

# TODO: For debug
# time.sleep(5.0)
if not (stream.isOpened()):
    print("Failed to get Video Capture")
    exit(-1)


# Name: generate_key
# Purpose: Generate key for streaming purpose
# Author: Minh Duc Pham
def generate_key(length):
    letters = string.ascii_lowercase
    result = ''.join(random.sample(letters, length))
    print('The key for stream URL is: ', result)
    return result


def start_stream(key):
    # Gather video info for ffmpeg
    global width
    global height
    global command
    global pipe

    width = int(stream.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(stream.get(cv2.CAP_PROP_FRAME_HEIGHT))

    # Command for ffmpeg
    # command = ['ffmpeg',
    #            '-y',
    #            '-f', 'rawvideo',
    #            '-vcodec', 'rawvideo',
    #            '-pix_fmt', 'bgr24',
    #            '-s', "{}x{}".format(width, height),
    #            '-r', str(fps),
    #            '-i', '-',
    #            '-c:v', 'h264_nvenc',
    #            '-pix_fmt', 'yuv420p',
    #            '-f', 'flv',
    #            '-tune', 'zerolatency',
    #            '-crf', '40',
    #            rtmp_url + key]
    command = ['ffmpeg',
               '-y',
               '-f', 'rawvideo',
               '-vcodec', 'rawvideo',
               '-pix_fmt', 'bgr24',
               '-s', "{}x{}".format(width, height),
               '-r', str(fps),
               '-i', '-',
               '-c:v', 'h264_nvenc',
               '-pix_fmt', 'yuv420p',
               '-f', 'flv',
               '-tune', 'zerolatency',
               '-crf', '40',
               rtmp_url + key]

    # Pipe and fetch frame data
    pipe = subprocess.Popen(command, stdin=subprocess.PIPE)


# Name: perform_detection
# Purpose: Call the detector and put bounding box on frame
# Author: Minh Duc Pham, Joshua Mukasa
def perform_detection(api):
    check_for_update_time = time.time()
    # Global variables
    global stream, outputFrame, lock

    # Initialize detector
    detector = ObjectDetector(api.weights, api.cfg, api.names, config['GPU'])

    # Loop over frames from video live feed
    while True:

        if has_updated:
            detector.set_config(api.weights, api.cfg, api.names, config['GPU'], api.objects)

        # Read the frame and resize, rescale
        ret, frame = stream.read()

        if ret is not True:
            exit()

        # TODO: Use grayscale for detection will be faster but currently will mess things up
        # gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        # gray_frame = cv2.GaussianBlur(gray_frame, (7, 7), 0)

        # Perform detection
        detector.detect(frame)
        detector.frame_count(frame, api.count)

        # Display timestamp
        timestamp = datetime.datetime.now()
        cv2.putText(frame,
                    timestamp.strftime("%A %d %B %Y %I:%M:%S%p"),
                    (10, frame.shape[0] - 10),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    0.35,
                    (38, 84, 192),
                    1)

        # Show video locally (for development)
        # cv2.imshow("Frame", frame)
        # cv2.waitKey(1) & 0xFF

        # Write to ffmpeg stream
        pipe.stdin.write(frame.tobytes())


def check_for_update(api):
    global has_updated
    while True:
        time.sleep(5)
        has_updated = api.compare_config()


# Name: Main function
# Purpose: Start web detection and update device IP address in database
# Author: Minh Duc Pham, Joshua Mukasa
if __name__ == '__main__':
    key = generate_key(8)
    # Initialize API, update IP address and stream key on start up and retrieve configuration files
    api = RestAPI()
    api.put_stream_key_update(key)

    update_thread = threading.Thread(target=check_for_update, args=(api,))
    update_thread.start()

    start_stream(key)
    perform_detection(api) # Perform detection and stream to RTMP server

# release the video stream pointer

# stream.release()
atexit.register(stream.release)
