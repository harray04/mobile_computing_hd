import firebase_admin
from firebase_admin import firestore
import flask
import feature
from feature import write_to_file
import knn
import os
import utils

utils.init_app()

def train_feature(count): # TODO other errors like file not found...
    file = ''
    for i in range(count):
        print('file nr.:' + str(i))
        csv_reader = utils.download_reader(str(i))
        file += write_to_file(csv_reader, i)
        #os.remove(fileName)
    
    if(len(file) > 0):
        utils.upload_string('features_final.csv', file)
    utils.set_flag()
    return flask.jsonify({'success': True})

def hasDoneFit():
    ready_flag = utils.get_flag()
    return flask.jsonify(ready_flag)

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
