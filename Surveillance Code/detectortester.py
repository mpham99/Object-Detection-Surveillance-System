from Detector.objectDetector import ObjectDetector
import cv2

# Initialize detector
detector = ObjectDetector("yolo-coco/yolov4.weights", "yolo-coco/yolov4.cfg", "yolo-coco/coco.names", 1, ["person", "car", "truck", "bus", "motorbike", "train", "dog", "traffic light"])

# dog.mp4, streets.mp4, traffic.mp4, 0 for livefeed
vs = cv2.VideoCapture("pursuit.mp4")

# Loop over frames from video live feed
while True:
    # Read the frame and resize, rescale
    (grabbed, frame) = vs.read()

    # check if there are no more frames
    if not grabbed:
        print("video stream has ended")
        exit()

    # call detector on frame
    detector.detect(frame)
    detector.frame_count(frame, ["vehicles", "person", "car", "dog"])

    # Show frame locally (for development)
    cv2.imshow("testing :)", frame)
    cv2.waitKey(1) & 0xFF