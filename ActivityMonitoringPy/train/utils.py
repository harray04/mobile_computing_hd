from google.cloud import storage
import csv
import firebase_admin
from firebase_admin import db
from firebase_admin import credentials
from firebase_admin.credentials import Certificate

def init_app():
    cred = credentials.Certificate('/home/harald/Dokumente/Mobile_Computing/credo.json')
    fA = firebase_admin.initialize_app(cred, {'databaseURL': 'https://coinz-c5130.firebaseio.com'})
    

def set_flag():
    root = db.reference()
    # Add a new user under /users.
    ready_for_knn = root.child('KNN').set({
        'is_ready' : True
    })

def get_flag():
    root = db.reference('KNN')
    # Add a new user under /users.
    ready_for_knn = root.get()
    return ready_for_knn

bucket_name = 'coinz-c5130.appspot.com'
def download_reader(bucketName):
    source_blob_name = bucketName + '.csv'
    #DOWNLOr
    storage_client = storage.Client()
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.get_blob(source_blob_name)
    fileData = blob.download_as_string()
    strindData = fileData.decode('UTF-8')
    if strindData[len(strindData) - 1] == '\n':
        strindData = strindData[:-1]
    reader = csv.reader(strindData.split('\n'), delimiter=',')
    return reader
    

def upload_file(fileName):
    client = storage.Client()
    bucket = client.get_bucket(bucket_name)
    blob = bucket.blob(fileName)
    blob.upload_from_filename(fileName)


def upload_string(fileName, fileData):
    client = storage.Client()
    bucket = client.get_bucket(bucket_name)
    blob = bucket.blob(fileName)
    blob.upload_from_string(fileData)
