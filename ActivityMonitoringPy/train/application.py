import firebase_admin
from firebase_admin import firestore
from flask import Flask, flash, request, redirect, url_for, jsonify
from flask.views import View
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
    return jsonify({'success': True})
    
# print a nice greeting.
def say_hello(username = "World"):
    return '<p>Hello %s!</p>\n' % username

class MyView(View):
    methods = ['GET', 'POST']

    def dispatch_request(self):
        if request.method == 'GET':
            return train_feature(2)


# some bits of text for the page.
header_text = '''
    <html>\n<head> <title>EB Flask Test</title> </head>\n<body>'''
instructions = '''
    <p><em>Hint</em>: This is a RESTful web service! Append a username
    to the URL (for example: <code>/Thelonious</code>) to say hello to
    someone specific.</p>\n'''
home_link = '<p><a href="/">Back</a></p>\n'
footer_text = '</body>\n</html>'

# EB looks for an 'application' callable by default.
application = Flask(__name__)
application.add_url_rule('/myview', view_func=MyView.as_view('myview'))
# add a rule for the index page.

# add a rule when the page is accessed with a name appended to the site
# URL.
application.add_url_rule('/<username>', 'hello', (lambda username:
    header_text + say_hello(username) + home_link + footer_text))

# run the app.
if __name__ == "__main__":
    # Setting debug to True enables debug output. This line should be
    # removed before deploying a production app.
    application.debug = True
    application.run()