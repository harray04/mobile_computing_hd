import os
from google.cloud import storage
import csv
import firebase_admin
from firebase_admin import db
from firebase_admin import credentials
from firebase_admin.credentials import Certificate


os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = './credo.json'

def init_app():
    if (not len(firebase_admin._apps)):
        cred = credentials.Certificate('./credo.json')        
        fA = firebase_admin.initialize_app(cred, {'databaseURL': 'https://coinz-c5130.firebaseio.com'})

def set_trained(trained_body):
    init_app()
    root = db.reference()
    # Add a new user under /users.
    ready_for_knn = root.child('TRAIN').set(trained_body)

def get_trained():
    init_app()
    root = db.reference('TRAIN')
    # Add a new user under /users.
    trained_content = root.get()
    return trained_content
    
def set_flag(count):
    init_app()
    root = db.reference()
    # Add a new user under /users.
    ready_for_knn = root.child('KNN').set({
        'is_ready' : True,
        'act_count' : count
    })


def reset_flag():
    init_app()
    root = db.reference()
    # Add a new user under /users.
    root.child('KNN').set({
        'is_ready' : False,
        'act_count' : 0
    })

def get_flag():
    init_app()
    root = db.reference('KNN')
    # Add a new user under /users.
    ready_for_knn = root.get()
    return ready_for_knn


def set_activites(json):
    init_app()
    root = db.reference()
    count = len(json)
    set_flag(count)
    root.child('Activities').set(json)

def get_activities():
    init_app()
    activities = db.reference('Activities').get()
    print(activities)
    return activities
   

bucket_name = 'coinz-c5130.appspot.com'
def download_reader(bucketName):
    source_blob_name = bucketName + '.csv'
    #DOWNLOr
    storage_client = storage.Client()
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.get_blob(source_blob_name)
    fileData = blob.download_as_string()
    strindData = fileData.decode('UTF-8')
    return string_to_csv_reader(strindData)
    
def string_to_csv_reader(strindData):
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
