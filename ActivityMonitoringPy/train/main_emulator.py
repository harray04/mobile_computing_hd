# File: main_emulate.py
import flask
from main import train

app = flask.Flask(__name__)
methods = ['GET', 'POST', 'PUT', 'DELETE']

@app.route('/train', methods=methods)
@app.route('/train/<path>', methods=methods)
def catch_all(path=''):
    flask.request.path = '/' + path
    return train(flask.request)

if __name__ == '__main__':
    app.run()