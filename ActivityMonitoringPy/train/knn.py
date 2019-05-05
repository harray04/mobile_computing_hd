import sklearn
from sklearn.neighbors import KNeighborsClassifier
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn import metrics
import utils




def fit_knn(npArray=np.array([])):
    global fitDone
    global knn
    knn = KNeighborsClassifier(n_neighbors=5)
    data_path = 'features_final'
    csvReader = utils.download_reader(data_path)
    csvArray = list(csvReader)
    # transform data into numpy array
 
    csvArray = np.array(csvArray).astype(float)
    #file_data = np.loadtxt(data_path,delimiter=',')
    data =[]
    final_data= []
    target_data = []
    
    for i ,val in enumerate(csvArray):
        data = [csvArray[i][0],csvArray[i][1],csvArray[i][2],csvArray[i][3],csvArray[i][4],csvArray[i][5]]
        final_data.append(data)
        target_data.append(int(csvArray[i][6]))
                        
    X_train, X_test, y_train, y_test = train_test_split(final_data, target_data, test_size=0.2)

    knn.fit(X_train, y_train)
    y_pred_proba = knn.predict_proba(X_test)
    
    #print(y_pred_proba)
    fitDone = True

    y_pred = knn.predict(X_test)
    retVal = "Accuracy:" + str(metrics.accuracy_score(y_test, y_pred)) 
    if npArray.size != 0:
        npArray = np.reshape(npArray, (-1, npArray.size))
        retVal = retVal + ' Predict:' + str(knn.predict_proba(npArray))
    
    return retVal




    
    
