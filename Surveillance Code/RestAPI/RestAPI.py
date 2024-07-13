import json
import os
import sys
from os import path
import time

from pip._vendor import requests
from .BearerAuth import BearerAuth


# Retrieve serial number from hardware
def get_serial_number():
    os_type = sys.platform.lower()
    if "darwin" in os_type:
        command = "ioreg -l | grep IOPlatformSerialNumber"
    elif "win" in os_type:
        command = "wmic bios get serialnumber"
    elif "linux" in os_type:
        command = "hal-get-property --udi /org/freedesktop/" \
                  "Hal/devices/computer --key system.hardware.uuid"
    return os.popen(command).read().replace("\n", "").replace("  ", "").replace(" ", "")


# Handles all endpoint calls to the REST application
class RestAPI:
    config = None
    host = None
    credential = None
    access_token = None
    refresh_token = None
    expiration = None
    weights = None
    cfg = None
    names = None
    objects = None
    verify_requests = False
    model = None
    count = None

    def __init__(self):
        try:
            
            # Get Access Token
            self.get_tokens()
            
            # Validate Token
            if self.access_token is None or self.expiration is None or self.refresh_token is None:
                raise Exception("Request for access token failed.")

            # Register this hardware with the REST application
            name = self.config['jetson_name']
            hardware_data = {"name": name, "serial": "test_serial_number", "streamKey": "test_stream_key"}
            response = requests.post(self.host + "/api/hardware",
                                     json=hardware_data,
                                     auth=BearerAuth(self.access_token),
                                     verify=self.verify_requests)

            # Retrieve models from endpoint
            current_config = {"hardware": name}
            print("Attempting to get models...")
            response = requests.post(self.host + "/api/detection/config/compare",
                                     json=current_config,
                                     auth=BearerAuth(self.access_token),
                                     verify=self.verify_requests)

            # If response is invalid, keep trying to retrieve models from endpoint
            while response.status_code != 200:
                time.sleep(5)
                print("Error...Failed to get models\nAttempting to get models...")
                response = requests.post(self.host + "api/detection/config/compare",
                                         json=current_config,
                                         auth=BearerAuth(self.access_token),
                                         verify=self.verify_requests)

            # Set model information based on response
            self.model = response.json()['model']
            self.count = response.json()['count']
            self.objects = response.json()['objects']

            # Retrieve config files for trained model
            self.get_config_files(self.model)

        except Exception as e:
            print(e)
            sys.exit(-1)

    def get_tokens(self):
        # open config file
        with open("api_config.json", "r") as json_data_file:
            self.config = json.load(json_data_file)
            self.host = self.config['url']

        try:
            self.credential = json.dumps(self.config['credential'])
            # request an access token
            request = requests.post(self.host + self.config['url_login'],
                                    data=self.credential,
                                    headers={'Content-Type': 'application/json'},
                                    verify=self.verify_requests)

            if request.status_code != 200:
                print("Failed to authenticate")
                sys.exit(-1)

            # Set token variables
            self.access_token = request.json()['accessToken']
            self.refresh_token = request.json()['refreshToken']
            self.expiration = time.time() + request.json()['expiration']

        except Exception as e:
            print("Failed to authenticate")
            sys.exit(-1)

    class Decorators:
        @staticmethod
        def refreshToken(decorated):
            # the function that is used to check
            # the JWT and refresh if necessary
            def wrapper(api, *args, **kwargs):
                if time.time() > api.expiration:
                    api.get_tokens()
                return decorated(api, *args, **kwargs)

            return wrapper

    @Decorators.refreshToken
    def compare_config(self):

        # Compare current configuration with REST endpoint
        hardware = self.config['jetson_name']
        current_config = {"hardware": hardware,
                          "model": self.model,
                          "objects": self.objects,
                          "count": self.count}
        response = requests.post(self.host + "/api/detection/config/compare",
                                 json=current_config,
                                 auth=BearerAuth(self.access_token),
                                 verify=self.verify_requests)
        if response.status_code == 202:
            # Configuration was the same
            return False
        elif response.status_code == 200:
            # Configuration was different, update current configuration
            self.model = response.json()['model']
            self.count = response.json()['count']
            self.objects = response.json()['objects']

            # Retrieve config files for trained model
            self.get_config_files(self.model)
            return True
        else:
            print("Error occurred when checking for updates to config/compare")

    @Decorators.refreshToken
    def get_models_list(self):
        # Print the model list
        response = requests.get(self.host + self.config['url_models_list'],
                                auth=BearerAuth(self.access_token),
                                verify=self.verify_requests)
        print(response.text)

    @Decorators.refreshToken
    def post_register_device(self):
        # Register this hardware with the REST application
        name = self.config['jetson_name']
        serial = get_serial_number()
        body = {
            'name': name,
            'serial': serial
        }
        headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        response = requests.post(self.host + self.config['url_hardware_list'],
                                 data=json.dumps(body),
                                 auth=BearerAuth(self.access_token),
                                 headers=headers,
                                 verify=self.verify_requests)
        print(response.text)

    @Decorators.refreshToken
    def put_stream_key_update(self, key):
        # Update Stream Key
        name = self.config['jetson_name']
        response = requests.put(self.host + self.config['url_hardware_list'] + self.config['url_hardware_key']
                                + "/" + name,
                                data=key,
                                auth=BearerAuth(self.access_token),
                                verify=self.verify_requests)
        print(response.text)

    @Decorators.refreshToken
    def get_config_files(self, model):
        # Get Configuration Files (weights, cfg, names) from REST Endpoint
        folder = 'model'
        if not path.isdir(folder):
            os.mkdir(folder)

        weights_file = '{}/{}.weights'.format(folder, model)
        cfg_file = '{}/{}.cfg'.format(folder, model)
        names_file = '{}/{}.names'.format(folder, model)

        # Download weights File
        print("\nDownloading weights file...")
        response = requests.get(
            self.host + self.config['url_model_files'],
            params={'model': model, 'file-type': 'weights'},
            auth=BearerAuth(self.access_token),
            verify=self.verify_requests
        )
        open(weights_file, 'wb').write(response.content)
        self.weights = weights_file
        print('Downloaded weights file')

        # Download cfg File
        print("\nDownloading cfg file...")
        response = requests.get(
            self.host + self.config['url_model_files'],
            params={'model': model, 'file-type': 'config'},
            auth=BearerAuth(self.access_token),
            verify=self.verify_requests
        )
        open(cfg_file, 'wb').write(response.content)
        self.cfg = cfg_file
        print('Downloaded cfg file\n')

        # Download names File
        print("\nDownloading names file...")
        response = requests.get(
            self.host + self.config['url_model_files'],
            params={'model': model, 'file-type': 'names'},
            auth=BearerAuth(self.access_token),
            verify=self.verify_requests
        )
        open(names_file, 'wb').write(response.content)
        self.names = names_file
        print('Downloaded name file\n')
