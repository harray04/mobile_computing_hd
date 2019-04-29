import firebase_admin
import feature
from feature import write_to_file
from google.cloud import storage
import flask
import knn
import os

def download_file(bucketName, destinationName):
    bucket_name = 'fir-functions-17922.appspot.com'
    source_blob_name = bucketName + '.csv'
    #DOWNLOAD
    storage_client = storage.Client()
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(source_blob_name)
    blob.download_to_filename(destinationName)




def train_feature(count): # TODO other errors like file not found...
    fileName = 'features.csv'
    for i in range(count):
        print('file nr.:' + str(i))
        download_file(str(i), fileName)
        write_to_file(fileName, i)
        #os.remove(fileName)
    return flask.jsonify({'success': True})

def hasDoneFit():
    if(knn.fitDone):
        return flask.jsonify({'success': True})
    else:
        return flask.jsonify({'success': False})

def train(request):
    if request.path.startswith('/'):
        count = request.path.lstrip('/')
        
        if count == 'fit':
            if request.method == 'GET':
                return knn.fit_knn(), 200
        if count == 'prob':
            if request.method == 'GET':
                return hasDoneFit(), 200
        if int(count) <= 0:
            return 'No number supported', 400
        elif int(count) > 0:
            if request.method == 'GET':
                return train_feature(int(count))
        else:
            return 'Method not supported', 405

    return 'URL not found', 404