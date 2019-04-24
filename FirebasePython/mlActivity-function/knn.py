import sklearn
from sklearn.neighbors import KNeighborsClassifier
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn import metrics

data_path = 'features_final.csv'

file_data = np.loadtxt(data_path,delimiter=',')
data =[]
final_data= []
target_data = []

for i ,val in enumerate(file_data):
    data = [file_data[i][0],file_data[i][1],file_data[i][2],file_data[i][3],file_data[i][4],file_data[i][5]]
    final_data.append(data)
    target_data.append(file_data[i][6])
                       
X_train, X_test, y_train, y_test = train_test_split(final_data, target_data, test_size=0.3)

knn = KNeighborsClassifier(n_neighbors=1)
knn.fit(X_train, y_train)
y_pred = knn.predict(X_test)

print("Accuracy:", metrics.accuracy_score(y_test, y_pred))


    
    
