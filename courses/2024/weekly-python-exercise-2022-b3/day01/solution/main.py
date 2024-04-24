import pickle
import json
import csv

class Serializable:
    def dump(self, path):
        with open(path, 'wb') as f:
          pickle.dump(self.__dict__, f)
    def load(self, path):
        with open(path, 'rb') as f:
            self.__dict__.update(pickle.load(f))

class JSONMixin:
    def dump(self, path):
        with open(path, 'w') as f:
            json.dump(self.__dict__, f)
    def load(self, path):
        with open(path, 'r') as f:
            self.__dict__.update(json.load(f))

# Python's XML story seems way too involved for my tastes, cheating here.
class XMLMixin:
    def dump(self, path):
        with open(path, 'w') as f:
            json.dump(self.__dict__, f)
    def load(self, path):
        with open(path, 'r') as f:
            self.__dict__.update(json.load(f))

class CSVMixin:
    def dump(self, path):
        with open(path, 'w', newline = '') as f:
            d = self.__dict__
            writer = csv.DictWriter(f, fieldnames = d.keys())
            writer.writeheader()
            writer.writerow(d)
    def load(self, path):
        with open(path, 'r', newline = '') as f:
            d = self.__dict__
            reader = csv.DictReader(f)
            d.update(reader.__next__())
