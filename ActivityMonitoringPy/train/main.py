import firebase_admin
from firebase_admin import firestore
import flask
from flask import Flask, flash, request, redirect, url_for
from werkzeug.utils import secure_filename
import feature
from feature import write_to_file, write_single_feature
import knn
import os
import utils
import numpy as np

def train_feature(count): # TODO other errors like file not found...
    file = ''
    sampleCount = 0
    testFile = ''
    trainFile = ''
    trainSampleCount = 0
    testSampleCount = 0
    for i in range(count):
        print('file nr.:' + str(i))
        csv_reader = utils.download_reader(str(i))
        trainSet, testSet, trainSize, testSize = write_to_file(csv_reader, i)
        trainFile += trainSet
        testFile += testSet
        trainSampleCount += trainSize
        testSampleCount += testSize
        #os.remove(fileName)
    uploadFile = str(trainSampleCount) + ',' + str(6) + ',' + str(0) + '\n'
    uploadFile += trainFile
    utils.upload_string('train_features.csv', uploadFile)

    uploadFile = str(testSampleCount) + ',' + str(6) + ',' + str(0) + '\n'
    uploadFile += testFile
    utils.upload_string('test_features.csv', uploadFile)
    utils.set_trained({
        'is_ready': True
    })
    return flask.jsonify({'success': True})

def hasDoneFit():
    ready_flag = utils.get_flag()
    return flask.jsonify(ready_flag)

def allowed_file(filename):
    ALLOWED_EXTENSIONS = set(['csv'])
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS
def train(request):
    if request.path.startswith('/'):
        count = request.path.lstrip('/')
       # if count == 'instant':
        if count == 'fit':
            if request.method == 'GET':
                return flask.jsonify(utils.get_trained()), 200
        if count == 'resetAll':
            if request.method == 'PUT':
                utils.set_trained({
                    'is_ready': False
                })
                retVal = utils.reset_flag()
                return flask.jsonify({'success': True}), 200
        if count == 'resetExt':
            if request.method == 'PUT':
                utils.set_trained({
                    'is_ready': False
                })
                return flask.jsonify({'success': True}), 200
        if count == 'act':
            if request.method == 'GET':
                return flask.jsonify(utils.get_activities()), 200
            if request.method == 'POST':
                request_json = request.get_json(silent=True)
                request_jsonified = flask.jsonify(request_json)
                utils.set_activites(request_json)
                return request_jsonified, 200
        #if count == 'fit':
        #    if request.method == 'GET':
        #        return knn.fit_knn(), 200
        if count == 'prob':
            if request.method == 'GET':
                return hasDoneFit(), 200
            if request.method == 'POST':
                if 'file' not in request.files:
                    flash('No file part')
                    return flask.jsonify({'success': False}), 400
                file = request.files['file']
                if file.filename == '':
                    flash('No selected file')
                    return flask.jsonify({'success': False}), 400
                if file and allowed_file(file.filename):
                    filename = secure_filename(file.filename)
                    fileData = file.read()
                    csvData = str(fileData, 'UTF-8')
                    csvReader = utils.string_to_csv_reader(csvData)
                    file = write_single_feature(csvReader)
                    currentDataArr = np.fromstring(file, dtype=float, sep=',')
                    #probability = knn.fit_knn(currentDataArr)
                    print(file)
                    #file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
                    return flask.jsonify({'success': True, 'data': file}), 200 #, 'prob': probability}), 200
                else:
                    return 'Wrong file extension', 400
        try:
            if int(count) <= 0:
                return 'No number supported', 400
            elif int(count) > 0:
                if request.method == 'GET':
                    return train_feature(int(count))
            else:
                return 'Method not supported', 405
        except ValueError:
               return 'Method not supported or ValueError', 400 

    return 'URL not found', 404
