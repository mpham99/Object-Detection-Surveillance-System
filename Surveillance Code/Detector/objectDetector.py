# imports
import sys

import cv2
import numpy as np
import os

class ObjectDetector:

    net = None
    classes = None
    layer_names = None
    objects_to_detect = None
    weights_file = None
    cfg_file = None
    labels = None
    colours = None
    confidence_factor = 0.4
    threshold_factor = 0.6

    #persistant data from last detected frame
    prs_boxes = None
    prs_confidences = None
    prs_class_ids = None

    # Name: Parameterized Constructor
    # Purpose: The constructor calls set_config to configure the Detector
    def __init__(self, weightsPath, configPath, namesPath, useGPU, objects=None):
        print(self.set_config(weightsPath, configPath, namesPath, useGPU, objects))

    # Name: set_config
    # Purpose: set configurations for detector
    # Path Variables: where the appropriate model files are located
    # useGPU: boolean variable for enabling GPU acceleration
    # objects: a list containing name file objects to restrict detection to
    def set_config(self, weights_path, config_path, names_path, use_gpu=0, objects=None):

        # checking parameters
        if not (isinstance(weights_path, str) and isinstance(config_path, str) and isinstance(names_path, str)):
            print("Invalid configurations parameters")
            sys.exit(-1)
        elif not (os.path.isfile(weights_path) and os.path.isfile(config_path) and os.path.isfile(names_path)):
            print("Invalid configurations parameters, files not found")
            sys.exit(-1)

        if not (objects is None):
            if not (isinstance(objects, list)):
                print("objects must be of type 'list'")
                sys.exit(-1)

        try:
            # Load the object detector
            self.net = cv2.dnn.readNetFromDarknet(config_path, weights_path)

            # check if GPU should be used
            if use_gpu:
                # set CUDA as the preferable backend and target
                # This will crash if dependencies are not configured properly
                self.net.setPreferableBackend(cv2.dnn.DNN_BACKEND_CUDA)
                self.net.setPreferableTarget(cv2.dnn.DNN_TARGET_CUDA)

            # set classes global variable
            classes = []
            with open(names_path, "r") as f:
                self.classes = [line.strip() for line in f.readlines()]

            ln = self.net.getLayerNames()
            self.layer_names = [ln[i[0] - 1] for i in self.net.getUnconnectedOutLayers()]

            # Set a list of objects to restrict the detector to
            self.objects_to_detect = objects

            # Store model to global variables
            self.weights_file = weights_path
            self.cfg_file = config_path
            self.labels = open(names_path).read().strip().split("\n")

            # initialize a list of colors to diversify class labels
            np.random.seed(22)
            self.colours = np.random.randint(0, 255, size=(len(self.labels), 3), dtype="uint8")

        except:
            print("Failed to configure the detector, perhaps provided files are corrupt/invalid")
            sys.exit(-1)


    # Name: is_configured
    # Purpose: check is required configurations are set
    def is_configured(self):
        # checking if net has been set
        if (self.net is None):
            print("'net' has not been set")
            return False
        # checking if classes has been set
        elif (self.classes is None):
            print("'classes' has not been set")
            return False
        # checking if layer_names has been set
        elif (self.layer_names is None):
            print("'layer_names' has not been set")
            return False
        else:
            return True

    # Name: detect
    # Purpose: takes an image and performs object detection on it.
    def detect(self, frame):
        # Check if detector is configured
        if not (self.is_configured()):
            return "Detector configuration not set\n"

        # process the input frame
        try:
            # grab frame dimensions
            (frameH, frameW) = frame.shape[:2]

            # Construct a blob from the input frame and perform
            # a forward pass of the YOLO object detector
            blob = cv2.dnn.blobFromImage(frame, 1 / 255.0, (416, 416),
                                         swapRB=True, crop=False)
            self.net.setInput(blob)
            layer_outputs = self.net.forward(self.layer_names)

            # initialize list of detected bounding boxes,
            # confidences, and class IDs
            boxes = []
            confidences = []
            class_ids = []

            # loop over each layer output
            for output in layer_outputs:
                # loop over each detection
                for detection in output:
                    # extract class ID and confidence of the current detection
                    scores = detection[5:]
                    class_id = np.argmax(scores)
                    confidence = scores[class_id]

                    # Filter out weak predictions
                    if confidence > 0.5:
                        # downscale bounding box coordinates by the size of the image
                        box = detection[0:4] * np.array([frameW, frameH, frameW, frameH])
                        (centerX, centerY, width, height) = box.astype("int")
                        # derive top left corner of the bounding box
                        # using its center coordinates
                        x = int(centerX - (width / 2))
                        y = int(centerY - (height / 2))

                        # update list of bounding box coordinates,
                        # confidences, and classIDs
                        boxes.append([x, y, int(width), int(height)])
                        confidences.append(float(confidence))
                        class_ids.append(class_id)

            # Suppress weak, overlapping bounding boxes
            # by applying non-maxima suppression
            indexes = cv2.dnn.NMSBoxes(boxes, confidences, self.confidence_factor, self.threshold_factor)

            # process bounding boxes if a detection exists
            font = cv2.FONT_HERSHEY_PLAIN
            for i in range(len(boxes)): # len(indexes) > 0:
                # loop over kept indexes
                if i in indexes:
                    # extract the bounding box coordinates
                    x, y, w, h = boxes[i]

                    # change the look & feel of the bounding box
                    colour = [int(c) for c in self.colours[class_ids[i]]]
                    label = str(self.classes[class_ids[i]])
                    text = "{}: {:.2f}".format(label, confidences[i])

                    # Checking if objects to detect was set
                    if not (self.objects_to_detect is None):
                        # Only draw a box around the objects that the user selected
                        for obj in self.objects_to_detect:
                            if label == obj:
                                cv2.rectangle(frame, (x, y), (x + w, y + h), colour, 2)
                                cv2.putText(frame, text, (x, y + 30), font, 1, (255, 255, 255), 2)
                    else:
                        cv2.rectangle(frame, (x, y), (x + w, y + h), colour, 2)
                        cv2.putText(frame, text, (x, y + 30), font, 1, (255, 255, 255), 2)

            self.prs_boxes = boxes
            self.prs_confidences = confidences
            self.prs_class_ids = class_ids
        except:
            return "Something went wrong while running the detector"

    # Name: get_frame_count
    # Purpose: writes object count to the frame
    def frame_count(self, frame, objects=None):
        vehicle_list = ["car", "truck", "motorbike", "bus", "train", "bicycle"]
        condensed_ids = []
        occurances = []

        #check for proper parameters
        if not (objects or isinstance(objects, list)):
                return "objects must be of type 'list'"
        #limit length of object list (max 8)
        if len(objects) > 8:
            objects = objects[:8]

        #Condense overlapping class ids into unique ids to accurately count the quantity of objects
        idxs = cv2.dnn.NMSBoxes(self.prs_boxes, self.prs_confidences, self.confidence_factor, self.threshold_factor)
        for i in range(len(self.prs_class_ids)):
            if i in idxs:
                condensed_ids.append(self.classes[self.prs_class_ids[i]])

        #count occurances & append information to frame
        for i in range(len(objects)):
            if objects[i] is "vehicles":
                vehicle_count = 0
                for vehicle in vehicle_list:
                    vehicle_count += condensed_ids.count(vehicle)
                occurances.append(vehicle_count)
                text = "Total vehicles = " + str(occurances[i])
            else:
                occurances.append(condensed_ids.count(objects[i]))
                text = objects[i] + " = " + str(occurances[i])
            cv2.putText(frame, text,(5, (i * 20) + 15), cv2.FONT_HERSHEY_SIMPLEX,
                        0.65, (255, 255, 0), 2, cv2.LINE_AA, False)

    # Name: get_objects
    # Purpose: Returns a list of the objects that the detector is setup to detect
    def get_objects(self):
        return self.classes


    # Name: set_objects
    # Purpose: Sets the objects that the detector will detect
    def set_objects(self, objects):
        self.objects_to_detect = objects


    # Name: get_config
    # Purpose: Returns the configuration of the detector
    def get_config(self):
        if not (self.is_configured()):
            return "Detector configuation not set\n"

        objects = ""
        for obj in self.bjects_to_detect:
            objects = objects + obj + ", "
        objects = objects[:-2]
        return "Weights: " + self.weights_file + "\n" + "Cfg: " + self.cfg_file + "\n" + "Names: " + self.labels + "\n" + "Objects being detected: " + objects

