import pickle
import json
import csv
import xml.etree.ElementTree as ET

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

class XMLMixin:
    def dump(self, path):
        a = ET.Element('attributes')
        for name, value in vars(self).items():
            node = ET.SubElement(a, name, value = str(value))
        tree = ET.ElementTree(a)

        with open(path, 'wb') as f:
            tree.write(f)

    def load(self, path):
        tree = ET.parse(open(path, 'rb'))
        for parent in tree.iter():
            for child in parent:
                setattr(self, child.tag, child.attrib['value'])

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
